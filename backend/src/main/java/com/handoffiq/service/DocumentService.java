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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final StructuredLogger structuredLogger;

    @Value("${handoffiq.storage.local.upload-dir:./uploads/documents}")
    private String uploadDir;

    @Value("${handoffiq.storage.type:local}")
    private String storageType;

    public DocumentService(
            KnowledgeDocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            StructuredLogger structuredLogger) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.structuredLogger = structuredLogger;
    }

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String title, String description, String category, Long projectId, Boolean isPublic, Long userId) {
        long start = System.currentTimeMillis();

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId).orElse(null);
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.txt";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;
        String storagePath = uploadDir + "/" + storedFilename;

        try {
            Path targetDir = Paths.get(uploadDir);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Files.write(targetDir.resolve(storedFilename), file.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file on disk: " + e.getMessage());
        }

        // Extract or read text content
        String extractedText = "";
        try {
            if (originalFilename.endsWith(".txt") || originalFilename.endsWith(".md") || originalFilename.endsWith(".json")) {
                extractedText = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                extractedText = title + ": " + description + " (Uploaded binary format " + file.getContentType() + ")";
            }
        } catch (Exception ex) {
            extractedText = title + " - " + description;
        }

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(title != null && !title.trim().isEmpty() ? title.trim() : originalFilename);
        doc.setDescription(description);
        doc.setFileName(originalFilename);
        doc.setFileType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setStoragePath(storagePath);
        doc.setS3Key("documents/" + storedFilename);
        doc.setCategory(category != null ? category.toUpperCase() : "PROJECT_DOC");
        doc.setUploadedBy(uploader);
        doc.setProject(project);
        doc.setIsPublic(Boolean.TRUE.equals(isPublic));
        doc.setContentText(extractedText);

        KnowledgeDocument saved = documentRepository.save(doc);

        // Create chunk for search
        if (!extractedText.isEmpty()) {
            chunkRepository.save(new DocumentChunk(saved, 0, extractedText.substring(0, Math.min(extractedText.length(), 2000))));
        }

        structuredLogger.logDomainEvent("DOCUMENT_UPLOAD", userId, "SUCCESS", System.currentTimeMillis() - start, "Document uploaded: " + saved.getTitle());
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
