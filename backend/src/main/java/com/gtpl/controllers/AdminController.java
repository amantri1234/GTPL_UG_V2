package com.gtpl.controllers;

import com.gtpl.models.*;
import com.gtpl.services.AdminService;
import com.gtpl.utils.SessionUtils;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin Controller Class
 * Handles admin-related HTTP requests.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class AdminController {
    
    private final AdminService adminService;
    
    public AdminController() {
        this.adminService = new AdminService();
    }
    
    // ==================== Dashboard ====================
    
    public void showDashboard(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Admin Dashboard - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        // Get dashboard data
        Map<String, Object> dashboardData = adminService.getDashboardData();
        model.putAll(dashboardData);
        
        ctx.render("admin/dashboard.html", model);
    }
    
    // ==================== Projects ====================
    
    public void showProjectsList(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Projects - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        List<Project> projects = adminService.getAllProjects();
        model.put("projects", projects);
        
        ctx.render("admin/projects.html", model);
    }
    
    public void showCreateProjectForm(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Create Project - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        // Get active vendors for dropdown
        List<Vendor> vendors = adminService.getActiveVendors();
        model.put("vendors", vendors);
        
        String error = ctx.sessionAttribute("projectError");
        if (error != null) {
            model.put("error", error);
            ctx.consumeSessionAttribute("projectError");
        }
        
        ctx.render("admin/create-project.html", model);
    }
    
    public void handleCreateProject(Context ctx) {
        try {
            String projectName = ctx.formParam("projectName");
            String workDescription = ctx.formParam("workDescription");
            String startingPoint = ctx.formParam("startingPoint");
            String endingPoint = ctx.formParam("endingPoint");
            String totalKmStr = ctx.formParam("totalKm");
            String expectedStartDateStr = ctx.formParam("expectedStartDate");
            String expectedEndDateStr = ctx.formParam("expectedEndDate");
            String priorityStr = ctx.formParam("priority");
            String vendorIdStr = ctx.formParam("vendorId");
            
            BigDecimal totalKm = new BigDecimal(totalKmStr);
            LocalDate expectedStartDate = LocalDate.parse(expectedStartDateStr);
            LocalDate expectedEndDate = LocalDate.parse(expectedEndDateStr);
            Project.Priority priority = Project.Priority.valueOf(priorityStr);
            Long adminId = SessionUtils.getUserId(ctx);
            
            Long projectId = adminService.createProject(projectName, workDescription, startingPoint,
                    endingPoint, totalKm, expectedStartDate, expectedEndDate, priority, adminId);
            
            if (projectId != null && vendorIdStr != null && !vendorIdStr.isEmpty()) {
                // Assign to vendor if selected
                Long vendorId = Long.parseLong(vendorIdStr);
                adminService.assignProjectToVendor(projectId, vendorId);
            }
            
            ctx.redirect("/admin/projects");
        } catch (Exception e) {
            ctx.sessionAttribute("projectError", e.getMessage());
            ctx.redirect("/admin/projects/create");
        }
    }
    
    public void showProjectDetails(Context ctx) {
        Long projectId = Long.parseLong(ctx.pathParam("id"));
        Optional<Project> projectOpt = adminService.getProject(projectId);
        
        if (!projectOpt.isPresent()) {
            ctx.status(404);
            ctx.render("error/404.html");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Project Details - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        model.put("project", projectOpt.get());
        
        ctx.render("admin/project-details.html", model);
    }
    
    public void handleUpdateProject(Context ctx) {
        // Implementation for updating project
        ctx.redirect("/admin/projects");
    }
    
    public void handleCancelProject(Context ctx) {
        Long projectId = Long.parseLong(ctx.pathParam("id"));
        adminService.cancelProject(projectId);
        ctx.redirect("/admin/projects");
    }
    
    // ==================== Vendors ====================
    
    public void showVendorsList(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Vendors - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        List<Vendor> vendors = adminService.getAllVendors();
        model.put("vendors", vendors);
        
        ctx.render("admin/vendors.html", model);
    }
    
    public void showVendorDetails(Context ctx) {
        Long vendorId = Long.parseLong(ctx.pathParam("id"));
        Optional<Vendor> vendorOpt = adminService.getVendor(vendorId);
        
        if (!vendorOpt.isPresent()) {
            ctx.status(404);
            ctx.render("error/404.html");
            return;
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Vendor Details - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        model.put("vendor", vendorOpt.get());
        
        ctx.render("admin/vendor-details.html", model);
    }
    
    public void handleToggleVendorStatus(Context ctx) {
        Long vendorId = Long.parseLong(ctx.pathParam("id"));
        adminService.toggleVendorStatus(vendorId);
        ctx.redirect("/admin/vendors");
    }
    
    public void showVendorPerformance(Context ctx) {
        Long vendorId = Long.parseLong(ctx.pathParam("id"));
        Map<String, Object> performance = adminService.getVendorPerformance(vendorId);
        
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Vendor Performance - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        model.putAll(performance);
        
        ctx.render("admin/vendor-performance.html", model);
    }
    
    // ==================== Materials ====================
    
    public void showMaterialsList(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Materials - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        List<Material> materials = adminService.getAllMaterials();
        model.put("materials", materials);
        
        ctx.render("admin/materials.html", model);
    }
    
    public void handleCreateMaterial(Context ctx) {
        try {
            String materialCode = ctx.formParam("materialCode");
            String materialName = ctx.formParam("materialName");
            String description = ctx.formParam("description");
            String unitStr = ctx.formParam("unit");
            String category = ctx.formParam("category");
            
            Material.Unit unit = Material.Unit.valueOf(unitStr);
            adminService.createMaterial(materialCode, materialName, description, unit, category);
            
            ctx.redirect("/admin/materials");
        } catch (Exception e) {
            ctx.sessionAttribute("materialError", e.getMessage());
            ctx.redirect("/admin/materials");
        }
    }
    
    public void handleUpdateMaterial(Context ctx) {
        // Implementation for updating material
        ctx.redirect("/admin/materials");
    }
    
    // ==================== Material Requests ====================
    
    public void showMaterialRequests(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Material Requests - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        List<MaterialRequest> requests = adminService.getAllMaterialRequests();
        model.put("requests", requests);
        
        ctx.render("admin/material-requests.html", model);
    }
    
    public void handleApproveMaterialRequest(Context ctx) {
        Long requestId = Long.parseLong(ctx.pathParam("id"));
        String approvedQuantityStr = ctx.formParam("approvedQuantity");
        BigDecimal approvedQuantity = new BigDecimal(approvedQuantityStr);
        Long adminId = SessionUtils.getUserId(ctx);
        
        adminService.approveMaterialRequest(requestId, approvedQuantity, adminId);
        ctx.redirect("/admin/material-requests");
    }
    
    public void handleRejectMaterialRequest(Context ctx) {
        Long requestId = Long.parseLong(ctx.pathParam("id"));
        String reason = ctx.formParam("reason");
        Long adminId = SessionUtils.getUserId(ctx);
        
        adminService.rejectMaterialRequest(requestId, reason, adminId);
        ctx.redirect("/admin/material-requests");
    }
    
    // ==================== Daily Progress ====================
    
    public void showDailyProgress(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Daily Progress - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        List<DailyProgress> progressList = adminService.getAllDailyProgress();
        model.put("dailyProgress", progressList);
        
        ctx.render("admin/daily-progress.html", model);
    }
    
    public void showComplianceStatus(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Compliance Status - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        List<Map<String, Object>> compliance = adminService.getComplianceStatus();
        model.put("compliance", compliance);
        
        ctx.render("admin/compliance.html", model);
    }
    
    // ==================== Reports ====================
    
    public void showReports(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Reports - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        ctx.render("admin/reports.html", model);
    }
    
    // ==================== Profile ====================
    
    public void showProfile(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Profile - GTPL_UG Management System");
        model.put("userName", SessionUtils.getFullName(ctx));
        model.put("userRole", "Administrator");
        
        Long adminId = SessionUtils.getUserId(ctx);
        Optional<Admin> adminOpt = adminService.getAdmin(adminId);
        adminOpt.ifPresent(admin -> model.put("admin", admin));
        
        ctx.render("admin/profile.html", model);
    }
    
    public void handleUpdateProfile(Context ctx) {
        Long adminId = SessionUtils.getUserId(ctx);
        String fullName = ctx.formParam("fullName");
        String email = ctx.formParam("email");
        String phone = ctx.formParam("phone");
        
        adminService.updateProfile(adminId, fullName, email, phone);
        SessionUtils.updateFullName(ctx, fullName);
        
        ctx.redirect("/admin/profile");
    }
    
    public void handleChangePassword(Context ctx) {
        // Implementation for changing password
        ctx.redirect("/admin/profile");
    }
}
