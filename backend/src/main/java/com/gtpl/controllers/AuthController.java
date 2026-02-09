package com.gtpl.controllers;

import com.gtpl.models.Admin;
import com.gtpl.models.Vendor;
import com.gtpl.services.AuthService;
import com.gtpl.utils.SessionUtils;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller Class
 * Handles authentication-related HTTP requests.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController() {
        this.authService = new AuthService();
    }
    
    /**
     * Shows the login page.
     */
    public void showLoginPage(Context ctx) {
        // If already logged in, redirect to appropriate dashboard
        if (SessionUtils.isLoggedIn(ctx)) {
            if (SessionUtils.isAdmin(ctx)) {
                ctx.redirect("/admin/dashboard");
            } else {
                ctx.redirect("/vendor/dashboard");
            }
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Login - GTPL_UG Management System");
        
        // Check for error message
        String error = ctx.sessionAttribute("loginError");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("loginError");
        }
        
        ctx.render("auth/login.html", model);
    }
    
    /**
     * Handles login form submission.
     */
    public void handleLogin(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            ctx.sessionAttribute("loginError", "Username and password are required");
            ctx.redirect("/login");
            return;
        }
        
        // Check if it's an admin login
        Optional<Admin> adminOpt = authService.authenticateAdmin(username, password);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            SessionUtils.createAdminSession(ctx, admin.getId(), admin.getUsername(), admin.getFullName());
            
            // Redirect to original destination or dashboard
            String redirect = ctx.sessionAttribute("redirectAfterLogin");
            ctx.consumeSessionAttribute("redirectAfterLogin");
            ctx.redirect(redirect != null ? redirect : "/admin/dashboard");
            return;
        }
        
        // Check if it's a vendor login (using email as username)
        Optional<Vendor> vendorOpt = authService.authenticateVendor(username, password);
        if (vendorOpt.isPresent()) {
            Vendor vendor = vendorOpt.get();
            SessionUtils.createVendorSession(ctx, vendor.getId(), vendor.getEmail(), vendor.getVendorName());
            
            // Redirect to original destination or dashboard
            String redirect = ctx.sessionAttribute("redirectAfterLogin");
            ctx.consumeSessionAttribute("redirectAfterLogin");
            ctx.redirect(redirect != null ? redirect : "/vendor/dashboard");
            return;
        }
        
        // Authentication failed
        ctx.sessionAttribute("loginError", "Invalid username or password");
        ctx.redirect("/login");
    }
    
    /**
     * Handles logout.
     */
    public void handleLogout(Context ctx) {
        SessionUtils.invalidateSession(ctx);
        ctx.redirect("/login");
    }
    
    /**
     * Shows the signup page for vendors.
     */
    public void showSignupPage(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Vendor Signup - GTPL_UG Management System");
        
        // Check for error or success message
        String error = ctx.sessionAttribute("signupError");
        String success = ctx.sessionAttribute("signupSuccess");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("signupError");
        }
        if (success != null) {
            model.put("success", success);
            ctx.consumeSessionAttribute("signupSuccess");
        }
        
        ctx.render("auth/signup.html", model);
    }
    
    /**
     * Handles vendor signup form submission.
     */
    public void handleSignup(Context ctx) {
        String vendorName = ctx.formParam("vendorName");
        String companyName = ctx.formParam("companyName");
        String email = ctx.formParam("email");
        String phoneNumber = ctx.formParam("phoneNumber");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirmPassword");
        
        // Validate input
        if (vendorName == null || vendorName.trim().isEmpty() ||
            companyName == null || companyName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phoneNumber == null || phoneNumber.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            ctx.sessionAttribute("signupError", "All fields are required");
            ctx.redirect("/signup");
            return;
        }
        
        // Check password match
        if (!password.equals(confirmPassword)) {
            ctx.sessionAttribute("signupError", "Passwords do not match");
            ctx.redirect("/signup");
            return;
        }
        
        try {
            Long vendorId = authService.registerVendor(vendorName, companyName, email, phoneNumber, password);
            
            if (vendorId != null) {
                ctx.sessionAttribute("signupSuccess", "Registration successful! Please login with your email and password.");
                ctx.redirect("/login");
            } else {
                ctx.sessionAttribute("signupError", "Registration failed. Please try again.");
                ctx.redirect("/signup");
            }
        } catch (IllegalArgumentException e) {
            ctx.sessionAttribute("signupError", e.getMessage());
            ctx.redirect("/signup");
        }
    }
    
    /**
     * Shows the forgot password page.
     */
    public void showForgotPasswordPage(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Forgot Password - GTPL_UG Management System");
        
        String error = ctx.sessionAttribute("forgotPasswordError");
        String success = ctx.sessionAttribute("forgotPasswordSuccess");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("forgotPasswordError");
        }
        if (success != null) {
            model.put("success", success);
            ctx.consumeSessionAttribute("forgotPasswordSuccess");
        }
        
        ctx.render("auth/forgot-password.html", model);
    }
    
    /**
     * Handles forgot password form submission.
     */
    public void handleForgotPassword(Context ctx) {
        String email = ctx.formParam("email");
        
        if (email == null || email.trim().isEmpty()) {
            ctx.sessionAttribute("forgotPasswordError", "Email is required");
            ctx.redirect("/forgot-password");
            return;
        }
        
        // Initiate password reset
        authService.initiatePasswordReset(email);
        
        // Always show success message (security best practice)
        ctx.sessionAttribute("forgotPasswordSuccess", 
            "If an account exists with this email, you will receive password reset instructions.");
        ctx.redirect("/forgot-password");
    }
    
    /**
     * Shows the reset password page.
     */
    public void showResetPasswordPage(Context ctx) {
        String token = ctx.queryParam("token");
        
        if (token == null || token.trim().isEmpty()) {
            ctx.sessionAttribute("loginError", "Invalid or expired reset token");
            ctx.redirect("/login");
            return;
        }
        
        // Validate token
        Optional<Vendor> vendorOpt = authService.validateResetToken(token);
        if (!vendorOpt.isPresent()) {
            ctx.sessionAttribute("loginError", "Invalid or expired reset token");
            ctx.redirect("/login");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Reset Password - GTPL_UG Management System");
        model.put("token", token);
        
        String error = ctx.sessionAttribute("resetPasswordError");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("resetPasswordError");
        }
        
        ctx.render("auth/reset-password.html", model);
    }
    
    /**
     * Handles reset password form submission.
     */
    public void handleResetPassword(Context ctx) {
        String token = ctx.formParam("token");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirmPassword");
        
        if (token == null || token.trim().isEmpty()) {
            ctx.sessionAttribute("loginError", "Invalid or expired reset token");
            ctx.redirect("/login");
            return;
        }
        
        if (password == null || password.isEmpty() || !password.equals(confirmPassword)) {
            ctx.sessionAttribute("resetPasswordError", "Passwords do not match");
            ctx.redirect("/reset-password?token=" + token);
            return;
        }
        
        try {
            boolean success = authService.resetPassword(token, password);
            
            if (success) {
                ctx.sessionAttribute("signupSuccess", "Password reset successful! Please login with your new password.");
                ctx.redirect("/login");
            } else {
                ctx.sessionAttribute("loginError", "Invalid or expired reset token");
                ctx.redirect("/login");
            }
        } catch (IllegalArgumentException e) {
            ctx.sessionAttribute("resetPasswordError", e.getMessage());
            ctx.redirect("/reset-password?token=" + token);
        }
    }
}
