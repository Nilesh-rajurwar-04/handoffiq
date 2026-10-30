package com.handoffiq.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTest {

    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService("target/test-uploads");
    }

    @Test
    void testLocalStorageUploadAndRetrieve() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handover_spec.pdf",
                "application/pdf",
                "Sample Handover PDF Content".getBytes()
        );

        StorageResult result = localStorageService.uploadFile(file, "test_folder");

        assertNotNull(result);
        assertNotNull(result.getFileUrl());
        assertNotNull(result.getPublicId());
        assertEquals("LOCAL", result.getStorageProvider());
        assertEquals("handover_spec.pdf", result.getOriginalName());
        assertTrue(result.getFileSize() > 0);

        byte[] bytes = localStorageService.downloadFile(result.getPublicId());
        assertNotNull(bytes);
        assertEquals("Sample Handover PDF Content", new String(bytes));

        boolean deleted = localStorageService.deleteFile(result.getPublicId());
        assertTrue(deleted);
    }

    @Test
    void testStorageResult() {
        StorageResult result = new StorageResult(
                "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                "sample_123",
                "sample.jpg",
                "CLOUDINARY",
                1024L,
                "image/jpeg",
                "image"
        );

        assertEquals("https://res.cloudinary.com/demo/image/upload/sample.jpg", result.getFileUrl());
        assertEquals("sample_123", result.getPublicId());
        assertEquals("CLOUDINARY", result.getStorageProvider());
        assertEquals("sample.jpg", result.getOriginalName());
        assertEquals(1024L, result.getFileSize());
        assertEquals("image/jpeg", result.getContentType());
        assertEquals("image", result.getResourceType());
    }
}
