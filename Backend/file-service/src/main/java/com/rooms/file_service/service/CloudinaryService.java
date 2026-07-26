package com.rooms.file_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public Map uploadFile(MultipartFile file, String folderPath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String resourceType = determineResourceType(file.getContentType(), originalFilename);

        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", folderPath,
                "use_filename", true,
                "unique_filename", true
        ));
    }

    public void deleteFile(String publicId) throws IOException {
        // We don't know the exact resource type at deletion time if we just pass publicId, 
        // but typically images and raw are handled. A more robust way is to store and pass resourceType.
        // For now, we will try with default 'image' and if it fails, try 'raw' or 'video'.
        // Better: store resource_type in DB and pass it here. We will pass it here.
    }
    
    public void deleteFile(String publicId, String resourceType) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
    }

    public static String determineResourceType(String contentType, String filename) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) return "image";
            if (contentType.startsWith("video/")) return "video";
            if (contentType.startsWith("audio/")) return "video"; // Cloudinary treats audio as video resource type
        }
        
        // Fallback based on extension if content type is missing or generic application/octet-stream
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".svg")) {
                return "image";
            }
            if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mov") || lower.endsWith(".mp3") || lower.endsWith(".wav")) {
                return "video";
            }
        }
        
        return "raw";
    }
}
