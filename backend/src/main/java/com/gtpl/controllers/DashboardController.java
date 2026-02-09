package com.gtpl.controllers;

import com.gtpl.daos.*;
import com.gtpl.models.*;
import com.gtpl.services.NotificationService;
import com.gtpl.utils.SessionUtils;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Dashboard Controller Class
 * Handles dashboard data API endpoints for React components.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class DashboardController {
    
    private final ProjectDAO projectDAO;
    private final VendorDAO vendorDAO;
    private final DailyProgressDAO dailyProgressDAO;
    private final MaterialRequestDAO materialRequestDAO;
    private final NotificationService notificationService;
    
    public DashboardController() {
        this.projectDAO = new ProjectDAO();
        this.vendorDAO = new VendorDAO();
        this.dailyProgressDAO = new DailyProgressDAO();
        this.materialRequestDAO = new MaterialRequestDAO();
        this.notificationService = new NotificationService();
    }
    
    // ==================== Admin Dashboard API ====================
    
    public void getAdminDashboardData(Context ctx) {
        Map<String, Object> data = new HashMap<>();
        
        // Project statistics
        int[] projectStats = projectDAO.getProjectStats();
        data.put("totalProjects", projectStats[0]);
        data.put("assignedProjects", projectStats[1]);
        data.put("inProgressProjects", projectStats[2]);
        data.put("completedProjects", projectStats[3]);
        data.put("onHoldProjects", projectStats[4]);
        data.put("cancelledProjects", projectStats[5]);
        
        // Vendor statistics
        int[] vendorStats = vendorDAO.getVendorCounts();
        data.put("totalVendors", vendorStats[0]);
        data.put("activeVendors", vendorStats[1]);
        data.put("inactiveVendors", vendorStats[2]);
        
        // Material request statistics
        int[] requestStats = materialRequestDAO.getRequestStats();
        data.put("totalMaterialRequests", requestStats[0]);
        data.put("pendingMaterialRequests", requestStats[1]);
        data.put("approvedMaterialRequests", requestStats[2]);
        data.put("rejectedMaterialRequests", requestStats[3]);
        data.put("fulfilledMaterialRequests", requestStats[4]);
        
        // Overdue projects
        data.put("overdueProjects", projectDAO.getOverdueProjectsCount());
        
        // Recent projects
        data.put("recentProjects", projectDAO.findAll(0, 5));
        
        ctx.json(data);
    }
    
    // ==================== Vendor Dashboard API ====================
    
    public void getVendorDashboardData(Context ctx) {
        Long vendorId = SessionUtils.getUserId(ctx);
        Map<String, Object> data = new HashMap<>();
        
        // Get assigned projects
        List<Project> projects = projectDAO.findByVendorId(vendorId);
        
        // Project statistics
        int totalProjects = projects.size();
        int activeProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == Project.Status.IN_PROGRESS)
                .count();
        int completedProjects = (int) projects.stream()
                .filter(p -> p.getStatus() == Project.Status.COMPLETED)
                .count();
        
        data.put("totalProjects", totalProjects);
        data.put("activeProjects", activeProjects);
        data.put("completedProjects", completedProjects);
        
        // KM statistics
        BigDecimal totalKmAssigned = projects.stream()
                .map(Project::getTotalKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalKmCompleted = projects.stream()
                .map(Project::getCompletedKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalKmRemaining = totalKmAssigned.subtract(totalKmCompleted);
        
        data.put("totalKmAssigned", totalKmAssigned);
        data.put("totalKmCompleted", totalKmCompleted);
        data.put("totalKmRemaining", totalKmRemaining);
        
        // Progress percentage
        BigDecimal overallProgress = totalKmAssigned.compareTo(BigDecimal.ZERO) > 0
                ? totalKmCompleted.multiply(new BigDecimal("100")).divide(totalKmAssigned, 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
        data.put("overallProgress", overallProgress);
        
        // Material requests
        int pendingRequests = materialRequestDAO.getPendingCountByVendor(vendorId);
        data.put("pendingMaterialRequests", pendingRequests);
        
        // Unread notifications
        int unreadNotifications = notificationService.countUnreadNotifications(
            Notification.RecipientType.VENDOR, vendorId
        );
        data.put("unreadNotifications", unreadNotifications);
        
        // Active projects list
        data.put("activeProjectsList", projectDAO.findActiveByVendorId(vendorId));
        
        ctx.json(data);
    }
    
    // ==================== Charts API ====================
    
    public void getProjectProgressChartData(Context ctx) {
        List<Project> projects = projectDAO.findAll();
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Project project : projects) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", project.getProjectName());
            item.put("completed", project.getCompletedKm());
            item.put("remaining", project.getRemainingKm());
            item.put("total", project.getTotalKm());
            item.put("progress", project.getProgressPercentage());
            data.add(item);
        }
        
        ctx.json(data);
    }
    
    public void getVendorPerformanceChartData(Context ctx) {
        List<Vendor> vendors = vendorDAO.findAllActive();
        
        List<Map<String, Object>> data = new ArrayList<>();
        for (Vendor vendor : vendors) {
            List<Project> projects = projectDAO.findByVendorId(vendor.getId());
            
            BigDecimal totalKm = projects.stream()
                    .map(Project::getTotalKm)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal completedKm = projects.stream()
                    .map(Project::getCompletedKm)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal completionRate = totalKm.compareTo(BigDecimal.ZERO) > 0
                    ? completedKm.multiply(new BigDecimal("100")).divide(totalKm, 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;
            
            Map<String, Object> item = new HashMap<>();
            item.put("vendorName", vendor.getVendorName());
            item.put("companyName", vendor.getCompanyName());
            item.put("totalProjects", projects.size());
            item.put("completedProjects", projects.stream().filter(p -> p.getStatus() == Project.Status.COMPLETED).count());
            item.put("totalKm", totalKm);
            item.put("completedKm", completedKm);
            item.put("completionRate", completionRate);
            data.add(item);
        }
        
        ctx.json(data);
    }
    
    public void getMaterialUsageChartData(Context ctx) {
        // Get material usage from project_materials
        List<Map<String, Object>> data = new ArrayList<>();
        
        // This would require a more complex query in a real implementation
        // For now, return sample data structure
        Map<String, Object> item1 = new HashMap<>();
        item1.put("materialName", "HDPE Pipe 110mm");
        item1.put("assigned", 5000);
        item1.put("used", 3200);
        item1.put("remaining", 1800);
        data.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("materialName", "Fiber Optic Cable");
        item2.put("assigned", 3000);
        item2.put("used", 2100);
        item2.put("remaining", 900);
        data.add(item2);
        
        ctx.json(data);
    }
    
    public void getDailyTimelineChartData(Context ctx) {
        // Get daily progress for the last 30 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Group by date and sum KM completed
        List<DailyProgress> allProgress = dailyProgressDAO.findAll();
        
        Map<LocalDate, BigDecimal> dailyTotals = new TreeMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailyTotals.put(date, BigDecimal.ZERO);
        }
        
        for (DailyProgress progress : allProgress) {
            if (!progress.getWorkDate().isBefore(startDate) && !progress.getWorkDate().isAfter(endDate)) {
                dailyTotals.merge(progress.getWorkDate(), progress.getKmCompletedToday(), BigDecimal::add);
            }
        }
        
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyTotals.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey().format(DateTimeFormatter.ISO_DATE));
            item.put("kmCompleted", entry.getValue());
            data.add(item);
        }
        
        ctx.json(data);
    }
    
    // ==================== Widgets API ====================
    
    public void getVendorStatsWidget(Context ctx) {
        int[] vendorStats = vendorDAO.getVendorCounts();
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", vendorStats[0]);
        data.put("active", vendorStats[1]);
        data.put("inactive", vendorStats[2]);
        
        ctx.json(data);
    }
    
    public void getProjectSummaryWidget(Context ctx) {
        int[] projectStats = projectDAO.getProjectStats();
        
        Map<String, Object> data = new HashMap<>();
        data.put("total", projectStats[0]);
        data.put("assigned", projectStats[1]);
        data.put("inProgress", projectStats[2]);
        data.put("completed", projectStats[3]);
        data.put("onHold", projectStats[4]);
        data.put("overdue", projectDAO.getOverdueProjectsCount());
        
        ctx.json(data);
    }
    
    public void getNotificationsWidget(Context ctx) {
        Long userId = SessionUtils.getUserId(ctx);
        String role = SessionUtils.getRole(ctx);
        
        Notification.RecipientType recipientType = "ADMIN".equals(role) 
            ? Notification.RecipientType.ADMIN 
            : Notification.RecipientType.VENDOR;
        
        int unreadCount = notificationService.countUnreadNotifications(recipientType, userId);
        List<Notification> recentNotifications = notificationService.getUnreadNotifications(recipientType, userId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", unreadCount);
        data.put("recentNotifications", recentNotifications.size() > 5 ? recentNotifications.subList(0, 5) : recentNotifications);
        
        ctx.json(data);
    }
    
    public void getComplianceStatusWidget(Context ctx) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Long> missingUpdates = dailyProgressDAO.getProjectsWithMissingUpdates(yesterday);
        
        Map<String, Object> data = new HashMap<>();
        data.put("projectsWithMissingUpdates", missingUpdates.size());
        data.put("date", yesterday.format(DateTimeFormatter.ISO_DATE));
        
        ctx.json(data);
    }
}
