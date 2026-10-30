package com.handoffiq.storage;

/**
 * Result metadata produced when a file or image is uploaded to cloud or local storage.
 */
public class StorageResult {

    private String fileUrl;         // Cloudinary secure_url or relative local URL
    private String publicId;        // Cloudinary public_id or unique local file name
    private String originalName;
    private String storageProvider; // "CLOUDINARY" or "LOCAL"
    private long fileSize;
    private String contentType;
    private String resourceType;    // "image", "raw", "video", etc.

    public StorageResult() {}

    public StorageResult(String fileUrl, String publicId, String originalName, String storageProvider, long fileSize, String contentType, String resourceType) {
        this.fileUrl = fileUrl;
        this.publicId = publicId;
        this.originalName = originalName;
        this.storageProvider = storageProvider;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.resourceType = resourceType;
    }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
}
