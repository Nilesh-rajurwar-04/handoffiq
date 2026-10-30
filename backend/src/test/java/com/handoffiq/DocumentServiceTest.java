package com.handoffiq;

import com.handoffiq.dto.DocumentResponse;
import com.handoffiq.entity.KnowledgeDocument;
import com.handoffiq.entity.User;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.DocumentChunkRepository;
import com.handoffiq.repository.KnowledgeDocumentRepository;
import com.handoffiq.repository.ProjectRepository;
import com.handoffiq.repository.UserRepository;
import com.handoffiq.service.DocumentService;
import com.handoffiq.storage.FileStorageService;
import com.handoffiq.storage.StorageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentServiceTest {

    private DocumentService documentService;
    private KnowledgeDocument savedDoc;
    private boolean chunkSaved;
    private boolean chunkDeleted;
    private boolean docDeleted;
    private boolean storageFileDeleted;

    @BeforeEach
    void setUp() {
        savedDoc = null;
        chunkSaved = false;
        chunkDeleted = false;
        docDeleted = false;
        storageFileDeleted = false;

        User testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Sarah Jenkins");

        KnowledgeDocument existingDoc = new KnowledgeDocument();
        existingDoc.setTitle("Architecture Specification");
        existingDoc.setStoragePath("https://res.cloudinary.com/handoffiq/raw/upload/v1/handoffiq/documents/spec.pdf");
        existingDoc.setCloudinaryPublicId("handoffiq/documents/spec_123");

        KnowledgeDocumentRepository docRepo = (KnowledgeDocumentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{KnowledgeDocumentRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        savedDoc = (KnowledgeDocument) args[0];
                        return savedDoc;
                    }
                    if ("findById".equals(name)) {
                        return Optional.of(existingDoc);
                    }
                    if ("delete".equals(name)) {
                        docDeleted = true;
                        return null;
                    }
                    return null;
                }
        );

        DocumentChunkRepository chunkRepo = (DocumentChunkRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{DocumentChunkRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        chunkSaved = true;
                        return args[0];
                    }
                    if ("deleteByDocumentId".equals(name)) {
                        chunkDeleted = true;
                        return null;
                    }
                    return null;
                }
        );

        ProjectRepository projRepo = (ProjectRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ProjectRepository.class},
                (proxy, method, args) -> Optional.empty()
        );

        UserRepository userRepo = (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(testUser);
                    }
                    return null;
                }
        );

        FileStorageService storageService = (FileStorageService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{FileStorageService.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getStorageType".equals(name)) {
                        return "CLOUDINARY";
                    }
                    if ("uploadFile".equals(name)) {
                        return new StorageResult(
                                "https://res.cloudinary.com/handoffiq/raw/upload/v1/handoffiq/documents/spec.pdf",
                                "handoffiq/documents/spec_123",
                                "spec.pdf",
                                "CLOUDINARY",
                                102400L,
                                "application/pdf",
                                "raw"
                        );
                    }
                    if ("downloadFile".equals(name)) {
                        return "Mock PDF content bytes from Cloudinary".getBytes();
                    }
                    if ("deleteFile".equals(name)) {
                        storageFileDeleted = true;
                        return true;
                    }
                    if ("getFileUrl".equals(name)) {
                        return args[0].toString();
                    }
                    return null;
                }
        );

        documentService = new DocumentService(
                docRepo,
                chunkRepo,
                projRepo,
                userRepo,
                storageService,
                new StructuredLogger()
        );
    }

    @Test
    @DisplayName("Should successfully upload document to Cloudinary and store metadata")
    void testUploadDocumentToCloudinary() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "spec.pdf",
                "application/pdf",
                "Architecture PDF content".getBytes()
        );

        DocumentResponse response = documentService.uploadDocument(
                file, "Architecture Specification", "Core API design", "TECHNICAL", null, true, 1L
        );

        assertNotNull(response);
        assertEquals("Architecture Specification", response.getTitle());
        assertEquals("https://res.cloudinary.com/handoffiq/raw/upload/v1/handoffiq/documents/spec.pdf", response.getFileUrl());
        assertEquals("handoffiq/documents/spec_123", response.getPublicId());
        assertEquals("CLOUDINARY", response.getStorageProvider());
        assertTrue(chunkSaved);
        assertNotNull(savedDoc);
        assertEquals("handoffiq/documents/spec_123", savedDoc.getCloudinaryPublicId());
    }

    @Test
    @DisplayName("Should download document content bytes")
    void testDownloadDocument() {
        byte[] bytes = documentService.downloadDocument(10L);
        assertNotNull(bytes);
        assertEquals("Mock PDF content bytes from Cloudinary", new String(bytes));
    }

    @Test
    @DisplayName("Should delete document from Cloudinary and clean up chunks and records")
    void testDeleteDocument() {
        documentService.deleteDocument(10L, 1L);
        assertTrue(chunkDeleted);
        assertTrue(storageFileDeleted);
        assertTrue(docDeleted);
    }
}
