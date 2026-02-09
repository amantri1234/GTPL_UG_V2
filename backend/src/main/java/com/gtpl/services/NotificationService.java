package com.gtpl.services;

import com.gtpl.daos.AdminDAO;
import com.gtpl.daos.NotificationDAO;
import com.gtpl.models.Notification;

import java.util.List;
import java.util.Optional;

/**
 * Notification Service Class
 * Handles notification creation, retrieval, and management.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class NotificationService {
    
    private final NotificationDAO notificationDAO;
    private final AdminDAO adminDAO;
    
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
        this.adminDAO = new AdminDAO();
    }
    
    /**
     * Creates a notification.
     * 
     * @param notification the notification to create
     * @return the created notification ID
     */
    public Long createNotification(Notification notification) {
        return notificationDAO.create(notification);
    }
    
    /**
     * Creates a notification for a vendor.
     * 
     * @param vendorId the vendor ID
     * @param type the notification type
     * @param title the title
     * @param message the message
     * @return the created notification ID
     */
    public Long notifyVendor(Long vendorId, Notification.NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setRecipientType(Notification.RecipientType.VENDOR);
        notification.setRecipientId(vendorId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        return notificationDAO.create(notification);
    }
    
    /**
     * Creates a notification for an admin.
     * 
     * @param adminId the admin ID
     * @param type the notification type
     * @param title the title
     * @param message the message
     * @return the created notification ID
     */
    public Long notifyAdmin(Long adminId, Notification.NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setRecipientType(Notification.RecipientType.ADMIN);
        notification.setRecipientId(adminId);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        return notificationDAO.create(notification);
    }
    
    /**
     * Notifies all admins.
     * 
     * @param type the notification type
     * @param title the title
     * @param message the message
     */
    public void notifyAllAdmins(Notification.NotificationType type, String title, String message) {
        // Get all admin IDs and notify each
        adminDAO.findAll().forEach(admin -> {
            if (admin.isActive()) {
                notifyAdmin(admin.getId(), type, title, message);
            }
        });
    }
    
    /**
     * Notifies about a new vendor registration.
     * 
     * @param vendorId the vendor ID
     * @param vendorName the vendor name
     * @param companyName the company name
     */
    public void notifyNewVendorRegistration(Long vendorId, String vendorName, String companyName) {
        notifyAllAdmins(
            Notification.NotificationType.SYSTEM_ALERT,
            "New Vendor Registration",
            String.format("%s from %s has registered as a new vendor.", vendorName, companyName)
        );
    }
    
    /**
     * Notifies about a project assignment.
     * 
     * @param vendorId the vendor ID
     * @param projectName the project name
     */
    public void notifyProjectAssigned(Long vendorId, String projectName) {
        notifyVendor(
            vendorId,
            Notification.NotificationType.PROJECT_ASSIGNED,
            "New Project Assigned",
            String.format("You have been assigned to project: %s", projectName)
        );
    }
    
    /**
     * Notifies about a missed daily update.
     * 
     * @param vendorId the vendor ID
     * @param projectName the project name
     * @param date the date missed
     */
    public void notifyMissedDailyUpdate(Long vendorId, String projectName, String date) {
        notifyVendor(
            vendorId,
            Notification.NotificationType.DAILY_UPDATE_MISSED,
            "Daily Update Missed",
            String.format("You missed the daily update for project '%s' on %s. Please submit it as soon as possible.", 
                projectName, date)
        );
    }
    
    /**
     * Notifies about material request approval.
     * 
     * @param vendorId the vendor ID
     * @param requestCode the request code
     * @param materialName the material name
     */
    public void notifyMaterialRequestApproved(Long vendorId, String requestCode, String materialName) {
        notifyVendor(
            vendorId,
            Notification.NotificationType.MATERIAL_APPROVED,
            "Material Request Approved",
            String.format("Your material request %s for %s has been approved.", requestCode, materialName)
        );
    }
    
    /**
     * Notifies about material request rejection.
     * 
     * @param vendorId the vendor ID
     * @param requestCode the request code
     * @param materialName the material name
     * @param reason the rejection reason
     */
    public void notifyMaterialRequestRejected(Long vendorId, String requestCode, String materialName, String reason) {
        notifyVendor(
            vendorId,
            Notification.NotificationType.MATERIAL_REJECTED,
            "Material Request Rejected",
            String.format("Your material request %s for %s has been rejected. Reason: %s", 
                requestCode, materialName, reason)
        );
    }
    
    /**
     * Notifies about project delay.
     * 
     * @param vendorId the vendor ID
     * @param projectName the project name
     * @param daysDelayed number of days delayed
     */
    public void notifyProjectDelayed(Long vendorId, String projectName, int daysDelayed) {
        notifyVendor(
            vendorId,
            Notification.NotificationType.PROJECT_DELAYED,
            "Project Delay Alert",
            String.format("Project '%s' is delayed by %d day(s). Please expedite the work.", 
                projectName, daysDelayed)
        );
    }
    
    /**
     * Notifies about upcoming deadline.
     * 
     * @param vendorId the vendor ID
     * @param projectName the project name
     * @param daysRemaining number of days remaining
     */
    public void notifyDeadlineReminder(Long vendorId, String projectName, int daysRemaining) {
        notifyVendor(
            vendorId,
            Notification.NotificationType.DEADLINE_REMINDER,
            "Project Deadline Reminder",
            String.format("Project '%s' deadline is in %d day(s). Please ensure timely completion.", 
                projectName, daysRemaining)
        );
    }
    
    /**
     * Gets notifications for a recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return list of notifications
     */
    public List<Notification> getNotifications(Notification.RecipientType recipientType, Long recipientId) {
        return notificationDAO.findByRecipient(recipientType, recipientId);
    }
    
    /**
     * Gets unread notifications for a recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return list of unread notifications
     */
    public List<Notification> getUnreadNotifications(Notification.RecipientType recipientType, Long recipientId) {
        return notificationDAO.findUnreadByRecipient(recipientType, recipientId);
    }
    
    /**
     * Counts unread notifications for a recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return count of unread notifications
     */
    public int countUnreadNotifications(Notification.RecipientType recipientType, Long recipientId) {
        return notificationDAO.countUnread(recipientType, recipientId);
    }
    
    /**
     * Marks a notification as read.
     * 
     * @param notificationId the notification ID
     * @return true if marked
     */
    public boolean markAsRead(Long notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }
    
    /**
     * Marks all notifications as read for a recipient.
     * 
     * @param recipientType the recipient type
     * @param recipientId the recipient ID
     * @return number of notifications marked
     */
    public int markAllAsRead(Notification.RecipientType recipientType, Long recipientId) {
        return notificationDAO.markAllAsRead(recipientType, recipientId);
    }
    
    /**
     * Gets a notification by ID.
     * 
     * @param notificationId the notification ID
     * @return Optional containing the notification
     */
    public Optional<Notification> getNotification(Long notificationId) {
        return notificationDAO.findById(notificationId);
    }
    
    /**
     * Cleans up old and expired notifications.
     * 
     * @param retentionDays number of days to retain notifications
     * @return number of deleted notifications
     */
    public int cleanupNotifications(int retentionDays) {
        int deleted = notificationDAO.deleteExpired();
        deleted += notificationDAO.deleteOld(retentionDays);
        return deleted;
    }
}
