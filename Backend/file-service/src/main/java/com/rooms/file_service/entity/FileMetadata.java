package com.rooms.file_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "files")
public class FileMetadata {

    @Id
    private String id;
    
    private UUID roomId;
    private UUID uploadedBy;
    private String originalName;
    private String mimeType;
    private long size;
    
    private String cloudinaryPublicId;
    private String cloudinarySecureUrl;
    private String resourceType; // image, video, raw
    
    private LocalDateTime uploadedAt;
    private LocalDateTime deletedAt;

    public FileMetadata() {
    }

    public FileMetadata(UUID roomId, UUID uploadedBy, String originalName, String mimeType, long size, 
                        String cloudinaryPublicId, String cloudinarySecureUrl, String resourceType) {
        this.roomId = roomId;
        this.uploadedBy = uploadedBy;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.cloudinarySecureUrl = cloudinarySecureUrl;
        this.resourceType = resourceType;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public UUID getRoomId() {
        return roomId;
    }
    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }
    public UUID getUploadedBy() {
        return uploadedBy;
    }
    public void setUploadedBy(UUID uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    public String getOriginalName() {
        return originalName;
    }
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }
    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }
    public String getCloudinarySecureUrl() {
        return cloudinarySecureUrl;
    }
    public void setCloudinarySecureUrl(String cloudinarySecureUrl) {
        this.cloudinarySecureUrl = cloudinarySecureUrl;
    }
    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
