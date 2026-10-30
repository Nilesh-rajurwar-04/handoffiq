package com.handoffiq.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.handoffiq.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Production Cloudinary Storage Service implementation using the official Cloudinary Java SDK.
 * Securely uploads, retrieves, and deletes documents, images, and attachments on Cloudinary CDN.
 */
@Service("cloudinaryStorageService")
public class CloudinaryStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;
    private final boolean isConfigured;

    public CloudinaryStorageService(
            @Value("${handoffiq.storage.cloudinary.cloud-name:${CLOUDINARY_CLOUD_NAME:}}") String cloudName,
            @Value("${handoffiq.storage.cloudinary.api-key:${CLOUDINARY_API_KEY:}}") String apiKey,
            @Value("${handoffiq.storage.cloudinary.api-secret:${CLOUDINARY_API_SECRET:}}") String apiSecret) {

        if (cloudName != null && !cloudName.trim().isEmpty()
                && apiKey != null && !apiKey.trim().isEmpty()
                && apiSecret != null && !apiSecret.trim().isEmpty()) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName.trim());
            config.put("api_key", apiKey.trim());
            config.put("api_secret", apiSecret.trim());
            config.put("secure", true);
            this.cloudinary = new Cloudinary(config);
            this.isConfigured = true;
            log.info("Cloudinary file storage service initialized successfully for cloud: {}", cloudName.trim());
        } else {
            this.cloudinary = null;
            this.isConfigured = false;
            log.warn("Cloudinary credentials not provided or incomplete. Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET.");
        }
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }
        try {
            return uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), folder);
        } catch (Exception e) {
            log.error("Cloudinary upload failed: {}", e.getMessage(), e);
            throw new BadRequestException("Cloudinary upload failed: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public StorageResult uploadFile(byte[] bytes, String originalFilename, String contentType, String folder) {
        if (!isConfigured) {
            throw new BadRequestException("Cloudinary storage is selected but credentials are not configured. Check CLOUDINARY_* environment variables.");
        }

        try {
            String sanitizedFolder = (folder != null && !folder.trim().isEmpty()) ? folder.trim() : "handoffiq/documents";

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", sanitizedFolder,
                    "resource_type", "auto",
                    "use_filename", true,
                    "unique_filename", true
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(bytes, uploadParams);

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            String resourceType = (String) uploadResult.get("resource_type");
            Number bytesCount = (Number) uploadResult.get("bytes");
            long size = bytesCount != null ? bytesCount.longValue() : bytes.length;

            log.info("Uploaded file to Cloudinary: publicId={}, url={}", publicId, secureUrl);

            return new StorageResult(
                    secureUrl,
                    publicId,
                    originalFilename,
                    "CLOUDINARY",
                    size,
                    contentType != null ? contentType : "application/octet-stream",
                    resourceType != null ? resourceType : "auto"
            );
        } catch (Exception e) {
            log.error("Cloudinary upload error: {}", e.getMessage(), e);
            throw new BadRequestException("Cloudinary upload error: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String storagePathOrPublicId) {
        if (storagePathOrPublicId == null || storagePathOrPublicId.trim().isEmpty()) {
            throw new BadRequestException("Storage path or public ID cannot be empty");
        }

        try {
            String downloadUrl = getFileUrl(storagePathOrPublicId);
            URL url = URI.create(downloadUrl).toURL();

            try (InputStream in = url.openStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.error("Failed to download file from Cloudinary: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to download file from Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String storagePathOrPublicId) {
        if (storagePathOrPublicId == null || storagePathOrPublicId.trim().isEmpty()) {
            return "";
        }
        if (storagePathOrPublicId.startsWith("http://") || storagePathOrPublicId.startsWith("https://")) {
            return storagePathOrPublicId;
        }
        if (!isConfigured) {
            return storagePathOrPublicId;
        }
        // Generate secure Cloudinary delivery URL
        return cloudinary.url().secure(true).generate(storagePathOrPublicId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean deleteFile(String storagePathOrPublicId) {
        if (storagePathOrPublicId == null || storagePathOrPublicId.trim().isEmpty()) {
            return false;
        }
        if (!isConfigured) {
            log.warn("Cloudinary not configured; cannot delete publicId={}", storagePathOrPublicId);
            return false;
        }

        String publicId = extractPublicId(storagePathOrPublicId);

        try {
            // First try auto/image
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String res = (String) result.get("result");

            if ("ok".equalsIgnoreCase(res)) {
                log.info("Deleted Cloudinary asset: {}", publicId);
                return true;
            }

            // If not found as image, try with resource_type: raw (for PDF, DOCX, TXT)
            result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
            res = (String) result.get("result");
            if ("ok".equalsIgnoreCase(res)) {
                log.info("Deleted Cloudinary raw asset: {}", publicId);
                return true;
            }

            log.warn("Cloudinary destroy returned result: {} for publicId: {}", res, publicId);
            return "ok".equalsIgnoreCase(res);
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary for publicId={}: {}", publicId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "CLOUDINARY";
    }

    private String extractPublicId(String input) {
        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            return input;
        }
        // Extract public ID from Cloudinary URL:
        // Format: https://res.cloudinary.com/<cloud>/[image|raw]/upload/v12345/<folder>/<public_id>.<ext>
        try {
            String path = URI.create(input).getPath();
            int uploadIdx = path.indexOf("/upload/");
            if (uploadIdx != -1) {
                String afterUpload = path.substring(uploadIdx + "/upload/".length());
                // Remove version tag if present (e.g., v17123456/)
                if (afterUpload.matches("^v[0-9]+/.+")) {
                    afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
                }
                // Strip extension
                int dotIdx = afterUpload.lastIndexOf('.');
                if (dotIdx != -1) {
                    afterUpload = afterUpload.substring(0, dotIdx);
                }
                return afterUpload;
            }
        } catch (Exception ignored) {}
        return input;
    }
}
