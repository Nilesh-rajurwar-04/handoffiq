package com.handoffiq.storage;

import com.handoffiq.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Local file system storage implementation for development, offline testing,
 * and graceful fallback when cloud storage is not connected.
 */
@Service("localStorageService")
public class LocalStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final String uploadDir;

    public LocalStorageService(@Value("${handoffiq.storage.local.upload-dir:./uploads/documents}") String uploadDir) {
        this.uploadDir = uploadDir;
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            log.error("Could not create local upload directory {}: {}", uploadDir, e.getMessage());
        }
    }

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }
        try {
            return uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), folder);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file bytes: " + e.getMessage());
        }
    }

    @Override
    public StorageResult uploadFile(byte[] bytes, String originalFilename, String contentType, String folder) {
        String original = (originalFilename != null && !originalFilename.trim().isEmpty()) ? originalFilename : "document.bin";
        String extension = "";
        int dotIdx = original.lastIndexOf('.');
        if (dotIdx > 0) {
            extension = original.substring(dotIdx);
        }

        String storedFilename = UUID.randomUUID().toString() + extension;
        Path targetDir = Paths.get(uploadDir);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Path targetFile = targetDir.resolve(storedFilename);
            Files.write(targetFile, bytes);
            log.info("Stored file locally at: {}", targetFile.toAbsolutePath());

            String storagePath = uploadDir + "/" + storedFilename;
            return new StorageResult(
                    storagePath,
                    storedFilename,
                    original,
                    "LOCAL",
                    bytes.length,
                    contentType != null ? contentType : "application/octet-stream",
                    "raw"
            );
        } catch (IOException e) {
            log.error("Failed to store file on disk: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to store file on disk: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String storagePathOrPublicId) {
        Path path = Paths.get(storagePathOrPublicId);
        if (!Files.exists(path)) {
            // Try inside uploadDir
            path = Paths.get(uploadDir, storagePathOrPublicId);
        }
        if (!Files.exists(path)) {
            throw new BadRequestException("File not found on local disk: " + storagePathOrPublicId);
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read file from local disk: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String storagePathOrPublicId) {
        return "/api/documents/raw/" + storagePathOrPublicId;
    }

    @Override
    public boolean deleteFile(String storagePathOrPublicId) {
        Path path = Paths.get(storagePathOrPublicId);
        if (!Files.exists(path)) {
            path = Paths.get(uploadDir, storagePathOrPublicId);
        }
        if (Files.exists(path)) {
            try {
                return Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("Failed to delete local file {}: {}", path, e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public String getStorageType() {
        return "LOCAL";
    }
}
