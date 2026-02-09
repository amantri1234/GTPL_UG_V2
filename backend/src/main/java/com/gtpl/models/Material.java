package com.gtpl.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Material Model Class
 * Represents a material/item in the system catalog.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class Material {
    
    // Enum for unit of measurement
    public enum Unit {
        METERS, KILOGRAMS, UNITS, LITERS, BOXES, ROLLS, SETS
    }
    
    private Long id;
    private String materialCode;
    private String materialName;
    private String description;
    private Unit unit;
    private String category;
    private BigDecimal standardQuantity;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Material() {
        this.isActive = true;
    }
    
    // Constructor for new material
    public Material(String materialCode, String materialName, String description, 
                    Unit unit, String category) {
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.description = description;
        this.unit = unit;
        this.category = category;
        this.isActive = true;
    }
    
    // Full constructor
    public Material(Long id, String materialCode, String materialName, String description,
                    Unit unit, String category, BigDecimal standardQuantity,
                    boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.description = description;
        this.unit = unit;
        this.category = category;
        this.standardQuantity = standardQuantity;
        this.isActive = isActive;
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
    
    public String getMaterialCode() {
        return materialCode;
    }
    
    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }
    
    public String getMaterialName() {
        return materialName;
    }
    
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Unit getUnit() {
        return unit;
    }
    
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public BigDecimal getStandardQuantity() {
        return standardQuantity;
    }
    
    public void setStandardQuantity(BigDecimal standardQuantity) {
        this.standardQuantity = standardQuantity;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
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
    
    /**
     * Gets the display name for the unit
     */
    public String getUnitDisplayName() {
        if (unit == null) return "";
        switch (unit) {
            case METERS: return "Meters";
            case KILOGRAMS: return "Kilograms";
            case UNITS: return "Units";
            case LITERS: return "Liters";
            case BOXES: return "Boxes";
            case ROLLS: return "Rolls";
            case SETS: return "Sets";
            default: return unit.name();
        }
    }
    
    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", unit=" + unit +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
