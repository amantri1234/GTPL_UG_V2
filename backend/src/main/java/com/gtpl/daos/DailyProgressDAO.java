package com.gtpl.daos;

import com.gtpl.models.DailyProgress;
import com.gtpl.utils.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * DailyProgress DAO Class
 * Data access operations for DailyProgress entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class DailyProgressDAO extends BaseDAO<DailyProgress, Long> {
    
    @Override
    protected String getTableName() {
        return "daily_progress";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected DailyProgress mapResultSet(ResultSet rs) throws SQLException {
        DailyProgress progress = new DailyProgress();
        progress.setId(rs.getLong("id"));
        progress.setProjectId(rs.getLong("project_id"));
        progress.setVendorId(rs.getLong("vendor_id"));
        progress.setWorkDate(getLocalDateOrNull(rs, "work_date"));
        progress.setFromPoint(rs.getString("from_point"));
        progress.setToPoint(rs.getString("to_point"));
        progress.setKmCompletedToday(getBigDecimalOrNull(rs, "km_completed_today"));
        progress.setCumulativeKm(getBigDecimalOrNull(rs, "cumulative_km"));
        progress.setRemainingEstimatedWork(getBigDecimalOrNull(rs, "remaining_estimated_work"));
        progress.setWorkDescription(rs.getString("work_description"));
        progress.setRemarks(rs.getString("remarks"));
        progress.setWeatherConditions(rs.getString("weather_conditions"));
        progress.setWorkforceCount(getIntOrNull(rs, "workforce_count"));
        progress.setEquipmentUsed(rs.getString("equipment_used"));
        progress.setIsSubmitted(rs.getBoolean("is_submitted"));
        progress.setSubmittedAt(getLocalDateTimeOrNull(rs, "submitted_at"));
        progress.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        return progress;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, DailyProgress progress) throws SQLException {
        stmt.setLong(1, progress.getProjectId());
        stmt.setLong(2, progress.getVendorId());
        stmt.setDate(3, Date.valueOf(progress.getWorkDate()));
        stmt.setString(4, progress.getFromPoint());
        stmt.setString(5, progress.getToPoint());
        stmt.setBigDecimal(6, progress.getKmCompletedToday());
        stmt.setBigDecimal(7, progress.getRemainingEstimatedWork());
        stmt.setString(8, progress.getWorkDescription());
        stmt.setString(9, progress.getRemarks());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, DailyProgress progress) throws SQLException {
        // Daily progress is immutable, no updates allowed
        throw new UnsupportedOperationException("Daily progress records are immutable");
    }
    
    /**
     * Creates a new daily progress record.
     * 
     * @param progress the progress to create
     * @return the created record ID
     */
    public Long create(DailyProgress progress) {
        // Calculate cumulative KM
        java.math.BigDecimal previousCumulative = getPreviousCumulativeKm(progress.getProjectId(), progress.getWorkDate());
        progress.setCumulativeKm(previousCumulative.add(progress.getKmCompletedToday()));
        
        String sql = "INSERT INTO daily_progress (project_id, vendor_id, work_date, from_point, to_point, " +
                     "km_completed_today, cumulative_km, remaining_estimated_work, work_description, remarks, " +
                     "weather_conditions, workforce_count, equipment_used, is_submitted, submitted_at, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, progress.getProjectId());
            stmt.setLong(2, progress.getVendorId());
            stmt.setDate(3, Date.valueOf(progress.getWorkDate()));
            stmt.setString(4, progress.getFromPoint());
            stmt.setString(5, progress.getToPoint());
            stmt.setBigDecimal(6, progress.getKmCompletedToday());
            stmt.setBigDecimal(7, progress.getCumulativeKm());
            stmt.setBigDecimal(8, progress.getRemainingEstimatedWork());
            stmt.setString(9, progress.getWorkDescription());
            stmt.setString(10, progress.getRemarks());
            stmt.setString(11, progress.getWeatherConditions());
            stmt.setObject(12, progress.getWorkforceCount());
            stmt.setString(13, progress.getEquipmentUsed());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating daily progress", e);
        }
        
        return null;
    }
    
    /**
     * Gets the previous cumulative KM for a project.
     */
    private java.math.BigDecimal getPreviousCumulativeKm(Long projectId, LocalDate workDate) {
        String sql = "SELECT cumulative_km FROM daily_progress WHERE project_id = ? AND work_date < ? " +
                     "ORDER BY work_date DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            stmt.setDate(2, Date.valueOf(workDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("cumulative_km");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting previous cumulative KM", e);
        }
        return java.math.BigDecimal.ZERO;
    }
    
    /**
     * Finds daily progress by project ID.
     * 
     * @param projectId the project ID
     * @return list of daily progress records
     */
    public List<DailyProgress> findByProjectId(Long projectId) {
        String sql = "SELECT dp.*, p.project_name, p.project_code, v.vendor_name " +
                     "FROM daily_progress dp " +
                     "JOIN projects p ON dp.project_id = p.id " +
                     "JOIN vendors v ON dp.vendor_id = v.id " +
                     "WHERE dp.project_id = ? ORDER BY dp.work_date DESC";
        return executeQueryWithJoins(sql, projectId);
    }
    
    /**
     * Finds daily progress by vendor ID.
     * 
     * @param vendorId the vendor ID
     * @return list of daily progress records
     */
    public List<DailyProgress> findByVendorId(Long vendorId) {
        String sql = "SELECT dp.*, p.project_name, p.project_code, v.vendor_name " +
                     "FROM daily_progress dp " +
                     "JOIN projects p ON dp.project_id = p.id " +
                     "JOIN vendors v ON dp.vendor_id = v.id " +
                     "WHERE dp.vendor_id = ? ORDER BY dp.work_date DESC";
        return executeQueryWithJoins(sql, vendorId);
    }
    
    /**
     * Finds daily progress by project and date.
     * 
     * @param projectId the project ID
     * @param workDate the work date
     * @return Optional containing the record if found
     */
    public Optional<DailyProgress> findByProjectAndDate(Long projectId, LocalDate workDate) {
        String sql = "SELECT dp.*, p.project_name, p.project_code, v.vendor_name " +
                     "FROM daily_progress dp " +
                     "JOIN projects p ON dp.project_id = p.id " +
                     "JOIN vendors v ON dp.vendor_id = v.id " +
                     "WHERE dp.project_id = ? AND dp.work_date = ?";
        return executeQuerySingleWithJoins(sql, projectId, Date.valueOf(workDate));
    }
    
    /**
     * Checks if a daily report exists for a project on a given date.
     * 
     * @param projectId the project ID
     * @param workDate the work date
     * @return true if exists
     */
    public boolean existsForProjectAndDate(Long projectId, LocalDate workDate) {
        String sql = "SELECT 1 FROM daily_progress WHERE project_id = ? AND work_date = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            stmt.setDate(2, Date.valueOf(workDate));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking daily progress existence", e);
        }
    }
    
    /**
     * Gets the last daily progress for a project.
     * 
     * @param projectId the project ID
     * @return Optional containing the last record
     */
    public Optional<DailyProgress> findLastByProjectId(Long projectId) {
        String sql = "SELECT dp.*, p.project_name, p.project_code, v.vendor_name " +
                     "FROM daily_progress dp " +
                     "JOIN projects p ON dp.project_id = p.id " +
                     "JOIN vendors v ON dp.vendor_id = v.id " +
                     "WHERE dp.project_id = ? ORDER BY dp.work_date DESC LIMIT 1";
        return executeQuerySingleWithJoins(sql, projectId);
    }
    
    /**
     * Gets projects that missed daily updates.
     * 
     * @param date the date to check
     * @return list of project IDs
     */
    public List<Long> getProjectsWithMissingUpdates(LocalDate date) {
        String sql = "SELECT p.id FROM projects p " +
                     "WHERE p.status = 'IN_PROGRESS' " +
                     "AND NOT EXISTS (SELECT 1 FROM daily_progress dp " +
                     "WHERE dp.project_id = p.id AND dp.work_date = ?)";
        List<Long> projectIds = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projectIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting projects with missing updates", e);
        }
        
        return projectIds;
    }
    
    /**
     * Gets daily progress statistics for a project.
     * 
     * @param projectId the project ID
     * @return array: [total_reports, total_km, avg_daily_km]
     */
    public Object[] getProjectStats(Long projectId) {
        String sql = "SELECT COUNT(*) as total_reports, SUM(km_completed_today) as total_km, " +
                     "AVG(km_completed_today) as avg_daily_km FROM daily_progress WHERE project_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("total_reports"),
                        rs.getBigDecimal("total_km"),
                        rs.getBigDecimal("avg_daily_km")
                    };
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting project daily progress stats", e);
        }
        return new Object[]{0, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO};
    }
    
    /**
     * Executes a query with joins and maps results.
     */
    private List<DailyProgress> executeQueryWithJoins(String sql, Object... params) {
        List<DailyProgress> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DailyProgress progress = mapResultSet(rs);
                    progress.setProjectName(rs.getString("project_name"));
                    progress.setProjectCode(rs.getString("project_code"));
                    progress.setVendorName(rs.getString("vendor_name"));
                    results.add(progress);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query with joins", e);
        }
        
        return results;
    }
    
    /**
     * Executes a query with joins and returns single result.
     */
    private Optional<DailyProgress> executeQuerySingleWithJoins(String sql, Object... params) {
        List<DailyProgress> results = executeQueryWithJoins(sql, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
