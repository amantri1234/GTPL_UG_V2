package com.gtpl.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MaterialRequest Model Class
 * Represents a material request submitted by vendors and processed by admins.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class MaterialRequest {
    
    // Enum for urgency level
    public enum Urgency {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // Enum for request status
    public enum Status {
        PENDING, APPROVED, REJECTED, PARTIALLY_APPROVED, FULFILLED
    }
    
    private Long id;
    private String requestCode;
    private Long projectId;
    private Long vendorId;
    private Long materialId;
    private BigDecimal quantityRequired;
    private String reason;
    private Urgency urgency;
    private Status status;
    private BigDecimal approvedQuantity;
    private Long approvedByAdminId;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime requestedAt;
    private LocalDateTime fulfilledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Transient fields
    private String projectName;
    private String projectCode;
    private String vendorName;
    private String materialName;
    private String materialCode;
    private String unit;
    private String adminName;
    
    // Default constructor
    public MaterialRequest() {
        this.urgency = Urgency.MEDIUM;
        this.status = Status.PENDING;
    }
    
    // Constructor for new request
    public MaterialRequest(Long projectId, Long vendorId, Long materialId,
                           BigDecimal quantityRequired, String reason, Urgency urgency) {
        this.projectId = projectId;
        this.vendorId = vendorId;
        this.materialId = materialId;
        this.quantityRequired = quantityRequired;
        this.reason = reason;
        this.urgency = urgency != null ? urgency : Urgency.MEDIUM;
        this.status = Status.PENDING;
    }
    
    // Full constructor
    public MaterialRequest(Long id, String requestCode, Long projectId, Long vendorId,
                           Long materialId, BigDecimal quantityRequired, String reason,
                           Urgency urgency, Status status, BigDecimal approvedQuantity,
                           Long approvedByAdminId, LocalDateTime approvedAt,
                           String rejectionReason, LocalDateTime requestedAt,
                           LocalDateTime fulfilledAt, LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.requestCode = requestCode;
        this.projectId = projectId;
        this.vendorId = vendorId;
        this.materialId = materialId;
        this.quantityRequired = quantityRequired;
        this.reason = reason;
        this.urgency = urgency;
        this.status = status;
        this.approvedQuantity = approvedQuantity;
        this.approvedByAdminId = approvedByAdminId;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.requestedAt = requestedAt;
        this.fulfilledAt = fulfilledAt;
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
    
    public String getRequestCode() {
        return requestCode;
    }
    
    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
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
    
    public Long getMaterialId() {
        return materialId;
    }
    
    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }
    
    public BigDecimal getQuantityRequired() {
        return quantityRequired;
    }
    
    public void setQuantityRequired(BigDecimal quantityRequired) {
        this.quantityRequired = quantityRequired;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Urgency getUrgency() {
        return urgency;
    }
    
    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public BigDecimal getApprovedQuantity() {
        return approvedQuantity;
    }
    
    public void setApprovedQuantity(BigDecimal approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
    }
    
    public Long getApprovedByAdminId() {
        return approvedByAdminId;
    }
    
    public void setApprovedByAdminId(Long approvedByAdminId) {
        this.approvedByAdminId = approvedByAdminId;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
    
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
    
    public LocalDateTime getFulfilledAt() {
        return fulfilledAt;
    }
    
    public void setFulfilledAt(LocalDateTime fulfilledAt) {
        this.fulfilledAt = fulfilledAt;
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
    
    public String getMaterialName() {
        return materialName;
    }
    
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }
    
    public String getMaterialCode() {
        return materialCode;
    }
    
    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    /**
     * Gets urgency level CSS class for UI display
     */
    public String getUrgencyClass() {
        if (urgency == null) return "secondary";
        switch (urgency) {
            case CRITICAL: return "danger";
            case HIGH: return "warning";
            case MEDIUM: return "info";
            case LOW: return "success";
            default: return "secondary";
        }
    }
    
    /**
     * Gets status CSS class for UI display
     */
    public String getStatusClass() {
        if (status == null) return "secondary";
        switch (status) {
            case APPROVED: return "success";
            case FULFILLED: return "primary";
            case REJECTED: return "danger";
            case PARTIALLY_APPROVED: return "info";
            case PENDING: return "warning";
            default: return "secondary";
        }
    }
    
    @Override
    public String toString() {
        return "MaterialRequest{" +
                "id=" + id +
                ", requestCode='" + requestCode + '\'' +
                ", projectId=" + projectId +
                ", vendorId=" + vendorId +
                ", materialId=" + materialId +
                ", quantityRequired=" + quantityRequired +
                ", urgency=" + urgency +
                ", status=" + status +
                '}';
    }
}
