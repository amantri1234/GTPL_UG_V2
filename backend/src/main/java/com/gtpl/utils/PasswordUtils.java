package com.gtpl.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Utility Class
 * Provides password hashing, verification, and token generation utilities.
 * Uses BCrypt for secure password hashing.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class PasswordUtils {
    
    // BCrypt workload (log rounds) - higher = more secure but slower
    private static final int BCRYPT_ROUNDS = 12;
    
    // Secure random for token generation
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // Token length for password reset
    private static final int RESET_TOKEN_LENGTH = 32;
    
    /**
     * Private constructor to prevent instantiation
     */
    private PasswordUtils() {
    }
    
    /**
     * Hashes a plain text password using BCrypt.
     * 
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    /**
     * Verifies a plain text password against a hashed password.
     * 
     * @param plainPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }
    
    /**
     * Generates a secure random token for password reset.
     * 
     * @return a URL-safe base64 encoded random token
     */
    public static String generateResetToken() {
        byte[] tokenBytes = new byte[RESET_TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Generates a secure random password.
     * 
     * @param length the length of the password
     * @return a random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8; // Minimum length
        }
        
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;
        
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one of each type
        password.append(upper.charAt(secureRandom.nextInt(upper.length())));
        password.append(lower.charAt(secureRandom.nextInt(lower.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        password.append(special.charAt(secureRandom.nextInt(special.length())));
        
        // Fill remaining with random characters
        for (int i = 4; i < length; i++) {
            password.append(all.charAt(secureRandom.nextInt(all.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Validates password strength.
     * 
     * @param password the password to validate
     * @return PasswordValidationResult containing validation status and message
     */
    public static PasswordValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password is required");
        }
        
        if (password.length() < 8) {
            return new PasswordValidationResult(false, "Password must be at least 8 characters long");
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*".indexOf(c) >= 0) hasSpecial = true;
        }
        
        if (!hasUpper) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter");
        }
        
        if (!hasLower) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter");
        }
        
        if (!hasDigit) {
            return new PasswordValidationResult(false, "Password must contain at least one digit");
        }
        
        if (!hasSpecial) {
            return new PasswordValidationResult(false, "Password must contain at least one special character (!@#$%^&*)");
        }
        
        return new PasswordValidationResult(true, "Password is strong");
    }
    
    /**
     * Result class for password validation
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;
        
        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
