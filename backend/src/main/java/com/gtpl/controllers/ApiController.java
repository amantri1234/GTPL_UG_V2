package com.gtpl.controllers;

import com.gtpl.daos.*;
import com.gtpl.models.*;
import com.gtpl.utils.SessionUtils;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API Controller Class
 * Handles REST API endpoints for data retrieval.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class ApiController {
    
    private final ProjectDAO projectDAO;
    private final VendorDAO vendorDAO;
    private final MaterialDAO materialDAO;
    private final MaterialRequestDAO materialRequestDAO;
    private final DailyProgressDAO dailyProgressDAO;
    
    public ApiController() {
        this.projectDAO = new ProjectDAO();
        this.vendorDAO = new VendorDAO();
        this.materialDAO = new MaterialDAO();
        this.materialRequestDAO = new MaterialRequestDAO();
        this.dailyProgressDAO = new DailyProgressDAO();
    }
    
    // ==================== Projects API ====================
    
    public void getProjectsList(Context ctx) {
        String status = ctx.queryParam("status");
        List<Project> projects;
        
        if (status != null && !status.isEmpty()) {
            try {
                Project.Status projectStatus = Project.Status.valueOf(status.toUpperCase());
                projects = projectDAO.findByStatus(projectStatus);
            } catch (IllegalArgumentException e) {
                projects = projectDAO.findAll();
            }
        } else {
            projects = projectDAO.findAll();
        }
        
        ctx.json(projects);
    }
    
    public void getProjectDetails(Context ctx) {
        Long projectId = Long.parseLong(ctx.pathParam("id"));
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        
        if (projectOpt.isPresent()) {
            ctx.json(projectOpt.get());
        } else {
            ctx.status(404);
            ctx.json(new ErrorResponse("Project not found", 404));
        }
    }
    
    public void getProjectProgress(Context ctx) {
        Long projectId = Long.parseLong(ctx.pathParam("id"));
        Optional<Project> projectOpt = projectDAO.findById(projectId);
        
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            Map<String, Object> progress = new HashMap<>();
            progress.put("projectId", project.getId());
            progress.put("projectName", project.getProjectName());
            progress.put("totalKm", project.getTotalKm());
            progress.put("completedKm", project.getCompletedKm());
            progress.put("remainingKm", project.getRemainingKm());
            progress.put("progressPercentage", project.getProgressPercentage());
            progress.put("status", project.getStatus());
            
            // Get daily progress history
            List<DailyProgress> dailyProgress = dailyProgressDAO.findByProjectId(projectId);
            progress.put("dailyProgress", dailyProgress);
            
            ctx.json(progress);
        } else {
            ctx.status(404);
            ctx.json(new ErrorResponse("Project not found", 404));
        }
    }
    
    // ==================== Vendors API ====================
    
    public void getVendorsList(Context ctx) {
        String active = ctx.queryParam("active");
        List<Vendor> vendors;
        
        if ("true".equalsIgnoreCase(active)) {
            vendors = vendorDAO.findAllActive();
        } else {
            vendors = vendorDAO.findAll();
        }
        
        ctx.json(vendors);
    }
    
    public void getVendorDetails(Context ctx) {
        Long vendorId = Long.parseLong(ctx.pathParam("id"));
        Optional<Vendor> vendorOpt = vendorDAO.findById(vendorId);
        
        if (vendorOpt.isPresent()) {
            ctx.json(vendorOpt.get().toSafeCopy());
        } else {
            ctx.status(404);
            ctx.json(new ErrorResponse("Vendor not found", 404));
        }
    }
    
    // ==================== Materials API ====================
    
    public void getMaterialsList(Context ctx) {
        String active = ctx.queryParam("active");
        List<Material> materials;
        
        if ("true".equalsIgnoreCase(active)) {
            materials = materialDAO.findAllActive();
        } else {
            materials = materialDAO.findAll();
        }
        
        ctx.json(materials);
    }
    
    // ==================== Material Requests API ====================
    
    public void getMaterialRequests(Context ctx) {
        String status = ctx.queryParam("status");
        String vendorId = ctx.queryParam("vendorId");
        List<MaterialRequest> requests;
        
        if (vendorId != null && !vendorId.isEmpty()) {
            requests = materialRequestDAO.findByVendorId(Long.parseLong(vendorId));
        } else if (status != null && !status.isEmpty()) {
            try {
                MaterialRequest.Status requestStatus = MaterialRequest.Status.valueOf(status.toUpperCase());
                requests = materialRequestDAO.findByStatus(requestStatus);
            } catch (IllegalArgumentException e) {
                requests = materialRequestDAO.findAll();
            }
        } else {
            requests = materialRequestDAO.findAll();
        }
        
        ctx.json(requests);
    }
    
    // ==================== Daily Progress API ====================
    
    public void getDailyProgress(Context ctx) {
        String projectId = ctx.queryParam("projectId");
        String vendorId = ctx.queryParam("vendorId");
        List<DailyProgress> progress;
        
        if (projectId != null && !projectId.isEmpty()) {
            progress = dailyProgressDAO.findByProjectId(Long.parseLong(projectId));
        } else if (vendorId != null && !vendorId.isEmpty()) {
            progress = dailyProgressDAO.findByVendorId(Long.parseLong(vendorId));
        } else {
            progress = dailyProgressDAO.findAll();
        }
        
        ctx.json(progress);
    }
    
    public void createDailyProgress(Context ctx) {
        // This would be implemented with proper request body parsing
        // For now, return a not implemented response
        ctx.status(501);
        ctx.json(new ErrorResponse("Not implemented", 501));
    }
    
    // ==================== Error Response ====================
    
    public static class ErrorResponse {
        public final String error;
        public final int code;
        public final long timestamp;
        
        public ErrorResponse(String error, int code) {
            this.error = error;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
