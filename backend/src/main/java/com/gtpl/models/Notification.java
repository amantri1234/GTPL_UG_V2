package com.gtpl.models;

import java.time.LocalDateTime;

/**
 * Notification Model Class
 * Represents an in-app notification for users.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class Notification {
    
    // Enum for recipient type
    public enum RecipientType {
        ADMIN, VENDOR
    }
    
    // Enum for sender type
    public enum SenderType {
        ADMIN, VENDOR, SYSTEM
    }
    
    // Enum for notification type
    public enum NotificationType {
        DAILY_UPDATE_MISSED,
        PROJECT_ASSIGNED,
        PROJECT_DELAYED,
        MATERIAL_REQUEST_STATUS,
        MATERIAL_APPROVED,
        MATERIAL_REJECTED,
        PROGRESS_UPDATE,
        SYSTEM_ALERT,
        DEADLINE_REMINDER
    }
    
    // Enum for related entity type
    public enum RelatedEntityType {
        PROJECT, WORK_ASSIGNMENT, MATERIAL_REQUEST, DAILY_PROGRESS
    }
    
    // Enum for priority
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    private Long id;
    private RecipientType recipientType;
    private Long recipientId;
    private SenderType senderType;
    private Long senderId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private RelatedEntityType relatedEntityType;
    private Long relatedEntityId;
    private boolean isRead;
    private LocalDateTime readAt;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // Transient fields
    private String senderName;
    private String timeAgo;
    
    // Default constructor
    public Notification() {
        this.senderType = SenderType.SYSTEM;
        this.isRead = false;
        this.priority = Priority.MEDIUM;
    }
    
    // Constructor for creating new notification
    public Notification(RecipientType recipientType, Long recipientId,
                        NotificationType notificationType, String title, String message) {
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.senderType = SenderType.SYSTEM;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.priority = Priority.MEDIUM;
    }
    
    // Full constructor
    public Notification(Long id, RecipientType recipientType, Long recipientId,
                        SenderType senderType, Long senderId, NotificationType notificationType,
                        String title, String message, RelatedEntityType relatedEntityType,
                        Long relatedEntityId, boolean isRead, LocalDateTime readAt,
                        Priority priority, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.isRead = isRead;
        this.readAt = readAt;
        this.priority = priority;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RecipientType getRecipientType() {
        return recipientType;
    }
    
    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }
    
    public Long getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
    
    public SenderType getSenderType() {
        return senderType;
    }
    
    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public RelatedEntityType getRelatedEntityType() {
        return relatedEntityType;
    }
    
    public void setRelatedEntityType(RelatedEntityType relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }
    
    public Long getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    // Transient field getters/setters
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getTimeAgo() {
        return timeAgo;
    }
    
    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
    
    /**
     * Marks the notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the notification has expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Gets priority CSS class for UI display
     */
    public String getPriorityClass() {
        if (priority == null) return "secondary";
        switch (priority) {
            case CRITICAL: return "danger";
            case HIGH: return "warning";
            case MEDIUM: return "info";
            case LOW: return "success";
            default: return "secondary";
        }
    }
    
    /**
     * Gets icon class based on notification type
     */
    public String getIconClass() {
        if (notificationType == null) return "bell";
        switch (notificationType) {
            case DAILY_UPDATE_MISSED: return "exclamation-triangle";
            case PROJECT_ASSIGNED: return "clipboard-check";
            case PROJECT_DELAYED: return "clock";
            case MATERIAL_REQUEST_STATUS: return "box";
            case MATERIAL_APPROVED: return "check-circle";
            case MATERIAL_REJECTED: return "times-circle";
            case PROGRESS_UPDATE: return "chart-line";
            case SYSTEM_ALERT: return "exclamation-circle";
            case DEADLINE_REMINDER: return "calendar-alt";
            default: return "bell";
        }
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipientType=" + recipientType +
                ", recipientId=" + recipientId +
                ", notificationType=" + notificationType +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", priority=" + priority +
                '}';
    }
}
