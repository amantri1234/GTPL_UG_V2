package com.gtpl.services;

import com.gtpl.daos.AdminDAO;
import com.gtpl.daos.VendorDAO;
import com.gtpl.models.Admin;
import com.gtpl.models.Vendor;
import com.gtpl.utils.PasswordUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Authentication Service Class
 * Handles user authentication, registration, and password management.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class AuthService {
    
    private final AdminDAO adminDAO;
    private final VendorDAO vendorDAO;
    private final NotificationService notificationService;
    
    public AuthService() {
        this.adminDAO = new AdminDAO();
        this.vendorDAO = new VendorDAO();
        this.notificationService = new NotificationService();
    }
    
    /**
     * Authenticates an admin user.
     * 
     * @param username the username
     * @param password the password
     * @return Optional containing the admin if authenticated
     */
    public Optional<Admin> authenticateAdmin(String username, String password) {
        Optional<Admin> adminOpt = adminDAO.findByUsername(username);
        
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (PasswordUtils.verifyPassword(password, admin.getPasswordHash())) {
                // Update last login
                adminDAO.updateLastLogin(admin.getId());
                return Optional.of(admin.toSafeCopy());
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Authenticates a vendor user.
     * 
     * @param email the email
     * @param password the password
     * @return Optional containing the vendor if authenticated
     */
    public Optional<Vendor> authenticateVendor(String email, String password) {
        Optional<Vendor> vendorOpt = vendorDAO.findByEmail(email);
        
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            if (vendor.isActive() && PasswordUtils.verifyPassword(password, vendor.getPasswordHash())) {
                // Update last login
                vendorDAO.updateLastLogin(vendor.getId());
                return Optional.of(vendor.toSafeCopy());
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Registers a new vendor.
     * 
     * @param vendorName the vendor name
     * @param companyName the company name
     * @param email the email
     * @param phoneNumber the phone number
     * @param password the password
     * @return the created vendor ID, or null if registration failed
     */
    public Long registerVendor(String vendorName, String companyName, String email, 
                               String phoneNumber, String password) {
        // Validate input
        if (vendorName == null || vendorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Vendor name is required");
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        // Check if email already exists
        if (vendorDAO.emailExists(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (vendorDAO.phoneExists(phoneNumber)) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        
        // Validate password strength
        PasswordUtils.PasswordValidationResult validation = PasswordUtils.validatePassword(password);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }
        
        // Hash password
        String passwordHash = PasswordUtils.hashPassword(password);
        
        // Create vendor
        Vendor vendor = new Vendor(vendorName, companyName, email, phoneNumber, passwordHash);
        Long vendorId = vendorDAO.create(vendor);
        
        if (vendorId != null) {
            // Notify admins about new vendor registration
            notificationService.notifyNewVendorRegistration(vendorId, vendorName, companyName);
        }
        
        return vendorId;
    }
    
    /**
     * Initiates password reset for a vendor.
     * 
     * @param email the vendor's email
     * @return true if reset email was sent (or would be sent in a real system)
     */
    public boolean initiatePasswordReset(String email) {
        Optional<Vendor> vendorOpt = vendorDAO.findByEmail(email);
        
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            
            // Generate reset token
            String token = PasswordUtils.generateResetToken();
            LocalDateTime expiry = LocalDateTime.now().plusHours(24); // 24 hour expiry
            
            // Save token to database
            vendorDAO.setResetToken(vendor.getId(), token, expiry);
            
            // In a real system, send email with reset link
            // For now, we just return true to indicate success
            // The token would be included in a link like: /reset-password?token=xxx
            
            return true;
        }
        
        // Return true even if email not found (security best practice)
        return true;
    }
    
    /**
     * Validates a password reset token.
     * 
     * @param token the reset token
     * @return Optional containing the vendor if token is valid
     */
    public Optional<Vendor> validateResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return vendorDAO.findByResetToken(token);
    }
    
    /**
     * Resets a vendor's password.
     * 
     * @param token the reset token
     * @param newPassword the new password
     * @return true if password was reset
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<Vendor> vendorOpt = validateResetToken(token);
        
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            
            // Validate password strength
            PasswordUtils.PasswordValidationResult validation = PasswordUtils.validatePassword(newPassword);
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getMessage());
            }
            
            // Hash and update password
            String passwordHash = PasswordUtils.hashPassword(newPassword);
            vendorDAO.updatePassword(vendor.getId(), passwordHash);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Changes a vendor's password.
     * 
     * @param vendorId the vendor ID
     * @param currentPassword the current password
     * @param newPassword the new password
     * @return true if password was changed
     */
    public boolean changeVendorPassword(Long vendorId, String currentPassword, String newPassword) {
        Optional<Vendor> vendorOpt = vendorDAO.findById(vendorId);
        
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            
            // Verify current password
            if (!PasswordUtils.verifyPassword(currentPassword, vendor.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            
            // Validate new password strength
            PasswordUtils.PasswordValidationResult validation = PasswordUtils.validatePassword(newPassword);
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getMessage());
            }
            
            // Hash and update password
            String passwordHash = PasswordUtils.hashPassword(newPassword);
            return vendorDAO.updatePassword(vendorId, passwordHash);
        }
        
        return false;
    }
    
    /**
     * Changes an admin's password.
     * 
     * @param adminId the admin ID
     * @param currentPassword the current password
     * @param newPassword the new password
     * @return true if password was changed
     */
    public boolean changeAdminPassword(Long adminId, String currentPassword, String newPassword) {
        Optional<Admin> adminOpt = adminDAO.findById(adminId);
        
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            
            // Verify current password
            if (!PasswordUtils.verifyPassword(currentPassword, admin.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            
            // Validate new password strength
            PasswordUtils.PasswordValidationResult validation = PasswordUtils.validatePassword(newPassword);
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getMessage());
            }
            
            // Hash and update password
            String passwordHash = PasswordUtils.hashPassword(newPassword);
            return adminDAO.updatePassword(adminId, passwordHash);
        }
        
        return false;
    }
    
    /**
     * Checks if a user is an admin.
     * 
     * @param username the username
     * @return true if admin exists
     */
    public boolean isAdmin(String username) {
        return adminDAO.findByUsername(username).isPresent();
    }
    
    /**
     * Validates email format.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
}
