package com.handoffiq.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Clean storage abstraction interface.
 * Decouples the application from cloud storage providers (Cloudinary / Local).
 */
public interface FileStorageService {

    /**
     * Uploads a multipart file to storage.
     *
     * @param file   the uploaded file
     * @param folder destination folder in Cloudinary (e.g., "handoffiq/documents")
     * @return metadata with secure file URL and public ID
     */
    StorageResult uploadFile(MultipartFile file, String folder);

    /**
     * Uploads raw bytes to storage.
     *
     * @param bytes            raw file bytes
     * @param originalFilename original name of the file
     * @param contentType      MIME type
     * @param folder           destination folder
     * @return metadata with secure file URL and public ID
     */
    StorageResult uploadFile(byte[] bytes, String originalFilename, String contentType, String folder);

    /**
     * Downloads file content as raw bytes.
     *
     * @param storagePathOrPublicId Cloudinary URL or publicId, or local path
     * @return raw byte content of the file
     */
    byte[] downloadFile(String storagePathOrPublicId);

    /**
     * Resolves the accessible URL for a stored file.
     *
     * @param storagePathOrPublicId Cloudinary URL or publicId
     * @return full HTTPS URL to access the file
     */
    String getFileUrl(String storagePathOrPublicId);

    /**
     * Deletes a file from storage.
     *
     * @param storagePathOrPublicId Cloudinary public_id or local file path
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteFile(String storagePathOrPublicId);

    /**
     * Returns the name of the active storage provider ("CLOUDINARY" or "LOCAL").
     */
    String getStorageType();
}
