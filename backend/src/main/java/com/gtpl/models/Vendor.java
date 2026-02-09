package com.gtpl.models;

import java.time.LocalDateTime;

/**
 * Vendor Model Class
 * Represents an external contractor/vendor in the system.
 * Vendors can sign up, log in, and manage their assigned work.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class Vendor {
    
    private Long id;
    private String vendorName;
    private String companyName;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstNumber;
    private boolean isActive;
    private boolean isVerified;
    private boolean emailVerified;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    // Default constructor
    public Vendor() {
    }
    
    // Constructor for signup
    public Vendor(String vendorName, String companyName, String email, 
                  String phoneNumber, String passwordHash) {
        this.vendorName = vendorName;
        this.companyName = companyName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.isActive = true;
        this.isVerified = false;
        this.emailVerified = false;
    }
    
    // Full constructor
    public Vendor(Long id, String vendorName, String companyName, String email,
                  String phoneNumber, String passwordHash, String address, String city,
                  String state, String pincode, String gstNumber, boolean isActive,
                  boolean isVerified, boolean emailVerified, String resetToken,
                  LocalDateTime resetTokenExpiry, LocalDateTime createdAt,
                  LocalDateTime updatedAt, LocalDateTime lastLogin) {
        this.id = id;
        this.vendorName = vendorName;
        this.companyName = companyName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.gstNumber = gstNumber;
        this.isActive = isActive;
        this.isVerified = isVerified;
        this.emailVerified = emailVerified;
        this.resetToken = resetToken;
        this.resetTokenExpiry = resetTokenExpiry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLogin = lastLogin;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getVendorName() {
        return vendorName;
    }
    
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPincode() {
        return pincode;
    }
    
    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
    
    public String getGstNumber() {
        return gstNumber;
    }
    
    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public String getResetToken() {
        return resetToken;
    }
    
    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
    
    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }
    
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
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
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    /**
     * Checks if the reset token is valid and not expired
     */
    public boolean isResetTokenValid() {
        if (resetToken == null || resetTokenExpiry == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(resetTokenExpiry);
    }
    
    /**
     * Returns a safe copy without sensitive data (password hash, reset token)
     */
    public Vendor toSafeCopy() {
        Vendor safe = new Vendor();
        safe.setId(this.id);
        safe.setVendorName(this.vendorName);
        safe.setCompanyName(this.companyName);
        safe.setEmail(this.email);
        safe.setPhoneNumber(this.phoneNumber);
        safe.setAddress(this.address);
        safe.setCity(this.city);
        safe.setState(this.state);
        safe.setPincode(this.pincode);
        safe.setGstNumber(this.gstNumber);
        safe.setActive(this.isActive);
        safe.setVerified(this.isVerified);
        safe.setEmailVerified(this.emailVerified);
        safe.setCreatedAt(this.createdAt);
        safe.setUpdatedAt(this.updatedAt);
        safe.setLastLogin(this.lastLogin);
        return safe;
    }
    
    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", vendorName='" + vendorName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isActive=" + isActive +
                ", isVerified=" + isVerified +
                '}';
    }
}
