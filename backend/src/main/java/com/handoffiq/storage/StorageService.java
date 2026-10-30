package com.handoffiq.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Primary Storage Service that orchestrates storage operations.
 * Automatically delegates to Cloudinary when configured, or Local storage as fallback.
 * Safely handles both legacy local/S3 references and new Cloudinary assets.
 */
@Service
@Primary
public class StorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final CloudinaryStorageService cloudinaryStorageService;
    private final LocalStorageService localStorageService;

    @Value("${handoffiq.storage.type:${STORAGE_TYPE:cloudinary}}")
    private String preferredStorageType;

    public StorageService(CloudinaryStorageService cloudinaryStorageService, LocalStorageService localStorageService) {
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.localStorageService = localStorageService;
    }

    private FileStorageService getActiveStorage() {
        if ("cloudinary".equalsIgnoreCase(preferredStorageType)) {
            if (cloudinaryStorageService.isConfigured()) {
                return cloudinaryStorageService;
            } else {
                log.warn("Cloudinary is configured as primary storage type, but API credentials are missing. Falling back to local storage.");
                return localStorageService;
            }
        }
        return localStorageService;
    }

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        return getActiveStorage().uploadFile(file, folder);
    }

    @Override
    public StorageResult uploadFile(byte[] bytes, String originalFilename, String contentType, String folder) {
        return getActiveStorage().uploadFile(bytes, originalFilename, contentType, folder);
    }

    @Override
    public byte[] downloadFile(String storagePathOrPublicId) {
        if (storagePathOrPublicId == null || storagePathOrPublicId.trim().isEmpty()) {
            return new byte[0];
        }
        // If it's a Cloudinary URL or Cloudinary is active, use Cloudinary
        if (storagePathOrPublicId.startsWith("http://") || storagePathOrPublicId.startsWith("https://") 
                || (cloudinaryStorageService.isConfigured() && !storagePathOrPublicId.contains("/"))) {
            return cloudinaryStorageService.downloadFile(storagePathOrPublicId);
        }
        return localStorageService.downloadFile(storagePathOrPublicId);
    }

    @Override
    public String getFileUrl(String storagePathOrPublicId) {
        if (storagePathOrPublicId == null || storagePathOrPublicId.trim().isEmpty()) {
            return "";
        }
        if (storagePathOrPublicId.startsWith("http://") || storagePathOrPublicId.startsWith("https://")) {
            return storagePathOrPublicId;
        }
        if (cloudinaryStorageService.isConfigured()) {
            return cloudinaryStorageService.getFileUrl(storagePathOrPublicId);
        }
        return localStorageService.getFileUrl(storagePathOrPublicId);
    }

    @Override
    public boolean deleteFile(String storagePathOrPublicId) {
        if (storagePathOrPublicId == null || storagePathOrPublicId.trim().isEmpty()) {
            return false;
        }
        if (storagePathOrPublicId.startsWith("http://") || storagePathOrPublicId.startsWith("https://")
                || (cloudinaryStorageService.isConfigured() && !storagePathOrPublicId.contains("/"))) {
            return cloudinaryStorageService.deleteFile(storagePathOrPublicId);
        }
        return localStorageService.deleteFile(storagePathOrPublicId);
    }

    @Override
    public String getStorageType() {
        return getActiveStorage().getStorageType();
    }
}
