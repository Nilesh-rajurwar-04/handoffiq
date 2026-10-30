package com.handoffiq.service;

import com.handoffiq.dto.DocumentResponse;
import com.handoffiq.entity.DocumentChunk;
import com.handoffiq.entity.KnowledgeDocument;
import com.handoffiq.entity.Project;
import com.handoffiq.entity.User;
import com.handoffiq.exception.BadRequestException;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.DocumentChunkRepository;
import com.handoffiq.repository.KnowledgeDocumentRepository;
import com.handoffiq.repository.ProjectRepository;
import com.handoffiq.repository.UserRepository;
import com.handoffiq.storage.FileStorageService;
import com.handoffiq.storage.StorageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageService storageService;
    private final StructuredLogger structuredLogger;

    public DocumentService(
            KnowledgeDocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            FileStorageService storageService,
            StructuredLogger structuredLogger) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.structuredLogger = structuredLogger;
    }

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String title, String description, String category, Long projectId, Boolean isPublic, Long userId) {
        long start = System.currentTimeMillis();

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId).orElse(null);
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.txt";

        // Upload to Cloudinary (or local storage fallback)
        StorageResult storageResult = storageService.uploadFile(file, "handoffiq/documents");

        // Extract or read text content for AI semantic indexing
        String extractedText = "";
        try {
            if (originalFilename.endsWith(".txt") || originalFilename.endsWith(".md") || originalFilename.endsWith(".json")) {
                extractedText = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                extractedText = (title != null ? title : originalFilename) + ": " + (description != null ? description : "")
                        + " (Format: " + file.getContentType() + ", Cloudinary asset: " + storageResult.getPublicId() + ")";
            }
        } catch (Exception ex) {
            extractedText = (title != null ? title : originalFilename) + " - " + (description != null ? description : "");
        }

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(title != null && !title.trim().isEmpty() ? title.trim() : originalFilename);
        doc.setDescription(description);
        doc.setFileName(originalFilename);
        doc.setFileType(file.getContentType());
        doc.setFileSize(storageResult.getFileSize() > 0 ? storageResult.getFileSize() : file.getSize());
        doc.setStoragePath(storageResult.getFileUrl());
        doc.setCloudinaryPublicId(storageResult.getPublicId());
        doc.setCategory(category != null ? category.toUpperCase() : "PROJECT_DOC");
        doc.setUploadedBy(uploader);
        doc.setProject(project);
        doc.setIsPublic(Boolean.TRUE.equals(isPublic));
        doc.setContentText(extractedText);

        KnowledgeDocument saved = documentRepository.save(doc);

        // Create chunk for semantic search
        if (!extractedText.isEmpty()) {
            chunkRepository.save(new DocumentChunk(saved, 0, extractedText.substring(0, Math.min(extractedText.length(), 2000))));
        }

        structuredLogger.logDomainEvent("DOCUMENT_UPLOAD", userId, "SUCCESS", System.currentTimeMillis() - start, "Document uploaded to " + storageService.getStorageType() + ": " + saved.getTitle());
        return mapToDocumentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments(String category, Long projectId) {
        List<KnowledgeDocument> list;
        if (projectId != null) {
            list = documentRepository.findByProjectId(projectId);
        } else if (category != null) {
            list = documentRepository.findByCategory(category.toUpperCase());
        } else {
            list = documentRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return mapToDocumentResponse(doc);
    }

    @Transactional(readOnly = true)
    public KnowledgeDocument getDocumentEntity(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long id) {
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        String target = doc.getCloudinaryPublicId() != null && !doc.getCloudinaryPublicId().isEmpty()
                ? doc.getCloudinaryPublicId()
                : doc.getStoragePath();

        return storageService.downloadFile(target);
    }

    @Transactional
    public void deleteDocument(Long id, Long userId) {
        long start = System.currentTimeMillis();
        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        // Delete text search chunks
        chunkRepository.deleteByDocumentId(doc.getId());

        // Delete asset from Cloudinary (or local storage)
        String target = doc.getCloudinaryPublicId() != null && !doc.getCloudinaryPublicId().isEmpty()
                ? doc.getCloudinaryPublicId()
                : doc.getStoragePath();

        if (target != null && !target.isEmpty()) {
            boolean deleted = storageService.deleteFile(target);
            log.info("Cloud storage deletion result for doc #{}: {}", id, deleted);
        }

        documentRepository.delete(doc);
        structuredLogger.logDomainEvent("DOCUMENT_DELETE", userId, "SUCCESS", System.currentTimeMillis() - start, "Document deleted: " + doc.getTitle());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocuments(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllDocuments(null, null);
        }
        return documentRepository.searchDocuments(query.trim()).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    private DocumentResponse mapToDocumentResponse(KnowledgeDocument doc) {
        DocumentResponse resp = new DocumentResponse();
        resp.setId(doc.getId());
        resp.setTitle(doc.getTitle());
        resp.setDescription(doc.getDescription());
        resp.setFileName(doc.getFileName());
        resp.setFileType(doc.getFileType());
        resp.setFileSize(doc.getFileSize());
        resp.setStoragePath(doc.getStoragePath());
        resp.setFileUrl(doc.getStoragePath());
        resp.setPublicId(doc.getCloudinaryPublicId());
        resp.setStorageProvider(storageService.getStorageType());
        resp.setCategory(doc.getCategory());
        resp.setUploadedById(doc.getUploadedBy() != null ? doc.getUploadedBy().getId() : null);
        resp.setUploadedByName(doc.getUploadedBy() != null ? doc.getUploadedBy().getFullName() : null);
        resp.setProjectId(doc.getProject() != null ? doc.getProject().getId() : null);
        resp.setProjectName(doc.getProject() != null ? doc.getProject().getName() : "Enterprise Global");
        resp.setIsPublic(doc.getIsPublic());
        if (doc.getContentText() != null) {
            resp.setSnippet(doc.getContentText().substring(0, Math.min(doc.getContentText().length(), 200)) + "...");
        }
        resp.setCreatedAt(doc.getCreatedAt());
        return resp;
    }
}
