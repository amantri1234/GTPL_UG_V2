package com.gtpl.services;

import com.gtpl.daos.*;
import com.gtpl.models.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Admin Service Class
 * Handles admin-related business logic and operations.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class AdminService {
    
    private final AdminDAO adminDAO;
    private final VendorDAO vendorDAO;
    private final ProjectDAO projectDAO;
    private final DailyProgressDAO dailyProgressDAO;
    private final MaterialDAO materialDAO;
    private final MaterialRequestDAO materialRequestDAO;
    private final NotificationService notificationService;
    
    public AdminService() {
        this.adminDAO = new AdminDAO();
        this.vendorDAO = new VendorDAO();
        this.projectDAO = new ProjectDAO();
        this.dailyProgressDAO = new DailyProgressDAO();
        this.materialDAO = new MaterialDAO();
        this.materialRequestDAO = new MaterialRequestDAO();
        this.notificationService = new NotificationService();
    }
    
    // ==================== Dashboard ====================
    
    /**
     * Gets admin dashboard data.
     * 
     * @return map containing dashboard statistics
     */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        // Project statistics
        int[] projectStats = projectDAO.getProjectStats();
        data.put("totalProjects", projectStats[0]);
        data.put("assignedProjects", projectStats[1]);
        data.put("inProgressProjects", projectStats[2]);
        data.put("completedProjects", projectStats[3]);
        data.put("onHoldProjects", projectStats[4]);
        data.put("cancelledProjects", projectStats[5]);
        
        // Vendor statistics
        int[] vendorStats = vendorDAO.getVendorCounts();
        data.put("totalVendors", vendorStats[0]);
        data.put("activeVendors", vendorStats[1]);
        data.put("inactiveVendors", vendorStats[2]);
        
        // Material request statistics
        int[] requestStats = materialRequestDAO.getRequestStats();
        data.put("totalMaterialRequests", requestStats[0]);
        data.put("pendingMaterialRequests", requestStats[1]);
        
        // Overdue projects
        data.put("overdueProjects", projectDAO.getOverdueProjectsCount());
        
        // Recent projects
        data.put("recentProjects", projectDAO.findAll(0, 5));
        
        // Recent material requests
        data.put("recentMaterialRequests", materialRequestDAO.findPending());
        
        return data;
    }
    
    // ==================== Project Management ====================
    
    /**
     * Creates a new project.
     * 
     * @param projectName the project name
     * @param workDescription the work description
     * @param startingPoint the starting point
     * @param endingPoint the ending point
     * @param totalKm the total KM
     * @param expectedStartDate the expected start date
     * @param expectedEndDate the expected end date
     * @param priority the priority
     * @param adminId the creating admin ID
     * @return the created project ID
     */
    public Long createProject(String projectName, String workDescription, String startingPoint,
                              String endingPoint, BigDecimal totalKm, LocalDate expectedStartDate,
                              LocalDate expectedEndDate, Project.Priority priority, Long adminId) {
        // Validate input
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (startingPoint == null || startingPoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Starting point is required");
        }
        if (endingPoint == null || endingPoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Ending point is required");
        }
        if (totalKm == null || totalKm.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total KM must be greater than 0");
        }
        if (expectedStartDate == null || expectedEndDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (expectedEndDate.isBefore(expectedStartDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        Project project = new Project(projectName, workDescription, startingPoint, endingPoint,
                totalKm, expectedStartDate, expectedEndDate, priority, adminId);
        
        return projectDAO.create(project);
    }
    
    /**
     * Assigns a project to a vendor.
     * 
     * @param projectId the project ID
     * @param vendorId the vendor ID
     * @return true if assigned
     */
    public boolean assignProjectToVendor(Long projectId, Long vendorId) {
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        Optional<Vendor> vendorOpt = vendorDAO.findById(vendorId);
        
        if (!projectOpt.isPresent()) {
            throw new IllegalArgumentException("Project not found");
        }
        if (!vendorOpt.isPresent()) {
            throw new IllegalArgumentException("Vendor not found");
        }
        
        Vendor vendor = vendorOpt.get();
        if (!vendor.isActive()) {
            throw new IllegalArgumentException("Vendor is not active");
        }
        
        boolean assigned = projectDAO.assignToVendor(projectId, vendorId);
        
        if (assigned) {
            // Notify vendor
            notificationService.notifyProjectAssigned(vendorId, projectOpt.get().getProjectName());
        }
        
        return assigned;
    }
    
    /**
     * Updates project status.
     * 
     * @param projectId the project ID
     * @param status the new status
     * @return true if updated
     */
    public boolean updateProjectStatus(Long projectId, Project.Status status) {
        return projectDAO.updateStatus(projectId, status);
    }
    
    /**
     * Cancels a project.
     * 
     * @param projectId the project ID
     * @return true if cancelled
     */
    public boolean cancelProject(Long projectId) {
        return projectDAO.updateStatus(projectId, Project.Status.CANCELLED);
    }
    
    /**
     * Gets all projects.
     * 
     * @return list of projects
     */
    public List<Project> getAllProjects() {
        return projectDAO.findAll();
    }
    
    /**
     * Gets project by ID.
     * 
     * @param projectId the project ID
     * @return Optional containing the project
     */
    public Optional<Project> getProject(Long projectId) {
        return projectDAO.findById(projectId);
    }
    
    // ==================== Vendor Management ====================
    
    /**
     * Gets all vendors.
     * 
     * @return list of vendors
     */
    public List<Vendor> getAllVendors() {
        return vendorDAO.findAll();
    }
    
    /**
     * Gets active vendors.
     * 
     * @return list of active vendors
     */
    public List<Vendor> getActiveVendors() {
        return vendorDAO.findAllActive();
    }
    
    /**
     * Gets vendor by ID.
     * 
     * @param vendorId the vendor ID
     * @return Optional containing the vendor
     */
    public Optional<Vendor> getVendor(Long vendorId) {
        return vendorDAO.findById(vendorId);
    }
    
    /**
     * Toggles vendor active status.
     * 
     * @param vendorId the vendor ID
     * @return true if toggled
     */
    public boolean toggleVendorStatus(Long vendorId) {
        return vendorDAO.toggleActiveStatus(vendorId);
    }
    
    /**
     * Verifies a vendor.
     * 
     * @param vendorId the vendor ID
     * @return true if verified
     */
    public boolean verifyVendor(Long vendorId) {
        return vendorDAO.verifyVendor(vendorId);
    }
    
    /**
     * Gets vendor performance data.
     * 
     * @param vendorId the vendor ID
     * @return map containing performance data
     */
    public Map<String, Object> getVendorPerformance(Long vendorId) {
        Map<String, Object> performance = new HashMap<>();
        
        Optional<Vendor> vendorOpt = vendorDAO.findById(vendorId);
        if (!vendorOpt.isPresent()) {
            return performance;
        }
        
        Vendor vendor = vendorOpt.get();
        performance.put("vendor", vendor.toSafeCopy());
        
        // Get vendor's projects
        List<Project> projects = projectDAO.findByVendorId(vendorId);
        performance.put("projects", projects);
        
        // Calculate statistics
        int totalProjects = projects.size();
        int completedProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == Project.Status.COMPLETED)
                .count();
        int activeProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == Project.Status.IN_PROGRESS)
                .count();
        
        BigDecimal totalKmAssigned = projects.stream()
                .map(Project::getTotalKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalKmCompleted = projects.stream()
                .map(Project::getCompletedKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal completionRate = totalKmAssigned.compareTo(BigDecimal.ZERO) > 0
                ? totalKmCompleted.multiply(new BigDecimal("100")).divide(totalKmAssigned, 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        
        performance.put("totalProjects", totalProjects);
        performance.put("completedProjects", completedProjects);
        performance.put("activeProjects", activeProjects);
        performance.put("totalKmAssigned", totalKmAssigned);
        performance.put("totalKmCompleted", totalKmCompleted);
        performance.put("completionRate", completionRate);
        
        // Get daily progress stats
        List<DailyProgress> dailyProgressList = dailyProgressDAO.findByVendorId(vendorId);
        performance.put("totalDailyReports", dailyProgressList.size());
        
        return performance;
    }
    
    // ==================== Material Management ====================
    
    /**
     * Gets all materials.
     * 
     * @return list of materials
     */
    public List<Material> getAllMaterials() {
        return materialDAO.findAll();
    }
    
    /**
     * Gets active materials.
     * 
     * @return list of active materials
     */
    public List<Material> getActiveMaterials() {
        return materialDAO.findAllActive();
    }
    
    /**
     * Creates a new material.
     * 
     * @param materialCode the material code
     * @param materialName the material name
     * @param description the description
     * @param unit the unit
     * @param category the category
     * @return the created material ID
     */
    public Long createMaterial(String materialCode, String materialName, String description,
                               Material.Unit unit, String category) {
        if (materialCode == null || materialCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Material code is required");
        }
        if (materialDAO.materialCodeExists(materialCode)) {
            throw new IllegalArgumentException("Material code already exists");
        }
        
        Material material = new Material(materialCode, materialName, description, unit, category);
        return materialDAO.create(material);
    }
    
    /**
     * Updates a material.
     * 
     * @param materialId the material ID
     * @param materialName the material name
     * @param description the description
     * @param unit the unit
     * @param category the category
     * @param isActive the active status
     * @return true if updated
     */
    public boolean updateMaterial(Long materialId, String materialName, String description,
                                  Material.Unit unit, String category, boolean isActive) {
        Optional<Material> materialOpt = materialDAO.findById(materialId);
        if (!materialOpt.isPresent()) {
            throw new IllegalArgumentException("Material not found");
        }
        
        Material material = materialOpt.get();
        material.setMaterialName(materialName);
        material.setDescription(description);
        material.setUnit(unit);
        material.setCategory(category);
        material.setActive(isActive);
        
        return materialDAO.update(material);
    }
    
    // ==================== Material Request Management ====================
    
    /**
     * Gets all material requests.
     * 
     * @return list of material requests
     */
    public List<MaterialRequest> getAllMaterialRequests() {
        return materialRequestDAO.findAll();
    }
    
    /**
     * Gets pending material requests.
     * 
     * @return list of pending requests
     */
    public List<MaterialRequest> getPendingMaterialRequests() {
        return materialRequestDAO.findPending();
    }
    
    /**
     * Approves a material request.
     * 
     * @param requestId the request ID
     * @param approvedQuantity the approved quantity
     * @param adminId the approving admin ID
     * @return true if approved
     */
    public boolean approveMaterialRequest(Long requestId, BigDecimal approvedQuantity, Long adminId) {
        Optional<MaterialRequest> requestOpt = materialRequestDAO.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Material request not found");
        }
        
        MaterialRequest request = requestOpt.get();
        boolean approved = materialRequestDAO.approve(requestId, approvedQuantity, adminId);
        
        if (approved) {
            // Notify vendor
            notificationService.notifyMaterialRequestApproved(
                request.getVendorId(), 
                request.getRequestCode(), 
                request.getMaterialName()
            );
        }
        
        return approved;
    }
    
    /**
     * Rejects a material request.
     * 
     * @param requestId the request ID
     * @param reason the rejection reason
     * @param adminId the rejecting admin ID
     * @return true if rejected
     */
    public boolean rejectMaterialRequest(Long requestId, String reason, Long adminId) {
        Optional<MaterialRequest> requestOpt = materialRequestDAO.findById(requestId);
        if (!requestOpt.isPresent()) {
            throw new IllegalArgumentException("Material request not found");
        }
        
        MaterialRequest request = requestOpt.get();
        boolean rejected = materialRequestDAO.reject(requestId, reason, adminId);
        
        if (rejected) {
            // Notify vendor
            notificationService.notifyMaterialRequestRejected(
                request.getVendorId(),
                request.getRequestCode(),
                request.getMaterialName(),
                reason
            );
        }
        
        return rejected;
    }
    
    // ==================== Daily Progress & Compliance ====================
    
    /**
     * Gets daily progress for all projects.
     * 
     * @return list of daily progress records
     */
    public List<DailyProgress> getAllDailyProgress() {
        return dailyProgressDAO.findAll();
    }
    
    /**
     * Checks for vendors who missed daily updates and sends notifications.
     */
    public void checkDailyUpdateCompliance() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Long> projectIds = dailyProgressDAO.getProjectsWithMissingUpdates(yesterday);
        
        for (Long projectId : projectIds) {
            Optional<Project> projectOpt = projectDAO.findById(projectId);
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                if (project.getAssignedVendorId() != null) {
                    notificationService.notifyMissedDailyUpdate(
                        project.getAssignedVendorId(),
                        project.getProjectName(),
                        yesterday.toString()
                    );
                }
            }
        }
    }
    
    /**
     * Gets compliance status for all active projects.
     * 
     * @return list of compliance data
     */
    public List<Map<String, Object>> getComplianceStatus() {
        List<Map<String, Object>> complianceList = new ArrayList<>();
        List<Project> activeProjects = projectDAO.findByStatus(Project.Status.IN_PROGRESS);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        for (Project project : activeProjects) {
            Map<String, Object> compliance = new HashMap<>();
            compliance.put("project", project);
            
            boolean hasUpdate = dailyProgressDAO.existsForProjectAndDate(project.getId(), yesterday);
            compliance.put("hasDailyUpdate", hasUpdate);
            compliance.put("expectedDate", yesterday);
            
            if (project.getAssignedVendorId() != null) {
                Optional<Vendor> vendorOpt = vendorDAO.findById(project.getAssignedVendorId());
                vendorOpt.ifPresent(v -> compliance.put("vendor", v.toSafeCopy()));
            }
            
            // Calculate days since last update
            Optional<DailyProgress> lastUpdate = dailyProgressDAO.findLastByProjectId(project.getId());
            if (lastUpdate.isPresent()) {
                long daysSinceUpdate = ChronoUnit.DAYS.between(
                    lastUpdate.get().getWorkDate(), 
                    LocalDate.now()
                );
                compliance.put("daysSinceUpdate", daysSinceUpdate);
            } else {
                compliance.put("daysSinceUpdate", ChronoUnit.DAYS.between(
                    project.getExpectedStartDate(), 
                    LocalDate.now()
                ));
            }
            
            complianceList.add(compliance);
        }
        
        return complianceList;
    }
    
    // ==================== Admin Profile ====================
    
    /**
     * Updates admin profile.
     * 
     * @param adminId the admin ID
     * @param fullName the full name
     * @param email the email
     * @param phone the phone
     * @return true if updated
     */
    public boolean updateProfile(Long adminId, String fullName, String email, String phone) {
        Optional<Admin> adminOpt = adminDAO.findById(adminId);
        if (!adminOpt.isPresent()) {
            throw new IllegalArgumentException("Admin not found");
        }
        
        Admin admin = adminOpt.get();
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setPhone(phone);
        
        return adminDAO.updateProfile(admin);
    }
    
    /**
     * Gets admin by ID.
     * 
     * @param adminId the admin ID
     * @return Optional containing the admin
     */
    public Optional<Admin> getAdmin(Long adminId) {
        return adminDAO.findById(adminId).map(Admin::toSafeCopy);
    }
}
