package com.gtpl.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Project Model Class
 * Represents a UG (Underground) work project in the system.
 * Projects are created by admins and assigned to vendors.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class Project {
    
    // Enum for project priority
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // Enum for project status
    public enum Status {
        ASSIGNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    }
    
    private Long id;
    private String projectCode;
    private String projectName;
    private String workDescription;
    private String startingPoint;
    private String endingPoint;
    private BigDecimal totalKm;
    private BigDecimal completedKm;
    private BigDecimal remainingKm;
    private BigDecimal progressPercentage;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private Priority priority;
    private Status status;
    private Long assignedVendorId;
    private Long createdByAdminId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields (not stored in DB)
    private String vendorName;
    private String vendorCompanyName;
    private String adminName;
    
    // Default constructor
    public Project() {
        this.totalKm = BigDecimal.ZERO;
        this.completedKm = BigDecimal.ZERO;
        this.priority = Priority.MEDIUM;
        this.status = Status.ASSIGNED;
    }
    
    // Constructor for creating new project
    public Project(String projectName, String workDescription, String startingPoint,
                   String endingPoint, BigDecimal totalKm, LocalDate expectedStartDate,
                   LocalDate expectedEndDate, Priority priority, Long createdByAdminId) {
        this.projectName = projectName;
        this.workDescription = workDescription;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.totalKm = totalKm != null ? totalKm : BigDecimal.ZERO;
        this.completedKm = BigDecimal.ZERO;
        this.expectedStartDate = expectedStartDate;
        this.expectedEndDate = expectedEndDate;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = Status.ASSIGNED;
        this.createdByAdminId = createdByAdminId;
    }
    
    // Full constructor
    public Project(Long id, String projectCode, String projectName, String workDescription,
                   String startingPoint, String endingPoint, BigDecimal totalKm,
                   BigDecimal completedKm, BigDecimal remainingKm, BigDecimal progressPercentage,
                   LocalDate expectedStartDate, LocalDate expectedEndDate,
                   LocalDate actualStartDate, LocalDate actualEndDate,
                   Priority priority, Status status, Long assignedVendorId,
                   Long createdByAdminId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.workDescription = workDescription;
        this.startingPoint = startingPoint;
        this.endingPoint = endingPoint;
        this.totalKm = totalKm;
        this.completedKm = completedKm;
        this.remainingKm = remainingKm;
        this.progressPercentage = progressPercentage;
        this.expectedStartDate = expectedStartDate;
        this.expectedEndDate = expectedEndDate;
        this.actualStartDate = actualStartDate;
        this.actualEndDate = actualEndDate;
        this.priority = priority;
        this.status = status;
        this.assignedVendorId = assignedVendorId;
        this.createdByAdminId = createdByAdminId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProjectCode() {
        return projectCode;
    }
    
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getWorkDescription() {
        return workDescription;
    }
    
    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }
    
    public String getStartingPoint() {
        return startingPoint;
    }
    
    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }
    
    public String getEndingPoint() {
        return endingPoint;
    }
    
    public void setEndingPoint(String endingPoint) {
        this.endingPoint = endingPoint;
    }
    
    public BigDecimal getTotalKm() {
        return totalKm;
    }
    
    public void setTotalKm(BigDecimal totalKm) {
        this.totalKm = totalKm;
    }
    
    public BigDecimal getCompletedKm() {
        return completedKm;
    }
    
    public void setCompletedKm(BigDecimal completedKm) {
        this.completedKm = completedKm;
    }
    
    public BigDecimal getRemainingKm() {
        return remainingKm;
    }
    
    public void setRemainingKm(BigDecimal remainingKm) {
        this.remainingKm = remainingKm;
    }
    
    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public LocalDate getExpectedStartDate() {
        return expectedStartDate;
    }
    
    public void setExpectedStartDate(LocalDate expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }
    
    public LocalDate getExpectedEndDate() {
        return expectedEndDate;
    }
    
    public void setExpectedEndDate(LocalDate expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }
    
    public LocalDate getActualStartDate() {
        return actualStartDate;
    }
    
    public void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }
    
    public LocalDate getActualEndDate() {
        return actualEndDate;
    }
    
    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Long getAssignedVendorId() {
        return assignedVendorId;
    }
    
    public void setAssignedVendorId(Long assignedVendorId) {
        this.assignedVendorId = assignedVendorId;
    }
    
    public Long getCreatedByAdminId() {
        return createdByAdminId;
    }
    
    public void setCreatedByAdminId(Long createdByAdminId) {
        this.createdByAdminId = createdByAdminId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Transient field getters/setters
    public String getVendorName() {
        return vendorName;
    }
    
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    public String getVendorCompanyName() {
        return vendorCompanyName;
    }
    
    public void setVendorCompanyName(String vendorCompanyName) {
        this.vendorCompanyName = vendorCompanyName;
    }
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    /**
     * Calculates progress percentage based on completed vs total KM
     */
    public void calculateProgress() {
        if (totalKm != null && totalKm.compareTo(BigDecimal.ZERO) > 0 && completedKm != null) {
            this.progressPercentage = completedKm
                    .multiply(new BigDecimal("100"))
                    .divide(totalKm, 2, BigDecimal.ROUND_HALF_UP);
            this.remainingKm = totalKm.subtract(completedKm);
        } else {
            this.progressPercentage = BigDecimal.ZERO;
            this.remainingKm = totalKm != null ? totalKm : BigDecimal.ZERO;
        }
    }
    
    /**
     * Checks if the project is overdue
     */
    public boolean isOverdue() {
        return status != Status.COMPLETED && 
               status != Status.CANCELLED && 
               expectedEndDate != null && 
               LocalDate.now().isAfter(expectedEndDate);
    }
    
    /**
     * Checks if the project is due soon (within 7 days)
     */
    public boolean isDueSoon() {
        return status != Status.COMPLETED && 
               status != Status.CANCELLED && 
               expectedEndDate != null && 
               !LocalDate.now().isAfter(expectedEndDate) &&
               LocalDate.now().plusDays(7).isAfter(expectedEndDate);
    }
    
    /**
     * Gets the number of days remaining until deadline
     */
    public long getDaysRemaining() {
        if (expectedEndDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expectedEndDate);
    }
    
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", projectCode='" + projectCode + '\'' +
                ", projectName='" + projectName + '\'' +
                ", totalKm=" + totalKm +
                ", completedKm=" + completedKm +
                ", progressPercentage=" + progressPercentage +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
