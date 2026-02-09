package com.gtpl.controllers;

import com.gtpl.models.*;
import com.gtpl.services.VendorService;
import com.gtpl.utils.SessionUtils;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Vendor Controller Class
 * Handles vendor-related HTTP requests.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class VendorController {
    
    private final VendorService vendorService;
    
    public VendorController() {
        this.vendorService = new VendorService();
    }
    
    // ==================== Dashboard ====================
    
    public void showDashboard(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Vendor Dashboard - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        // Get dashboard data
        Map<String, Object> dashboardData = vendorService.getDashboardData(vendorId);
        model.putAll(dashboardData);
        
        ctx.render("vendor/dashboard.html", model);
    }
    
    // ==================== Projects ====================
    
    public void showMyProjects(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "My Projects - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        List<Project> projects = vendorService.getMyProjects(vendorId);
        model.put("projects", projects);
        
        ctx.render("vendor/projects.html", model);
    }
    
    public void showProjectDetails(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        Long projectId = Long.parseLong(ctx.pathParam("id"));
        
        Optional<Project> projectOpt = vendorService.getProjectDetails(projectId, vendorId);
        
        if (!projectOpt.isPresent()) {
            ctx.status(404);
            ctx.render("error/404.html");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Project Details - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        model.put("project", projectOpt.get());
        
        // Get daily progress for this project
        List<DailyProgress> progressList = vendorService.getProjectDailyProgress(projectId, vendorId);
        model.put("dailyProgress", progressList);
        
        ctx.render("vendor/project-details.html", model);
    }
    
    // ==================== Daily Reporting ====================
    
    public void showDailyReportForm(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Submit Daily Report - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        // Get active projects for dropdown
        List<Project> projects = vendorService.getActiveProjects(vendorId);
        model.put("projects", projects);
        
        String error = ctx.sessionAttribute("dailyReportError");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("dailyReportError");
        }
        
        ctx.render("vendor/daily-report.html", model);
    }
    
    public void handleSubmitDailyReport(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        try {
            Long projectId = Long.parseLong(ctx.formParam("projectId"));
            String workDateStr = ctx.formParam("workDate");
            String fromPoint = ctx.formParam("fromPoint");
            String toPoint = ctx.formParam("toPoint");
            String kmCompletedTodayStr = ctx.formParam("kmCompletedToday");
            String remainingEstimatedWorkStr = ctx.formParam("remainingEstimatedWork");
            String workDescription = ctx.formParam("workDescription");
            String remarks = ctx.formParam("remarks");
            
            LocalDate workDate = LocalDate.parse(workDateStr);
            BigDecimal kmCompletedToday = new BigDecimal(kmCompletedTodayStr);
            BigDecimal remainingEstimatedWork = new BigDecimal(remainingEstimatedWorkStr);
            
            vendorService.submitDailyReport(projectId, vendorId, workDate, fromPoint, toPoint,
                    kmCompletedToday, remainingEstimatedWork, workDescription, remarks);
            
            ctx.redirect("/vendor/dashboard");
        } catch (Exception e) {
            ctx.sessionAttribute("dailyReportError", e.getMessage());
            ctx.redirect("/vendor/daily-report");
        }
    }
    
    public void showDailyReportHistory(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Daily Report History - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        List<DailyProgress> progressList = vendorService.getDailyProgressHistory(vendorId);
        model.put("dailyProgress", progressList);
        
        ctx.render("vendor/daily-report-history.html", model);
    }
    
    // ==================== Material Requests ====================
    
    public void showMyMaterialRequests(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "My Material Requests - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        List<MaterialRequest> requests = vendorService.getMyMaterialRequests(vendorId);
        model.put("requests", requests);
        
        ctx.render("vendor/material-requests.html", model);
    }
    
    public void showCreateMaterialRequestForm(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Create Material Request - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        // Get active projects for dropdown
        List<Project> projects = vendorService.getActiveProjects(vendorId);
        model.put("projects", projects);
        
        // Get available materials
        List<Material> materials = vendorService.getAvailableMaterials();
        model.put("materials", materials);
        
        String error = ctx.sessionAttribute("materialRequestError");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("materialRequestError");
        }
        
        ctx.render("vendor/create-material-request.html", model);
    }
    
    public void handleCreateMaterialRequest(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        try {
            Long projectId = Long.parseLong(ctx.formParam("projectId"));
            Long materialId = Long.parseLong(ctx.formParam("materialId"));
            String quantityRequiredStr = ctx.formParam("quantityRequired");
            String reason = ctx.formParam("reason");
            String urgencyStr = ctx.formParam("urgency");
            
            BigDecimal quantityRequired = new BigDecimal(quantityRequiredStr);
            MaterialRequest.Urgency urgency = MaterialRequest.Urgency.valueOf(urgencyStr);
            
            vendorService.createMaterialRequest(projectId, vendorId, materialId, quantityRequired, reason, urgency);
            
            ctx.redirect("/vendor/material-requests");
        } catch (Exception e) {
            ctx.sessionAttribute("materialRequestError", e.getMessage());
            ctx.redirect("/vendor/material-requests/create");
        }
    }
    
    // ==================== Notifications ====================
    
    public void showNotifications(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Notifications - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        List<Notification> notifications = vendorService.getNotifications(vendorId);
        model.put("notifications", notifications);
        
        ctx.render("vendor/notifications.html", model);
    }
    
    public void handleMarkNotificationRead(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        Long notificationId = Long.parseLong(ctx.pathParam("id"));
        
        vendorService.markNotificationAsRead(notificationId, vendorId);
        ctx.redirect("/vendor/notifications");
    }
    
    public void handleMarkAllNotificationsRead(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        vendorService.markAllNotificationsAsRead(vendorId);
        ctx.redirect("/vendor/notifications");
    }
    
    // ==================== Profile ====================
    
    public void showProfile(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Profile - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Vendor");
        
        Optional<Vendor> vendorOpt = vendorService.getVendor(vendorId);
        vendorOpt.ifPresent(vendor -> model.put("vendor", vendor));
        
        ctx.render("vendor/profile.html", model);
    }
    
    public void handleUpdateProfile(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        
        String vendorName = ctx.formParam("vendorName");
        String companyName = ctx.formParam("companyName");
        String email = ctx.formParam("email");
        String phoneNumber = ctx.formParam("phoneNumber");
        String address = ctx.formParam("address");
        String city = ctx.formParam("city");
        String state = ctx.formParam("state");
        String pincode = ctx.formParam("pincode");
        String gstNumber = ctx.formParam("gstNumber");
        
        vendorService.updateProfile(vendorId, vendorName, companyName, email, phoneNumber,
                address, city, state, pincode, gstNumber);
        
        SessionUtils.updateFullName(ctx, vendorName);
        
        ctx.redirect("/vendor/profile");
    }
    
    public void handleChangePassword(Context ctx) {
        // Implementation for changing password
        ctx.redirect("/vendor/profile");
    }
    
    // ==================== Password Reset (Vendor Only) ====================
    
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
    
    public void handleForgotPassword(Context ctx) {
        String email = ctx.formParam("email");
        
        if (email == null || email.trim().isEmpty()) {
            ctx.sessionAttribute("forgotPasswordError", "Email is required");
            ctx.redirect("/vendor/forgot-password");
            return;
        }
        
        String token = vendorService.initiatePasswordReset(email);
        
        if (token != null) {
            // In a real system, send email with reset link
            // For demo, we'll show the token in the success message
            ctx.sessionAttribute("forgotPasswordSuccess", 
                "Password reset initiated. Please check your email for instructions.");
        } else {
            // Still show success for security
            ctx.sessionAttribute("forgotPasswordSuccess", 
                "If an account exists with this email, you will receive password reset instructions.");
        }
        
        ctx.redirect("/vendor/forgot-password");
    }
}
