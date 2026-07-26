package com.rooms.chat_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "messages")
public class ChatMessage {

    @Id
    private String id;
    
    private UUID roomId;
    private UUID authorId;
    private String authorUsername;
    private String content;
    
    // e.g. TEXT, FILE_REF
    private String type;
    
    // Only populated if type is FILE_REF
    private String attachmentId;
    
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private LocalDateTime deletedAt;

    public ChatMessage() {
    }

    public ChatMessage(UUID roomId, UUID authorId, String authorUsername, String content, String type, String attachmentId) {
        this.roomId = roomId;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.type = type;
        this.attachmentId = attachmentId;
        this.sentAt = LocalDateTime.now();
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
    public UUID getAuthorId() {
        return authorId;
    }
    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }
    public String getAuthorUsername() {
        return authorUsername;
    }
    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getAttachmentId() {
        return attachmentId;
    }
    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    public LocalDateTime getEditedAt() {
        return editedAt;
    }
    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
