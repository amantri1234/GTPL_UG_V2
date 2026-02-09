package com.gtpl.daos;

import com.gtpl.models.Project;
import com.gtpl.utils.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Project DAO Class
 * Data access operations for Project entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class ProjectDAO extends BaseDAO<Project, Long> {
    
    @Override
    protected String getTableName() {
        return "projects";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected Project mapResultSet(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getLong("id"));
        project.setProjectCode(rs.getString("project_code"));
        project.setProjectName(rs.getString("project_name"));
        project.setWorkDescription(rs.getString("work_description"));
        project.setStartingPoint(rs.getString("starting_point"));
        project.setEndingPoint(rs.getString("ending_point"));
        project.setTotalKm(getBigDecimalOrNull(rs, "total_km"));
        project.setCompletedKm(getBigDecimalOrNull(rs, "completed_km"));
        project.setRemainingKm(getBigDecimalOrNull(rs, "remaining_km"));
        project.setProgressPercentage(getBigDecimalOrNull(rs, "progress_percentage"));
        project.setExpectedStartDate(getLocalDateOrNull(rs, "expected_start_date"));
        project.setExpectedEndDate(getLocalDateOrNull(rs, "expected_end_date"));
        project.setActualStartDate(getLocalDateOrNull(rs, "actual_start_date"));
        project.setActualEndDate(getLocalDateOrNull(rs, "actual_end_date"));
        
        String priorityStr = rs.getString("priority");
        if (priorityStr != null) {
            project.setPriority(Project.Priority.valueOf(priorityStr));
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            project.setStatus(Project.Status.valueOf(statusStr));
        }
        
        project.setAssignedVendorId(getLongOrNull(rs, "assigned_vendor_id"));
        project.setCreatedByAdminId(rs.getLong("created_by_admin_id"));
        project.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        project.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        
        return project;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Project project) throws SQLException {
        stmt.setString(1, project.getProjectCode());
        stmt.setString(2, project.getProjectName());
        stmt.setString(3, project.getWorkDescription());
        stmt.setString(4, project.getStartingPoint());
        stmt.setString(5, project.getEndingPoint());
        stmt.setBigDecimal(6, project.getTotalKm());
        stmt.setDate(7, Date.valueOf(project.getExpectedStartDate()));
        stmt.setDate(8, Date.valueOf(project.getExpectedEndDate()));
        stmt.setString(9, project.getPriority() != null ? project.getPriority().name() : "MEDIUM");
        stmt.setLong(10, project.getCreatedByAdminId());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Project project) throws SQLException {
        stmt.setString(1, project.getProjectName());
        stmt.setString(2, project.getWorkDescription());
        stmt.setString(3, project.getStartingPoint());
        stmt.setString(4, project.getEndingPoint());
        stmt.setBigDecimal(5, project.getTotalKm());
        stmt.setDate(6, Date.valueOf(project.getExpectedStartDate()));
        stmt.setDate(7, Date.valueOf(project.getExpectedEndDate()));
        stmt.setString(8, project.getPriority() != null ? project.getPriority().name() : "MEDIUM");
        stmt.setString(9, project.getStatus() != null ? project.getStatus().name() : "ASSIGNED");
        stmt.setLong(10, project.getId());
    }
    
    /**
     * Creates a new project with auto-generated code.
     * 
     * @param project the project to create
     * @return the created project ID
     */
    public Long create(Project project) {
        // Generate project code: PRJ-YYYY-XXXXX
        String projectCode = generateProjectCode();
        project.setProjectCode(projectCode);
        
        String sql = "INSERT INTO projects (project_code, project_name, work_description, starting_point, " +
                     "ending_point, total_km, expected_start_date, expected_end_date, priority, status, " +
                     "created_by_admin_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ASSIGNED', ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, project);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating project", e);
        }
        
        return null;
    }
    
    /**
     * Generates a unique project code.
     */
    private String generateProjectCode() {
        String year = String.valueOf(java.time.Year.now().getValue());
        String sql = "SELECT COUNT(*) FROM projects WHERE YEAR(created_at) = YEAR(CURRENT_DATE)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            return String.format("PRJ-%s-%05d", year, count + 1);
        } catch (SQLException e) {
            throw new RuntimeException("Error generating project code", e);
        }
    }
    
    /**
     * Finds a project by its code.
     * 
     * @param projectCode the project code
     * @return Optional containing the project if found
     */
    public Optional<Project> findByProjectCode(String projectCode) {
        String sql = "SELECT * FROM projects WHERE project_code = ?";
        return executeQuerySingle(sql, projectCode);
    }
    
    /**
     * Finds projects by vendor ID.
     * 
     * @param vendorId the vendor ID
     * @return list of projects
     */
    public List<Project> findByVendorId(Long vendorId) {
        String sql = "SELECT p.*, v.vendor_name, v.company_name as vendor_company_name " +
                     "FROM projects p LEFT JOIN vendors v ON p.assigned_vendor_id = v.id " +
                     "WHERE p.assigned_vendor_id = ? ORDER BY p.created_at DESC";
        return executeQueryWithJoins(sql, vendorId);
    }
    
    /**
     * Finds active projects by vendor ID.
     * 
     * @param vendorId the vendor ID
     * @return list of active projects
     */
    public List<Project> findActiveByVendorId(Long vendorId) {
        String sql = "SELECT p.*, v.vendor_name, v.company_name as vendor_company_name " +
                     "FROM projects p LEFT JOIN vendors v ON p.assigned_vendor_id = v.id " +
                     "WHERE p.assigned_vendor_id = ? AND p.status IN ('ASSIGNED', 'IN_PROGRESS') " +
                     "ORDER BY p.priority DESC, p.expected_end_date";
        return executeQueryWithJoins(sql, vendorId);
    }
    
    /**
     * Finds projects by status.
     * 
     * @param status the project status
     * @return list of projects
     */
    public List<Project> findByStatus(Project.Status status) {
        String sql = "SELECT p.*, v.vendor_name, v.company_name as vendor_company_name " +
                     "FROM projects p LEFT JOIN vendors v ON p.assigned_vendor_id = v.id " +
                     "WHERE p.status = ? ORDER BY p.created_at DESC";
        return executeQueryWithJoins(sql, status.name());
    }
    
    /**
     * Assigns a project to a vendor.
     * 
     * @param projectId the project ID
     * @param vendorId the vendor ID
     * @return true if assigned
     */
    public boolean assignToVendor(Long projectId, Long vendorId) {
        String sql = "UPDATE projects SET assigned_vendor_id = ?, status = 'ASSIGNED', " +
                     "actual_start_date = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, vendorId, projectId) > 0;
    }
    
    /**
     * Updates project status.
     * 
     * @param projectId the project ID
     * @param status the new status
     * @return true if updated
     */
    public boolean updateStatus(Long projectId, Project.Status status) {
        String sql = "UPDATE projects SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, status.name(), projectId) > 0;
    }
    
    /**
     * Updates project progress.
     * 
     * @param projectId the project ID
     * @param completedKm the completed KM
     * @return true if updated
     */
    public boolean updateProgress(Long projectId, java.math.BigDecimal completedKm) {
        String sql = "UPDATE projects SET completed_km = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, completedKm, projectId) > 0;
    }
    
    /**
     * Gets project statistics.
     * 
     * @return array: [total, assigned, in_progress, completed, on_hold, cancelled]
     */
    public int[] getProjectStats() {
        String sql = "SELECT " +
                     "COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned, " +
                     "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress, " +
                     "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
                     "SUM(CASE WHEN status = 'ON_HOLD' THEN 1 ELSE 0 END) as on_hold, " +
                     "SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
                     "FROM projects";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new int[]{
                    rs.getInt("total"),
                    rs.getInt("assigned"),
                    rs.getInt("in_progress"),
                    rs.getInt("completed"),
                    rs.getInt("on_hold"),
                    rs.getInt("cancelled")
                };
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting project stats", e);
        }
        return new int[]{0, 0, 0, 0, 0, 0};
    }
    
    /**
     * Gets overdue projects count.
     * 
     * @return count of overdue projects
     */
    public int getOverdueProjectsCount() {
        String sql = "SELECT COUNT(*) FROM projects WHERE status IN ('ASSIGNED', 'IN_PROGRESS') " +
                     "AND expected_end_date < CURRENT_DATE";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting overdue projects count", e);
        }
        return 0;
    }
    
    /**
     * Executes a query and maps results with vendor joins.
     */
    private List<Project> executeQueryWithJoins(String sql, Object... params) {
        List<Project> projects = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Project project = mapResultSet(rs);
                    // Set transient fields from join
                    try {
                        project.setVendorName(rs.getString("vendor_name"));
                        project.setVendorCompanyName(rs.getString("vendor_company_name"));
                    } catch (SQLException e) {
                        // Columns might not exist in all queries
                    }
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query with joins", e);
        }
        
        return projects;
    }
}
