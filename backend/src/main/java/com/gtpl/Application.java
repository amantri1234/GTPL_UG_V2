package com.gtpl;

import com.gtpl.controllers.*;
import com.gtpl.middleware.AuthMiddleware;
import com.gtpl.utils.DatabaseConfig;
import com.gtpl.utils.ViewEngineConfig;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

/**
 * GTPL_UG Management System - Main Application Class
 * 
 * Enterprise-grade UG (Underground) infrastructure management platform
 * for managing vendors, work assignments, progress tracking, and material management.
 * 
 * Technology Stack:
 * - Java 17
 * - Javalin 5.x (Web Framework)
 * - Thymeleaf (Server-side templating)
 * - MySQL (Database)
 * - JDBC (Data access)
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class Application {
    
    // Configuration constants
    private static final int DEFAULT_PORT = 8080;
    private static final String STATIC_FILES_PATH = "/static";
    
    public static void main(String[] args) {
        // Initialize database connection pool
        DatabaseConfig.initialize();
        
        // Create and configure Javalin application
        Javalin app = createApplication();
        
        // Configure routes and middleware
        configureApplication(app);
        
        // Start the server
        int port = getPort(args);
        app.start(port);
        
        System.out.println("=================================================");
        System.out.println("  GTPL_UG Management System Started Successfully");
        System.out.println("  Version: 1.0.0");
        System.out.println("  Port: " + port);
        System.out.println("  URL: http://localhost:" + port);
        System.out.println("=================================================");
        
        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down GTPL_UG Management System...");
            DatabaseConfig.close();
        }));
    }
    
    /**
     * Creates and configures the Javalin application instance.
     */
    private static Javalin createApplication() {
        return Javalin.create(config -> {
            // Static files configuration
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/static";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = true;
            });
            
            // Template engine configuration
            config.fileRenderer(new JavalinThymeleaf(ViewEngineConfig.getTemplateEngine()));
            
            // Request/Response configuration
            config.http.defaultContentType = "text/html; charset=UTF-8";
            config.http.prefer405over404 = true;
            
            // Development configuration
            config.plugins.enableDevLogging();
            
            // CORS configuration for API endpoints
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(corsRule -> {
                    corsRule.allowHost("http://localhost:3000"); // React dev server
                    corsRule.allowHost("http://localhost:8080");
                });
            });
        });
    }
    
    /**
     * Configures routes, middleware, and controllers.
     */
    private static void configureApplication(Javalin app) {
        // Initialize middleware
        AuthMiddleware authMiddleware = new AuthMiddleware();
        
        // Initialize controllers
        AuthController authController = new AuthController();
        AdminController adminController = new AdminController();
        VendorController vendorController = new VendorController();
        ApiController apiController = new ApiController();
        DashboardController dashboardController = new DashboardController();
        
        // =====================================================
        // PUBLIC ROUTES (No authentication required)
        // =====================================================
        
        // Home and info routes
        app.get("/", ctx -> ctx.redirect("/login"));
        app.get("/login", authController::showLoginPage);
        app.post("/login", authController::handleLogin);
        app.get("/logout", authController::handleLogout);
        
        // Health check endpoint for Railway
        app.get("/health", ctx -> {
            ctx.json(new HealthResponse("UP", "1.0.0", System.currentTimeMillis()));
        });
        
        // Vendor registration (public)
        app.get("/signup", authController::showSignupPage);
        app.post("/signup", authController::handleSignup);
        
        // Password reset (vendor only)
        app.get("/forgot-password", authController::showForgotPasswordPage);
        app.post("/forgot-password", authController::handleForgotPassword);
        app.get("/reset-password", authController::showResetPasswordPage);
        app.post("/reset-password", authController::handleResetPassword);
        
        // =====================================================
        // AUTHENTICATED ROUTES (Login required)
        // =====================================================
        app.before("/admin/*", authMiddleware::requireAdmin);
        app.before("/vendor/*", authMiddleware::requireVendor);
        app.before("/api/*", authMiddleware::requireAuthenticated);
        
        // =====================================================
        // ADMIN ROUTES
        // =====================================================
        
        // Admin Dashboard
        app.get("/admin/dashboard", adminController::showDashboard);
        
        // Project Management
        app.get("/admin/projects", adminController::showProjectsList);
        app.get("/admin/projects/create", adminController::showCreateProjectForm);
        app.post("/admin/projects/create", adminController::handleCreateProject);
        app.get("/admin/projects/{id}", adminController::showProjectDetails);
        app.post("/admin/projects/{id}/update", adminController::handleUpdateProject);
        app.post("/admin/projects/{id}/cancel", adminController::handleCancelProject);
        
        // Vendor Management
        app.get("/admin/vendors", adminController::showVendorsList);
        app.get("/admin/vendors/{id}", adminController::showVendorDetails);
        app.post("/admin/vendors/{id}/toggle-status", adminController::handleToggleVendorStatus);
        app.get("/admin/vendors/{id}/performance", adminController::showVendorPerformance);
        
        // Material Management
        app.get("/admin/materials", adminController::showMaterialsList);
        app.post("/admin/materials/create", adminController::handleCreateMaterial);
        app.post("/admin/materials/{id}/update", adminController::handleUpdateMaterial);
        
        // Material Requests
        app.get("/admin/material-requests", adminController::showMaterialRequests);
        app.post("/admin/material-requests/{id}/approve", adminController::handleApproveMaterialRequest);
        app.post("/admin/material-requests/{id}/reject", adminController::handleRejectMaterialRequest);
        
        // Daily Progress Monitoring
        app.get("/admin/daily-progress", adminController::showDailyProgress);
        app.get("/admin/compliance", adminController::showComplianceStatus);
        
        // Reports & Analytics
        app.get("/admin/reports", adminController::showReports);
        
        // Admin Profile
        app.get("/admin/profile", adminController::showProfile);
        app.post("/admin/profile/update", adminController::handleUpdateProfile);
        app.post("/admin/profile/change-password", adminController::handleChangePassword);
        
        // =====================================================
        // VENDOR ROUTES
        // =====================================================
        
        // Vendor Dashboard
        app.get("/vendor/dashboard", vendorController::showDashboard);
        
        // My Projects
        app.get("/vendor/projects", vendorController::showMyProjects);
        app.get("/vendor/projects/{id}", vendorController::showProjectDetails);
        
        // Daily Reporting
        app.get("/vendor/daily-report", vendorController::showDailyReportForm);
        app.post("/vendor/daily-report/submit", vendorController::handleSubmitDailyReport);
        app.get("/vendor/daily-report/history", vendorController::showDailyReportHistory);
        
        // Material Requests
        app.get("/vendor/material-requests", vendorController::showMyMaterialRequests);
        app.get("/vendor/material-requests/create", vendorController::showCreateMaterialRequestForm);
        app.post("/vendor/material-requests/create", vendorController::handleCreateMaterialRequest);
        
        // Notifications
        app.get("/vendor/notifications", vendorController::showNotifications);
        app.post("/vendor/notifications/{id}/mark-read", vendorController::handleMarkNotificationRead);
        app.post("/vendor/notifications/mark-all-read", vendorController::handleMarkAllNotificationsRead);
        
        // Vendor Profile
        app.get("/vendor/profile", vendorController::showProfile);
        app.post("/vendor/profile/update", vendorController::handleUpdateProfile);
        app.post("/vendor/profile/change-password", vendorController::handleChangePassword);
        
        // Password Reset (Vendor only)
        app.get("/vendor/forgot-password", vendorController::showForgotPasswordPage);
        app.post("/vendor/forgot-password", vendorController::handleForgotPassword);
        
        // =====================================================
        // API ROUTES (JSON endpoints for React components)
        // =====================================================
        
        // Dashboard API
        app.get("/api/dashboard/admin", dashboardController::getAdminDashboardData);
        app.get("/api/dashboard/vendor", dashboardController::getVendorDashboardData);
        
        // Charts & Analytics API
        app.get("/api/charts/project-progress", dashboardController::getProjectProgressChartData);
        app.get("/api/charts/vendor-performance", dashboardController::getVendorPerformanceChartData);
        app.get("/api/charts/material-usage", dashboardController::getMaterialUsageChartData);
        app.get("/api/charts/daily-timeline", dashboardController::getDailyTimelineChartData);
        
        // Widget API
        app.get("/api/widgets/vendor-stats", dashboardController::getVendorStatsWidget);
        app.get("/api/widgets/project-summary", dashboardController::getProjectSummaryWidget);
        app.get("/api/widgets/notifications", dashboardController::getNotificationsWidget);
        app.get("/api/widgets/compliance-status", dashboardController::getComplianceStatusWidget);
        
        // Project API
        app.get("/api/projects", apiController::getProjectsList);
        app.get("/api/projects/{id}", apiController::getProjectDetails);
        app.get("/api/projects/{id}/progress", apiController::getProjectProgress);
        
        // Vendor API
        app.get("/api/vendors", apiController::getVendorsList);
        app.get("/api/vendors/{id}", apiController::getVendorDetails);
        
        // Material API
        app.get("/api/materials", apiController::getMaterialsList);
        app.get("/api/materials/requests", apiController::getMaterialRequests);
        
        // Daily Progress API
        app.get("/api/daily-progress", apiController::getDailyProgress);
        app.post("/api/daily-progress", apiController::createDailyProgress);
        
        // =====================================================
        // ERROR HANDLERS
        // =====================================================
        
        app.error(404, ctx -> {
            if (ctx.header("Accept") != null && ctx.header("Accept").contains("application/json")) {
                ctx.json(new ErrorResponse("Resource not found", 404));
            } else {
                ctx.render("error/404.html");
            }
        });
        
        app.error(500, ctx -> {
            if (ctx.header("Accept") != null && ctx.header("Accept").contains("application/json")) {
                ctx.json(new ErrorResponse("Internal server error", 500));
            } else {
                ctx.render("error/500.html");
            }
        });
    }
    
    /**
     * Gets the port from command line arguments or uses default.
     */
    private static int getPort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + DEFAULT_PORT);
            }
        }
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT environment variable. Using default port " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
    
    /**
     * Simple error response class for JSON errors.
     */
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
    
    /**
     * Health check response for Railway deployment.
     */
    public static class HealthResponse {
        public final String status;
        public final String version;
        public final long timestamp;
        
        public HealthResponse(String status, String version, long timestamp) {
            this.status = status;
            this.version = version;
            this.timestamp = timestamp;
        }
    }
}
