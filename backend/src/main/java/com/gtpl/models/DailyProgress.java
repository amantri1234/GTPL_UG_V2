package com.gtpl.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DailyProgress Model Class
 * Represents a daily work report submitted by vendors.
 * These records are immutable once submitted.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class DailyProgress {
    
    private Long id;
    private Long projectId;
    private Long vendorId;
    private LocalDate workDate;
    private String fromPoint;
    private String toPoint;
    private BigDecimal kmCompletedToday;
    private BigDecimal cumulativeKm;
    private BigDecimal remainingEstimatedWork;
    private String workDescription;
    private String remarks;
    private String weatherConditions;
    private Integer workforceCount;
    private String equipmentUsed;
    private Boolean isSubmitted;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    
    // Transient fields
    private String projectName;
    private String projectCode;
    private String vendorName;
    
    // Default constructor
    public DailyProgress() {
        this.kmCompletedToday = BigDecimal.ZERO;
        this.cumulativeKm = BigDecimal.ZERO;
        this.workforceCount = 0;
        this.isSubmitted = true;
    }
    
    // Constructor for new daily report
    public DailyProgress(Long projectId, Long vendorId, LocalDate workDate,
                         String fromPoint, String toPoint, BigDecimal kmCompletedToday,
                         BigDecimal remainingEstimatedWork, String workDescription,
                         String remarks) {
        this.projectId = projectId;
        this.vendorId = vendorId;
        this.workDate = workDate;
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        this.kmCompletedToday = kmCompletedToday != null ? kmCompletedToday : BigDecimal.ZERO;
        this.remainingEstimatedWork = remainingEstimatedWork;
        this.workDescription = workDescription;
        this.remarks = remarks;
        this.isSubmitted = true;
    }
    
    // Full constructor
    public DailyProgress(Long id, Long projectId, Long vendorId, LocalDate workDate,
                         String fromPoint, String toPoint, BigDecimal kmCompletedToday,
                         BigDecimal cumulativeKm, BigDecimal remainingEstimatedWork,
                         String workDescription, String remarks, String weatherConditions,
                         Integer workforceCount, String equipmentUsed, Boolean isSubmitted,
                         LocalDateTime submittedAt, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.vendorId = vendorId;
        this.workDate = workDate;
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        this.kmCompletedToday = kmCompletedToday;
        this.cumulativeKm = cumulativeKm;
        this.remainingEstimatedWork = remainingEstimatedWork;
        this.workDescription = workDescription;
        this.remarks = remarks;
        this.weatherConditions = weatherConditions;
        this.workforceCount = workforceCount;
        this.equipmentUsed = equipmentUsed;
        this.isSubmitted = isSubmitted;
        this.submittedAt = submittedAt;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public Long getVendorId() {
        return vendorId;
    }
    
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }
    
    public LocalDate getWorkDate() {
        return workDate;
    }
    
    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }
    
    public String getFromPoint() {
        return fromPoint;
    }
    
    public void setFromPoint(String fromPoint) {
        this.fromPoint = fromPoint;
    }
    
    public String getToPoint() {
        return toPoint;
    }
    
    public void setToPoint(String toPoint) {
        this.toPoint = toPoint;
    }
    
    public BigDecimal getKmCompletedToday() {
        return kmCompletedToday;
    }
    
    public void setKmCompletedToday(BigDecimal kmCompletedToday) {
        this.kmCompletedToday = kmCompletedToday;
    }
    
    public BigDecimal getCumulativeKm() {
        return cumulativeKm;
    }
    
    public void setCumulativeKm(BigDecimal cumulativeKm) {
        this.cumulativeKm = cumulativeKm;
    }
    
    public BigDecimal getRemainingEstimatedWork() {
        return remainingEstimatedWork;
    }
    
    public void setRemainingEstimatedWork(BigDecimal remainingEstimatedWork) {
        this.remainingEstimatedWork = remainingEstimatedWork;
    }
    
    public String getWorkDescription() {
        return workDescription;
    }
    
    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public String getWeatherConditions() {
        return weatherConditions;
    }
    
    public void setWeatherConditions(String weatherConditions) {
        this.weatherConditions = weatherConditions;
    }
    
    public Integer getWorkforceCount() {
        return workforceCount;
    }
    
    public void setWorkforceCount(Integer workforceCount) {
        this.workforceCount = workforceCount;
    }
    
    public String getEquipmentUsed() {
        return equipmentUsed;
    }
    
    public void setEquipmentUsed(String equipmentUsed) {
        this.equipmentUsed = equipmentUsed;
    }
    
    public Boolean getIsSubmitted() {
        return isSubmitted;
    }
    
    public void setIsSubmitted(Boolean isSubmitted) {
        this.isSubmitted = isSubmitted;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Transient field getters/setters
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getProjectCode() {
        return projectCode;
    }
    
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    
    public String getVendorName() {
        return vendorName;
    }
    
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    @Override
    public String toString() {
        return "DailyProgress{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", vendorId=" + vendorId +
                ", workDate=" + workDate +
                ", kmCompletedToday=" + kmCompletedToday +
                ", cumulativeKm=" + cumulativeKm +
                '}';
    }
}
