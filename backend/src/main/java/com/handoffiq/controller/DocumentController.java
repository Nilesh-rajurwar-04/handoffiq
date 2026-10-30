package com.handoffiq.controller;

import com.handoffiq.dto.DocumentResponse;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Knowledge Base", description = "Document repository, file uploads, and knowledge indexing")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload knowledge document or runbook (PDF, DOCX, TXT)")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            @AuthenticationPrincipal UserPrincipal principal) {
        DocumentResponse response = documentService.uploadDocument(file, title, description, category, projectId, isPublic, principal.getId());
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
}
