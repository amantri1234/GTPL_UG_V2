package com.gtpl;

import com.gtpl.controllers.*;
import com.gtpl.middleware.AuthMiddleware;
import com.gtpl.utils.DatabaseConfig;
import com.gtpl.utils.ViewEngineConfig;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.sql.*;  // ADD THIS IMPORT

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
        try {
            DatabaseConfig.initialize();
            
            // ADD THIS LINE - Initialize database schema
            initializeDatabaseSchema();
            
        } catch (Exception e) {
            System.err.println("⚠️ Database not available. Application will start, but DB features disabled.");
        }

        
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
    
    // =====================================================
    // ADD THIS ENTIRE METHOD AFTER main() METHOD
    // =====================================================
    
    /**
     * Initializes database schema by creating all required tables if they don't exist.
     * This method is called automatically on application startup.
     */
    private static void initializeDatabaseSchema() {
        System.out.println("=== Checking Database Schema ===");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if tables already exist
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'vendors'");
            if (rs.next()) {
                System.out.println("✓ Database tables already exist");
                return;
            }
            
            System.out.println("⚠ Tables not found. Creating database schema...");
            
            // Create admins table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admins (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100),
                    phone VARCHAR(20),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);
            System.out.println("✓ Created admins table");
            
            // Create vendors table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vendors (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    company_name VARCHAR(200),
                    contact_person VARCHAR(100),
                    phone VARCHAR(20),
                    address TEXT,
                    is_active BOOLEAN DEFAULT TRUE,
                    is_verified BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);
            System.out.println("✓ Created vendors table");
            
            // Create projects table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    project_name VARCHAR(200) NOT NULL,
                    project_code VARCHAR(50) UNIQUE,
                    location VARCHAR(200),
                    description TEXT,
                    start_date DATE,
                    end_date DATE,
                    total_km DECIMAL(10,2),
                    completed_km DECIMAL(10,2) DEFAULT 0,
                    status ENUM('PLANNING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'ON_HOLD') DEFAULT 'PLANNING',
                    budget DECIMAL(15,2),
                    created_by INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (created_by) REFERENCES admins(id)
                )
            """);
            System.out.println("✓ Created projects table");
            
            // Create work_assignments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS work_assignments (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    project_id INT NOT NULL,
                    vendor_id INT NOT NULL,
                    assigned_km DECIMAL(10,2),
                    start_date DATE,
                    end_date DATE,
                    status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'ASSIGNED',
                    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (project_id) REFERENCES projects(id),
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id)
                )
            """);
            System.out.println("✓ Created work_assignments table");
            
            // Create materials table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS materials (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    material_name VARCHAR(200) NOT NULL,
                    material_code VARCHAR(50) UNIQUE,
                    category VARCHAR(100),
                    unit VARCHAR(50),
                    unit_price DECIMAL(10,2),
                    stock_quantity INT DEFAULT 0,
                    min_stock_level INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);
            System.out.println("✓ Created materials table");
            
            // Create project_materials table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS project_materials (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    project_id INT NOT NULL,
                    material_id INT NOT NULL,
                    allocated_quantity INT,
                    used_quantity INT DEFAULT 0,
                    FOREIGN KEY (project_id) REFERENCES projects(id),
                    FOREIGN KEY (material_id) REFERENCES materials(id)
                )
            """);
            System.out.println("✓ Created project_materials table");
            
            // Create material_requests table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS material_requests (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    vendor_id INT NOT NULL,
                    project_id INT NOT NULL,
                    material_id INT NOT NULL,
                    requested_quantity INT,
                    approved_quantity INT,
                    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'DELIVERED') DEFAULT 'PENDING',
                    request_date DATE,
                    approved_by INT,
                    approved_at TIMESTAMP NULL,
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
                    FOREIGN KEY (project_id) REFERENCES projects(id),
                    FOREIGN KEY (material_id) REFERENCES materials(id),
                    FOREIGN KEY (approved_by) REFERENCES admins(id)
                )
            """);
            System.out.println("✓ Created material_requests table");
            
            // Create daily_progress table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS daily_progress (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    vendor_id INT NOT NULL,
                    project_id INT NOT NULL,
                    report_date DATE NOT NULL,
                    work_description TEXT,
                    km_completed DECIMAL(10,2),
                    workers_count INT,
                    equipment_used TEXT,
                    weather_conditions VARCHAR(100),
                    issues_faced TEXT,
                    photos_uploaded BOOLEAN DEFAULT FALSE,
                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_verified BOOLEAN DEFAULT FALSE,
                    verified_by INT,
                    verified_at TIMESTAMP NULL,
                    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
                    FOREIGN KEY (project_id) REFERENCES projects(id),
                    FOREIGN KEY (verified_by) REFERENCES admins(id),
                    UNIQUE KEY unique_daily_report (vendor_id, project_id, report_date)
                )
            """);
            System.out.println("✓ Created daily_progress table");
            
            // Create notifications table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_type ENUM('ADMIN', 'VENDOR') NOT NULL,
                    user_id INT NOT NULL,
                    title VARCHAR(200),
                    message TEXT,
                    type VARCHAR(50),
                    is_read BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_user (user_type, user_id, is_read)
                )
            """);
            System.out.println("✓ Created notifications table");
            
            // Create audit_logs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_type ENUM('ADMIN', 'VENDOR'),
                    user_id INT,
                    action VARCHAR(100),
                    table_name VARCHAR(100),
                    record_id INT,
                    changes TEXT,
                    ip_address VARCHAR(45),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            System.out.println("✓ Created audit_logs table");
            
            // Create system_config table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS system_config (
                    config_key VARCHAR(100) PRIMARY KEY,
                    config_value TEXT,
                    description TEXT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);
            System.out.println("✓ Created system_config table");
            
            // Insert default admin account
            stmt.execute("""
                INSERT INTO admins (username, email, password, full_name) VALUES
                ('admin', 'admin@gtpl.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator')
                ON DUPLICATE KEY UPDATE username=username
            """);
            System.out.println("✓ Created default admin user");
            
            // Insert system configuration
            stmt.execute("""
                INSERT INTO system_config (config_key, config_value, description) VALUES
                ('SESSION_TIMEOUT_MINUTES', '120', 'Session timeout in minutes'),
                ('PASSWORD_RESET_EXPIRY_HOURS', '24', 'Password reset token expiry in hours'),
                ('DAILY_UPDATE_CUTOFF_TIME', '18:00', 'Daily report submission deadline'),
                ('MAX_LOGIN_ATTEMPTS', '5', 'Maximum failed login attempts before lockout')
                ON DUPLICATE KEY UPDATE config_key=config_key
            """);
            System.out.println("✓ Created system configuration");
            
            System.out.println("=== Database Schema Created Successfully! ===");
            System.out.println("Default Admin Login:");
            System.out.println("  Username: admin");
            System.out.println("  Password: Admin@123");
            
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database schema:");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =====================================================
    // REST OF YOUR CODE STAYS THE SAME - DON'T CHANGE ANYTHING BELOW
    // =====================================================
    
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
            config.plugins.enableCors(cors -> {
                cors.add(corsRule -> {
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
            boolean dbHealthy = DatabaseConfig.isHealthy();
            ctx.json(new HealthResponse(
            dbHealthy ? "UP" : "DEGRADED",
            "1.0.0",
            System.currentTimeMillis()
            ));
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