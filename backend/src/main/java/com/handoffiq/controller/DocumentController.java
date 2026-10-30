package com.handoffiq.controller;

import com.handoffiq.dto.DocumentResponse;
import com.handoffiq.entity.KnowledgeDocument;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Knowledge Base", description = "Document repository, file uploads, and knowledge indexing via Cloudinary")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload knowledge document or image to Cloudinary (PDF, DOCX, TXT, PNG, JPG)")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : 1L;
        DocumentResponse response = documentService.uploadDocument(file, title, description, category, projectId, isPublic, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List documents with optional category and project filtering")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long projectId) {
        List<DocumentResponse> documents = documentService.getAllDocuments(category, projectId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get knowledge document details by ID")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search knowledge documents by keyword or topic")
    public ResponseEntity<List<DocumentResponse>> searchDocuments(@RequestParam String query) {
        List<DocumentResponse> documents = documentService.searchDocuments(query);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document directly or redirect to secure Cloudinary CDN URL")
    public ResponseEntity<?> downloadDocument(@PathVariable Long id) {
        KnowledgeDocument doc = documentService.getDocumentEntity(id);

        // If the document has a public Cloudinary HTTPS URL, issue a 302 redirect for direct high-speed CDN delivery
        if (doc.getStoragePath() != null && (doc.getStoragePath().startsWith("http://") || doc.getStoragePath().startsWith("https://"))) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(doc.getStoragePath()))
                    .build();
        }

        // Otherwise stream the file bytes
        byte[] bytes = documentService.downloadDocument(id);
        String contentType = doc.getFileType() != null ? doc.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(bytes);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document from Cloudinary storage and database")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : 1L;
        documentService.deleteDocument(id, userId);
        return ResponseEntity.ok(Map.of("message", "Document deleted successfully", "documentId", id.toString()));
    }
}
