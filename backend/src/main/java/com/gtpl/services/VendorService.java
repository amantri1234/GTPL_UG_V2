package com.gtpl.services;

import com.gtpl.daos.*;
import com.gtpl.models.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Vendor Service Class
 * Handles vendor-related business logic and operations.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class VendorService {
    
    private final VendorDAO vendorDAO;
    private final ProjectDAO projectDAO;
    private final DailyProgressDAO dailyProgressDAO;
    private final MaterialDAO materialDAO;
    private final MaterialRequestDAO materialRequestDAO;
    private final NotificationService notificationService;
    
    public VendorService() {
        this.vendorDAO = new VendorDAO();
        this.projectDAO = new ProjectDAO();
        this.dailyProgressDAO = new DailyProgressDAO();
        this.materialDAO = new MaterialDAO();
        this.materialRequestDAO = new MaterialRequestDAO();
        this.notificationService = new NotificationService();
    }
    
    // ==================== Dashboard ====================
    
    /**
     * Gets vendor dashboard data.
     * 
     * @param vendorId the vendor ID
     * @return map containing dashboard statistics
     */
    public Map<String, Object> getDashboardData(Long vendorId) {
        Map<String, Object> data = new HashMap<>();
        
        // Get vendor info
        Optional<Vendor> vendorOpt = vendorDAO.findById(vendorId);
        if (vendorOpt.isPresent()) {
            data.put("vendor", vendorOpt.get().toSafeCopy());
        }
        
        // Get assigned projects
        List<Project> projects = projectDAO.findByVendorId(vendorId);
        data.put("projects", projects);
        
        // Project statistics
        int totalProjects = projects.size();
        int activeProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == Project.Status.IN_PROGRESS)
                .count();
        int completedProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == Project.Status.COMPLETED)
                .count();
        
        data.put("totalProjects", totalProjects);
        data.put("activeProjects", activeProjects);
        data.put("completedProjects", completedProjects);
        
        // KM statistics
        BigDecimal totalKmAssigned = projects.stream()
                .map(Project::getTotalKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalKmCompleted = projects.stream()
                .map(Project::getCompletedKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalKmRemaining = totalKmAssigned.subtract(totalKmCompleted);
        
        data.put("totalKmAssigned", totalKmAssigned);
        data.put("totalKmCompleted", totalKmCompleted);
        data.put("totalKmRemaining", totalKmRemaining);
        
        // Progress percentage
        BigDecimal overallProgress = totalKmAssigned.compareTo(BigDecimal.ZERO) > 0
                ? totalKmCompleted.multiply(new BigDecimal("100")).divide(totalKmAssigned, 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        data.put("overallProgress", overallProgress);
        
        // Material requests
        List<MaterialRequest> materialRequests = materialRequestDAO.findByVendorId(vendorId);
        int pendingRequests = (int) materialRequests.stream()
                .filter(r -> r.getStatus() == MaterialRequest.Status.PENDING)
                .count();
        data.put("totalMaterialRequests", materialRequests.size());
        data.put("pendingMaterialRequests", pendingRequests);
        
        // Unread notifications
        int unreadNotifications = notificationService.countUnreadNotifications(
            Notification.RecipientType.VENDOR, vendorId
        );
        data.put("unreadNotifications", unreadNotifications);
        
        // Recent daily progress
        List<DailyProgress> recentProgress = dailyProgressDAO.findByVendorId(vendorId);
        data.put("recentDailyProgress", recentProgress.size() > 5 ? recentProgress.subList(0, 5) : recentProgress);
        
        // Today's work status
        LocalDate today = LocalDate.now();
        boolean hasSubmittedToday = false;
        for (Project project : projects) {
            if (project.getStatus() == Project.Status.IN_PROGRESS) {
                if (dailyProgressDAO.existsForProjectAndDate(project.getId(), today)) {
                    hasSubmittedToday = true;
                    break;
                }
            }
        }
        data.put("hasSubmittedToday", hasSubmittedToday);
        
        // Projects requiring daily update
        List<Project> projectsNeedingUpdate = new ArrayList<>();
        for (Project project : projects) {
            if (project.getStatus() == Project.Status.IN_PROGRESS) {
                if (!dailyProgressDAO.existsForProjectAndDate(project.getId(), today)) {
                    projectsNeedingUpdate.add(project);
                }
            }
        }
        data.put("projectsNeedingUpdate", projectsNeedingUpdate);
        
        return data;
    }
    
    // ==================== Projects ====================
    
    /**
     * Gets all projects assigned to a vendor.
     * 
     * @param vendorId the vendor ID
     * @return list of projects
     */
    public List<Project> getMyProjects(Long vendorId) {
        return projectDAO.findByVendorId(vendorId);
    }
    
    /**
     * Gets active projects for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return list of active projects
     */
    public List<Project> getActiveProjects(Long vendorId) {
        return projectDAO.findActiveByVendorId(vendorId);
    }
    
    /**
     * Gets project details if assigned to vendor.
     * 
     * @param projectId the project ID
     * @param vendorId the vendor ID
     * @return Optional containing the project
     */
    public Optional<Project> getProjectDetails(Long projectId, Long vendorId) {
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (vendorId.equals(project.getAssignedVendorId())) {
                return projectOpt;
            }
        }
        return Optional.empty();
    }
    
    // ==================== Daily Reporting ====================
    
    /**
     * Submits a daily progress report.
     * 
     * @param projectId the project ID
     * @param vendorId the vendor ID
     * @param workDate the work date
     * @param fromPoint the from point
     * @param toPoint the to point
     * @param kmCompletedToday the KM completed today
     * @param remainingEstimatedWork the remaining estimated work
     * @param workDescription the work description
     * @param remarks the remarks
     * @return the created report ID
     */
    public Long submitDailyReport(Long projectId, Long vendorId, LocalDate workDate,
                                   String fromPoint, String toPoint, BigDecimal kmCompletedToday,
                                   BigDecimal remainingEstimatedWork, String workDescription,
                                   String remarks) {
        // Verify project is assigned to vendor
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new IllegalArgumentException("Project not found");
        }
        
        Project project = projectOpt.get();
        if (!vendorId.equals(project.getAssignedVendorId())) {
            throw new SecurityException("Project not assigned to this vendor");
        }
        
        // Check if project is active
        if (project.getStatus() != Project.Status.ASSIGNED && 
            project.getStatus() != Project.Status.IN_PROGRESS) {
            throw new IllegalArgumentException("Project is not active");
        }
        
        // Check if report already exists for this date
        if (dailyProgressDAO.existsForProjectAndDate(projectId, workDate)) {
            throw new IllegalArgumentException("Daily report already submitted for this date");
        }
        
        // Validate KM
        if (kmCompletedToday == null || kmCompletedToday.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("KM completed must be non-negative");
        }
        
        // Check if KM would exceed total
        BigDecimal newTotal = project.getCompletedKm().add(kmCompletedToday);
        if (newTotal.compareTo(project.getTotalKm()) > 0) {
            throw new IllegalArgumentException("KM would exceed project total");
        }
        
        // Create daily progress
        DailyProgress progress = new DailyProgress();
        progress.setProjectId(projectId);
        progress.setVendorId(vendorId);
        progress.setWorkDate(workDate);
        progress.setFromPoint(fromPoint);
        progress.setToPoint(toPoint);
        progress.setKmCompletedToday(kmCompletedToday);
        progress.setRemainingEstimatedWork(remainingEstimatedWork);
        progress.setWorkDescription(workDescription);
        progress.setRemarks(remarks);
        
        Long progressId = dailyProgressDAO.create(progress);
        
        // Update project status to IN_PROGRESS if it's the first report
        if (project.getStatus() == Project.Status.ASSIGNED) {
            projectDAO.updateStatus(projectId, Project.Status.IN_PROGRESS);
        }
        
        return progressId;
    }
    
    /**
     * Gets daily progress history for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return list of daily progress records
     */
    public List<DailyProgress> getDailyProgressHistory(Long vendorId) {
        return dailyProgressDAO.findByVendorId(vendorId);
    }
    
    /**
     * Gets daily progress for a specific project.
     * 
     * @param projectId the project ID
     * @param vendorId the vendor ID
     * @return list of daily progress records
     */
    public List<DailyProgress> getProjectDailyProgress(Long projectId, Long vendorId) {
        // Verify project is assigned to vendor
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        if (projectOpt.isPresent() && vendorId.equals(projectOpt.get().getAssignedVendorId())) {
            return dailyProgressDAO.findByProjectId(projectId);
        }
        return new ArrayList<>();
    }
    
    // ==================== Material Requests ====================
    
    /**
     * Gets material requests for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return list of material requests
     */
    public List<MaterialRequest> getMyMaterialRequests(Long vendorId) {
        return materialRequestDAO.findByVendorId(vendorId);
    }
    
    /**
     * Creates a material request.
     * 
     * @param projectId the project ID
     * @param vendorId the vendor ID
     * @param materialId the material ID
     * @param quantityRequired the quantity required
     * @param reason the reason
     * @param urgency the urgency
     * @return the created request ID
     */
    public Long createMaterialRequest(Long projectId, Long vendorId, Long materialId,
                                      BigDecimal quantityRequired, String reason,
                                      MaterialRequest.Urgency urgency) {
        // Verify project is assigned to vendor
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        if (!projectOpt.isPresent()) {
            throw new IllegalArgumentException("Project not found");
        }
        
        if (!vendorId.equals(projectOpt.get().getAssignedVendorId())) {
            throw new SecurityException("Project not assigned to this vendor");
        }
        
        // Verify material exists
        Optional<Material> materialOpt = materialDAO.findById(materialId);
        if (!materialOpt.isPresent()) {
            throw new IllegalArgumentException("Material not found");
        }
        
        // Validate quantity
        if (quantityRequired == null || quantityRequired.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        MaterialRequest request = new MaterialRequest();
        request.setProjectId(projectId);
        request.setVendorId(vendorId);
        request.setMaterialId(materialId);
        request.setQuantityRequired(quantityRequired);
        request.setReason(reason);
        request.setUrgency(urgency != null ? urgency : MaterialRequest.Urgency.MEDIUM);
        
        return materialRequestDAO.create(request);
    }
    
    /**
     * Gets available materials for requests.
     * 
     * @return list of active materials
     */
    public List<Material> getAvailableMaterials() {
        return materialDAO.findAllActive();
    }
    
    // ==================== Notifications ====================
    
    /**
     * Gets notifications for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return list of notifications
     */
    public List<Notification> getNotifications(Long vendorId) {
        return notificationService.getNotifications(Notification.RecipientType.VENDOR, vendorId);
    }
    
    /**
     * Gets unread notifications for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return list of unread notifications
     */
    public List<Notification> getUnreadNotifications(Long vendorId) {
        return notificationService.getUnreadNotifications(Notification.RecipientType.VENDOR, vendorId);
    }
    
    /**
     * Marks a notification as read.
     * 
     * @param notificationId the notification ID
     * @param vendorId the vendor ID
     * @return true if marked
     */
    public boolean markNotificationAsRead(Long notificationId, Long vendorId) {
        Optional<Notification> notificationOpt = notificationService.getNotification(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getRecipientType() == Notification.RecipientType.VENDOR &&
                vendorId.equals(notification.getRecipientId())) {
                return notificationService.markAsRead(notificationId);
            }
        }
        return false;
    }
    
    /**
     * Marks all notifications as read for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return number of notifications marked
     */
    public int markAllNotificationsAsRead(Long vendorId) {
        return notificationService.markAllAsRead(Notification.RecipientType.VENDOR, vendorId);
    }
    
    // ==================== Profile ====================
    
    /**
     * Updates vendor profile.
     * 
     * @param vendorId the vendor ID
     * @param vendorName the vendor name
     * @param companyName the company name
     * @param email the email
     * @param phoneNumber the phone number
     * @param address the address
     * @param city the city
     * @param state the state
     * @param pincode the pincode
     * @param gstNumber the GST number
     * @return true if updated
     */
    public boolean updateProfile(Long vendorId, String vendorName, String companyName,
                                  String email, String phoneNumber, String address,
                                  String city, String state, String pincode, String gstNumber) {
        Optional<Vendor> vendorOpt = vendorDAO.findById(vendorId);
        if (!vendorOpt.isPresent()) {
            throw new IllegalArgumentException("Vendor not found");
        }
        
        Vendor vendor = vendorOpt.get();
        
        // Check if email is being changed and if new email already exists
        if (!email.equals(vendor.getEmail()) && vendorDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        vendor.setVendorName(vendorName);
        vendor.setCompanyName(companyName);
        vendor.setEmail(email);
        vendor.setPhoneNumber(phoneNumber);
        vendor.setAddress(address);
        vendor.setCity(city);
        vendor.setState(state);
        vendor.setPincode(pincode);
        vendor.setGstNumber(gstNumber);
        
        return vendorDAO.updateProfile(vendor);
    }
    
    /**
     * Gets vendor by ID.
     * 
     * @param vendorId the vendor ID
     * @return Optional containing the vendor
     */
    public Optional<Vendor> getVendor(Long vendorId) {
        return vendorDAO.findById(vendorId).map(Vendor::toSafeCopy);
    }
    
    /**
     * Initiates password reset for a vendor.
     * 
     * @param email the vendor's email
     * @return the reset token if successful
     */
    public String initiatePasswordReset(String email) {
        Optional<Vendor> vendorOpt = vendorDAO.findByEmail(email);
        
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            
            // Generate reset token
            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusHours(24);
            
            // Save token
            vendorDAO.setResetToken(vendor.getId(), token, expiry);
            
            return token;
        }
        
        return null;
    }
    
    /**
     * Resets password using token.
     * 
     * @param token the reset token
     * @param newPassword the new password
     * @return true if successful
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<Vendor> vendorOpt = vendorDAO.findByResetToken(token);
        
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            
            // Hash new password
            String passwordHash = com.gtpl.utils.PasswordUtils.hashPassword(newPassword);
            
            // Update password and clear token
            vendorDAO.updatePassword(vendor.getId(), passwordHash);
            
            return true;
        }
        
        return false;
    }
}
