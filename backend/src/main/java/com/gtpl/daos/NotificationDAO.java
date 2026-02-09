package com.gtpl.daos;

import com.gtpl.models.Notification;
import com.gtpl.utils.DatabaseConfig;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Notification DAO Class
 * Data access operations for Notification entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class NotificationDAO extends BaseDAO<Notification, Long> {
    
    @Override
    protected String getTableName() {
        return "notifications";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected Notification mapResultSet(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getLong("id"));
        
        String recipientTypeStr = rs.getString("recipient_type");
        if (recipientTypeStr != null) {
            notification.setRecipientType(Notification.RecipientType.valueOf(recipientTypeStr));
        }
        
        notification.setRecipientId(rs.getLong("recipient_id"));
        
        String senderTypeStr = rs.getString("sender_type");
        if (senderTypeStr != null) {
            notification.setSenderType(Notification.SenderType.valueOf(senderTypeStr));
        }
        
        notification.setSenderId(getLongOrNull(rs, "sender_id"));
        
        String notificationTypeStr = rs.getString("notification_type");
        if (notificationTypeStr != null) {
            notification.setNotificationType(Notification.NotificationType.valueOf(notificationTypeStr));
        }
        
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        
        String relatedEntityTypeStr = rs.getString("related_entity_type");
        if (relatedEntityTypeStr != null) {
            notification.setRelatedEntityType(Notification.RelatedEntityType.valueOf(relatedEntityTypeStr));
        }
        
        notification.setRelatedEntityId(getLongOrNull(rs, "related_entity_id"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setReadAt(getLocalDateTimeOrNull(rs, "read_at"));
        
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            notification.setPriority(Notification.Priority.valueOf(priorityStr));
        }
        
        notification.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        notification.setExpiresAt(getLocalDateTimeOrNull(rs, "expires_at"));
        return notification;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Notification notification) throws SQLException {
        stmt.setString(1, notification.getRecipientType() != null ? notification.getRecipientType().name() : "VENDOR");
        stmt.setLong(2, notification.getRecipientId());
        stmt.setString(3, notification.getSenderType() != null ? notification.getSenderType().name() : "SYSTEM");
        stmt.setObject(4, notification.getSenderId());
        stmt.setString(5, notification.getNotificationType() != null ? notification.getNotificationType().name() : "SYSTEM_ALERT");
        stmt.setString(6, notification.getTitle());
        stmt.setString(7, notification.getMessage());
        stmt.setString(8, notification.getRelatedEntityType() != null ? notification.getRelatedEntityType().name() : null);
        stmt.setObject(9, notification.getRelatedEntityId());
        stmt.setString(10, notification.getPriority() != null ? notification.getPriority().name() : "MEDIUM");
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Notification notification) throws SQLException {
        stmt.setBoolean(1, notification.isRead());
        stmt.setTimestamp(2, notification.getReadAt() != null ? Timestamp.valueOf(notification.getReadAt()) : null);
        stmt.setLong(3, notification.getId());
    }
    
    /**
     * Creates a new notification.
     * 
     * @param notification the notification to create
     * @return the created notification ID
     */
    public Long create(Notification notification) {
        String sql = "INSERT INTO notifications (recipient_type, recipient_id, sender_type, sender_id, " +
                     "notification_type, title, message, related_entity_type, related_entity_id, " +
                     "is_read, priority, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, notification);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating notification", e);
        }
        
        return null;
    }
    
    /**
     * Finds notifications by recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return list of notifications
     */
    public List<Notification> findByRecipient(Notification.RecipientType recipientType, Long recipientId) {
        String sql = "SELECT n.*, " +
                     "CASE WHEN n.sender_type = 'ADMIN' THEN a.full_name " +
                     "     WHEN n.sender_type = 'VENDOR' THEN v.vendor_name " +
                     "     ELSE 'System' END as sender_name " +
                     "FROM notifications n " +
                     "LEFT JOIN admins a ON n.sender_type = 'ADMIN' AND n.sender_id = a.id " +
                     "LEFT JOIN vendors v ON n.sender_type = 'VENDOR' AND n.sender_id = v.id " +
                     "WHERE n.recipient_type = ? AND n.recipient_id = ? " +
                     "AND (n.expires_at IS NULL OR n.expires_at > CURRENT_TIMESTAMP) " +
                     "ORDER BY n.created_at DESC";
        return executeQueryWithJoins(sql, recipientType.name(), recipientId);
    }
    
    /**
     * Finds unread notifications by recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return list of unread notifications
     */
    public List<Notification> findUnreadByRecipient(Notification.RecipientType recipientType, Long recipientId) {
        String sql = "SELECT n.*, " +
                     "CASE WHEN n.sender_type = 'ADMIN' THEN a.full_name " +
                     "     WHEN n.sender_type = 'VENDOR' THEN v.vendor_name " +
                     "     ELSE 'System' END as sender_name " +
                     "FROM notifications n " +
                     "LEFT JOIN admins a ON n.sender_type = 'ADMIN' AND n.sender_id = a.id " +
                     "LEFT JOIN vendors v ON n.sender_type = 'VENDOR' AND n.sender_id = v.id " +
                     "WHERE n.recipient_type = ? AND n.recipient_id = ? AND n.is_read = FALSE " +
                     "AND (n.expires_at IS NULL OR n.expires_at > CURRENT_TIMESTAMP) " +
                     "ORDER BY n.priority DESC, n.created_at DESC";
        return executeQueryWithJoins(sql, recipientType.name(), recipientId);
    }
    
    /**
     * Counts unread notifications for a recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return count of unread notifications
     */
    public int countUnread(Notification.RecipientType recipientType, Long recipientId) {
        String sql = "SELECT COUNT(*) FROM notifications " +
                     "WHERE recipient_type = ? AND recipient_id = ? AND is_read = FALSE " +
                     "AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recipientType.name());
            stmt.setLong(2, recipientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread notifications", e);
        }
        return 0;
    }
    
    /**
     * Marks a notification as read.
     * 
     * @param notificationId the notification ID
     * @return true if marked
     */
    public boolean markAsRead(Long notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE, read_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, notificationId) > 0;
    }
    
    /**
     * Marks all notifications as read for a recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return number of notifications marked
     */
    public int markAllAsRead(Notification.RecipientType recipientType, Long recipientId) {
        String sql = "UPDATE notifications SET is_read = TRUE, read_at = CURRENT_TIMESTAMP " +
                     "WHERE recipient_type = ? AND recipient_id = ? AND is_read = FALSE";
        return executeUpdate(sql, recipientType.name(), recipientId);
    }
    
    /**
     * Deletes expired notifications.
     * 
     * @return number of deleted notifications
     */
    public int deleteExpired() {
        String sql = "DELETE FROM notifications WHERE expires_at IS NOT NULL AND expires_at < CURRENT_TIMESTAMP";
        return executeUpdate(sql);
    }
    
    /**
     * Deletes old notifications.
     * 
     * @param days number of days to keep
     * @return number of deleted notifications
     */
    public int deleteOld(int days) {
        String sql = "DELETE FROM notifications WHERE created_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? DAY)";
        return executeUpdate(sql, days);
    }
    
    /**
     * Executes a query with joins and maps results.
     */
    private List<Notification> executeQueryWithJoins(String sql, Object... params) {
        List<Notification> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification notification = mapResultSet(rs);
                    notification.setSenderName(rs.getString("sender_name"));
                    results.add(notification);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query with joins", e);
        }
        
        return results;
    }
}
