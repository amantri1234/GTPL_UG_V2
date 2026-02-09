-- =====================================================
-- GTPL_UG Management System - Database Schema
-- Enterprise-Grade MySQL Database
-- =====================================================

CREATE DATABASE IF NOT EXISTS gtpl_ug_system 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE gtpl_ug_system;

-- =====================================================
-- 1. ADMINS TABLE
-- Hard-coded enterprise staff (4 admins)
-- =====================================================
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL DEFAULT 'System Administrator',
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. VENDORS TABLE
-- External contractors who sign up
-- =====================================================
CREATE TABLE vendors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(100) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    pincode VARCHAR(10),
    gst_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    reset_token VARCHAR(255) NULL,
    reset_token_expiry TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_active (is_active),
    INDEX idx_verified (is_verified),
    INDEX idx_reset_token (reset_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. PROJECTS TABLE
-- UG work projects created by admins
-- =====================================================
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_code VARCHAR(20) NOT NULL UNIQUE,
    project_name VARCHAR(200) NOT NULL,
    work_description TEXT,
    starting_point VARCHAR(200) NOT NULL,
    ending_point VARCHAR(200) NOT NULL,
    total_km DECIMAL(10,2) NOT NULL,
    completed_km DECIMAL(10,2) DEFAULT 0.00,
    remaining_km DECIMAL(10,2) GENERATED ALWAYS AS (total_km - completed_km) STORED,
    progress_percentage DECIMAL(5,2) GENERATED ALWAYS AS (completed_km / total_km * 100) STORED,
    expected_start_date DATE NOT NULL,
    expected_end_date DATE NOT NULL,
    actual_start_date DATE NULL,
    actual_end_date DATE NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED') DEFAULT 'ASSIGNED',
    assigned_vendor_id BIGINT NULL,
    created_by_admin_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_vendor_id) REFERENCES vendors(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_admin_id) REFERENCES admins(id) ON DELETE RESTRICT,
    INDEX idx_project_code (project_code),
    INDEX idx_status (status),
    INDEX idx_vendor (assigned_vendor_id),
    INDEX idx_priority (priority),
    INDEX idx_dates (expected_start_date, expected_end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. WORK_ASSIGNMENTS TABLE
-- Detailed work assignment records
-- =====================================================
CREATE TABLE work_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    assigned_by_admin_id BIGINT NOT NULL,
    assignment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_completion_date DATE NOT NULL,
    actual_completion_date DATE NULL,
    status ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'DELAYED', 'CANCELLED') DEFAULT 'PENDING',
    delay_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by_admin_id) REFERENCES admins(id) ON DELETE RESTRICT,
    INDEX idx_project (project_id),
    INDEX idx_vendor (vendor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. DAILY_PROGRESS TABLE
-- Immutable daily work reports from vendors
-- =====================================================
CREATE TABLE daily_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    from_point VARCHAR(200) NOT NULL,
    to_point VARCHAR(200) NOT NULL,
    km_completed_today DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cumulative_km DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    remaining_estimated_work DECIMAL(10,2) NOT NULL,
    work_description TEXT,
    remarks TEXT,
    weather_conditions VARCHAR(100),
    workforce_count INT DEFAULT 0,
    equipment_used VARCHAR(255),
    is_submitted BOOLEAN DEFAULT TRUE,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    UNIQUE KEY unique_daily_report (project_id, work_date),
    INDEX idx_project_date (project_id, work_date),
    INDEX idx_vendor_date (vendor_id, work_date),
    INDEX idx_work_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. MATERIALS TABLE
-- Master material catalog
-- =====================================================
CREATE TABLE materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    material_name VARCHAR(150) NOT NULL,
    description TEXT,
    unit ENUM('METERS', 'KILOGRAMS', 'UNITS', 'LITERS', 'BOXES', 'ROLLS', 'SETS') NOT NULL,
    category VARCHAR(100),
    standard_quantity DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material_code (material_code),
    INDEX idx_category (category),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. PROJECT_MATERIALS TABLE
-- Materials assigned to specific projects
-- =====================================================
CREATE TABLE project_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity_assigned DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    quantity_used DECIMAL(10,2) DEFAULT 0.00,
    quantity_remaining DECIMAL(10,2) GENERATED ALWAYS AS (quantity_assigned - quantity_used) STORED,
    assigned_by_admin_id BIGINT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_by_admin_id) REFERENCES admins(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_project_material (project_id, material_id),
    INDEX idx_project (project_id),
    INDEX idx_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. MATERIAL_REQUESTS TABLE
-- Vendor material requests
-- =====================================================
CREATE TABLE material_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_code VARCHAR(20) NOT NULL UNIQUE,
    project_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity_required DECIMAL(10,2) NOT NULL,
    reason TEXT NOT NULL,
    urgency ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'PARTIALLY_APPROVED', 'FULFILLED') DEFAULT 'PENDING',
    approved_quantity DECIMAL(10,2) NULL,
    approved_by_admin_id BIGINT NULL,
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fulfilled_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE RESTRICT,
    FOREIGN KEY (approved_by_admin_id) REFERENCES admins(id) ON DELETE SET NULL,
    INDEX idx_project (project_id),
    INDEX idx_vendor (vendor_id),
    INDEX idx_status (status),
    INDEX idx_request_code (request_code),
    INDEX idx_urgency (urgency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. DAILY_MATERIAL_USAGE TABLE
-- Track material usage per daily progress
-- =====================================================
CREATE TABLE daily_material_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_progress_id BIGINT NOT NULL,
    project_material_id BIGINT NOT NULL,
    quantity_used DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (daily_progress_id) REFERENCES daily_progress(id) ON DELETE CASCADE,
    FOREIGN KEY (project_material_id) REFERENCES project_materials(id) ON DELETE CASCADE,
    INDEX idx_daily_progress (daily_progress_id),
    INDEX idx_project_material (project_material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. NOTIFICATIONS TABLE
-- In-app notification system
-- =====================================================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_type ENUM('ADMIN', 'VENDOR') NOT NULL,
    recipient_id BIGINT NOT NULL,
    sender_type ENUM('ADMIN', 'VENDOR', 'SYSTEM') DEFAULT 'SYSTEM',
    sender_id BIGINT NULL,
    notification_type ENUM('DAILY_UPDATE_MISSED', 'PROJECT_ASSIGNED', 'PROJECT_DELAYED', 'MATERIAL_REQUEST_STATUS', 'MATERIAL_APPROVED', 'MATERIAL_REJECTED', 'PROGRESS_UPDATE', 'SYSTEM_ALERT', 'DEADLINE_REMINDER') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type ENUM('PROJECT', 'WORK_ASSIGNMENT', 'MATERIAL_REQUEST', 'DAILY_PROGRESS') NULL,
    related_entity_id BIGINT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    INDEX idx_recipient (recipient_type, recipient_id),
    INDEX idx_unread (recipient_type, recipient_id, is_read),
    INDEX idx_type (notification_type),
    INDEX idx_created (created_at),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. AUDIT_LOGS TABLE
-- Track important system actions
-- =====================================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_type ENUM('ADMIN', 'VENDOR') NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NULL,
    entity_id BIGINT NULL,
    old_values JSON NULL,
    new_values JSON NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_type, user_id),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. VENDOR_PERFORMANCE TABLE
-- Aggregated performance metrics
-- =====================================================
CREATE TABLE vendor_performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL UNIQUE,
    total_projects INT DEFAULT 0,
    active_projects INT DEFAULT 0,
    completed_projects INT DEFAULT 0,
    delayed_projects INT DEFAULT 0,
    total_km_assigned DECIMAL(12,2) DEFAULT 0.00,
    total_km_completed DECIMAL(12,2) DEFAULT 0.00,
    average_completion_rate DECIMAL(5,2) DEFAULT 0.00,
    on_time_delivery_rate DECIMAL(5,2) DEFAULT 0.00,
    daily_update_compliance_rate DECIMAL(5,2) DEFAULT 0.00,
    material_request_count INT DEFAULT 0,
    material_approval_rate DECIMAL(5,2) DEFAULT 0.00,
    last_evaluation_date DATE NULL,
    performance_rating ENUM('EXCELLENT', 'GOOD', 'AVERAGE', 'BELOW_AVERAGE', 'POOR') NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
    INDEX idx_vendor (vendor_id),
    INDEX idx_rating (performance_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 13. SYSTEM_CONFIG TABLE
-- System configuration settings
-- =====================================================
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description TEXT,
    is_editable BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by_admin_id BIGINT NULL,
    FOREIGN KEY (updated_by_admin_id) REFERENCES admins(id) ON DELETE SET NULL,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT PRE-DEFINED DATA
-- =====================================================

-- Insert 4 Hard-coded Admins (Passwords are bcrypt hashes of 'Admin@123')
-- Hash generated using: BCrypt.hashpw("Admin@123", BCrypt.gensalt(12))
INSERT INTO admins (username, password_hash, full_name, email, phone) VALUES
('admin1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYA.qGZvKG6G', 'System Administrator 1', 'admin1@gtpl.com', '+91-9876543210'),
('admin2', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYA.qGZvKG6G', 'System Administrator 2', 'admin2@gtpl.com', '+91-9876543211'),
('admin3', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYA.qGZvKG6G', 'System Administrator 3', 'admin3@gtpl.com', '+91-9876543212'),
('admin4', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.VTtYA.qGZvKG6G', 'System Administrator 4', 'admin4@gtpl.com', '+91-9876543213');

-- Insert Default Materials
INSERT INTO materials (material_code, material_name, description, unit, category) VALUES
('HDPE-PIPE-110', 'HDPE Pipe 110mm', 'High-density polyethylene pipe for underground cabling', 'METERS', 'PIPING'),
('HDPE-PIPE-160', 'HDPE Pipe 160mm', 'High-density polyethylene pipe for main lines', 'METERS', 'PIPING'),
('PVC-DUCT-100', 'PVC Duct 100mm', 'PVC duct for cable protection', 'METERS', 'PIPING'),
('CABLE-FO-24', 'Fiber Optic Cable 24 Core', 'Single mode fiber optic cable', 'METERS', 'CABLE'),
('CABLE-FO-48', 'Fiber Optic Cable 48 Core', 'Single mode fiber optic cable', 'METERS', 'CABLE'),
('CABLE-COPPER', 'Copper Cable', 'Underground copper communication cable', 'METERS', 'CABLE'),
('MANHOLE-PRECAST', 'Precast Manhole', 'Standard precast concrete manhole', 'UNITS', 'MANHOLE'),
('HANDHOLE-PLASTIC', 'Plastic Handhole', 'Plastic handhole for access points', 'UNITS', 'MANHOLE'),
('WARNING-TAPE', 'Underground Warning Tape', 'Detectable warning tape for buried cables', 'METERS', 'SAFETY'),
('CABLE-MARKER', 'Cable Route Marker', 'Post type cable route marker', 'UNITS', 'SAFETY'),
('JOINT-CLOSURE', 'Cable Joint Closure', 'Underground cable joint closure kit', 'UNITS', 'ACCESSORIES'),
('SAND-BEDDING', 'Sand for Bedding', 'Fine sand for pipe bedding', 'KILOGRAMS', 'CONSUMABLE'),
('CEMENT-OPC', 'OPC Cement', 'Ordinary Portland Cement', 'KILOGRAMS', 'CONSUMABLE'),
('BRICK-CLASSA', 'Class A Bricks', 'First class bricks for construction', 'UNITS', 'CONSUMABLE');

-- Insert System Configuration
INSERT INTO system_config (config_key, config_value, description, is_editable) VALUES
('APP_NAME', 'GTPL_UG Management System', 'Application display name', FALSE),
('APP_VERSION', '1.0.0', 'Current application version', FALSE),
('DAILY_UPDATE_CUTOFF_TIME', '18:00', 'Daily update submission cutoff time (24-hour format)', TRUE),
('PASSWORD_RESET_EXPIRY_HOURS', '24', 'Password reset token expiry time in hours', TRUE),
('SESSION_TIMEOUT_MINUTES', '120', 'User session timeout in minutes', TRUE),
('MAX_LOGIN_ATTEMPTS', '5', 'Maximum failed login attempts before lockout', TRUE),
('LOCKOUT_DURATION_MINUTES', '30', 'Account lockout duration in minutes', TRUE),
('DEFAULT_PROJECT_PRIORITY', 'MEDIUM', 'Default priority for new projects', TRUE),
('NOTIFICATION_RETENTION_DAYS', '90', 'Number of days to retain notifications', TRUE),
('AUDIT_LOG_RETENTION_DAYS', '365', 'Number of days to retain audit logs', TRUE);

-- Create triggers for automatic updates
DELIMITER //

-- Trigger to update project completed_km when daily progress is added
CREATE TRIGGER trg_update_project_progress
AFTER INSERT ON daily_progress
FOR EACH ROW
BEGIN
    DECLARE total_completed DECIMAL(10,2);
    
    SELECT COALESCE(SUM(km_completed_today), 0) INTO total_completed
    FROM daily_progress
    WHERE project_id = NEW.project_id;
    
    UPDATE projects
    SET completed_km = total_completed,
        status = CASE 
            WHEN total_completed >= total_km THEN 'COMPLETED'
            WHEN total_completed > 0 THEN 'IN_PROGRESS'
            ELSE status
        END,
        actual_end_date = CASE 
            WHEN total_completed >= total_km THEN CURDATE()
            ELSE actual_end_date
        END
    WHERE id = NEW.project_id;
END//

-- Trigger to update vendor performance metrics
CREATE TRIGGER trg_update_vendor_performance
AFTER UPDATE ON projects
FOR EACH ROW
BEGIN
    DECLARE v_total_projects INT;
    DECLARE v_active_projects INT;
    DECLARE v_completed_projects INT;
    DECLARE v_total_km_assigned DECIMAL(12,2);
    DECLARE v_total_km_completed DECIMAL(12,2);
    
    IF OLD.assigned_vendor_id IS NOT NULL THEN
        SELECT 
            COUNT(*),
            SUM(CASE WHEN status IN ('ASSIGNED', 'IN_PROGRESS') THEN 1 ELSE 0 END),
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END),
            COALESCE(SUM(total_km), 0),
            COALESCE(SUM(completed_km), 0)
        INTO 
            v_total_projects,
            v_active_projects,
            v_completed_projects,
            v_total_km_assigned,
            v_total_km_completed
        FROM projects
        WHERE assigned_vendor_id = OLD.assigned_vendor_id;
        
        INSERT INTO vendor_performance (
            vendor_id, total_projects, active_projects, completed_projects,
            total_km_assigned, total_km_completed, average_completion_rate
        ) VALUES (
            OLD.assigned_vendor_id, v_total_projects, v_active_projects, v_completed_projects,
            v_total_km_assigned, v_total_km_completed,
            CASE WHEN v_total_km_assigned > 0 THEN (v_total_km_completed / v_total_km_assigned * 100) ELSE 0 END
        )
        ON DUPLICATE KEY UPDATE
            total_projects = v_total_projects,
            active_projects = v_active_projects,
            completed_projects = v_completed_projects,
            total_km_assigned = v_total_km_assigned,
            total_km_completed = v_total_km_completed,
            average_completion_rate = CASE WHEN v_total_km_assigned > 0 THEN (v_total_km_completed / v_total_km_assigned * 100) ELSE 0 END,
            updated_at = CURRENT_TIMESTAMP;
    END IF;
END//

DELIMITER ;

-- Create views for common queries
CREATE VIEW vw_project_summary AS
SELECT 
    p.id,
    p.project_code,
    p.project_name,
    p.total_km,
    p.completed_km,
    p.remaining_km,
    p.progress_percentage,
    p.status,
    p.priority,
    v.vendor_name,
    v.company_name,
    v.email as vendor_email,
    p.expected_start_date,
    p.expected_end_date,
    DATEDIFF(p.expected_end_date, CURDATE()) as days_remaining,
    CASE 
        WHEN p.expected_end_date < CURDATE() AND p.status != 'COMPLETED' THEN 'OVERDUE'
        WHEN DATEDIFF(p.expected_end_date, CURDATE()) <= 7 THEN 'DUE_SOON'
        ELSE 'ON_TRACK'
    END as deadline_status
FROM projects p
LEFT JOIN vendors v ON p.assigned_vendor_id = v.id;

CREATE VIEW vw_vendor_dashboard AS
SELECT 
    v.id as vendor_id,
    v.vendor_name,
    v.company_name,
    COUNT(DISTINCT p.id) as total_projects,
    SUM(CASE WHEN p.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as active_projects,
    SUM(CASE WHEN p.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_projects,
    COALESCE(SUM(p.total_km), 0) as total_km_assigned,
    COALESCE(SUM(p.completed_km), 0) as total_km_completed,
    COALESCE(SUM(p.remaining_km), 0) as total_km_remaining,
    COUNT(DISTINCT mr.id) as pending_material_requests,
    COUNT(DISTINCT n.id) as unread_notifications
FROM vendors v
LEFT JOIN projects p ON v.id = p.assigned_vendor_id AND p.status IN ('ASSIGNED', 'IN_PROGRESS')
LEFT JOIN material_requests mr ON v.id = mr.vendor_id AND mr.status = 'PENDING'
LEFT JOIN notifications n ON v.id = n.recipient_id AND n.recipient_type = 'VENDOR' AND n.is_read = FALSE
WHERE v.is_active = TRUE
GROUP BY v.id, v.vendor_name, v.company_name;

CREATE VIEW vw_daily_compliance AS
SELECT 
    p.id as project_id,
    p.project_code,
    p.project_name,
    p.assigned_vendor_id,
    v.vendor_name,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY) as expected_report_date,
    EXISTS(
        SELECT 1 FROM daily_progress dp 
        WHERE dp.project_id = p.id 
        AND dp.work_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
    ) as has_daily_update,
    (
        SELECT dp.km_completed_today FROM daily_progress dp 
        WHERE dp.project_id = p.id 
        AND dp.work_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
    ) as last_km_completed,
    (
        SELECT dp.submitted_at FROM daily_progress dp 
        WHERE dp.project_id = p.id 
        AND dp.work_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)
    ) as last_submitted_at
FROM projects p
JOIN vendors v ON p.assigned_vendor_id = v.id
WHERE p.status = 'IN_PROGRESS';
