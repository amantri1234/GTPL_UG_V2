package com.gtpl.daos;

import com.gtpl.models.Admin;
import com.gtpl.utils.DatabaseConfig;

import java.sql.*;
import java.util.Optional;

/**
 * Admin DAO Class
 * Data access operations for Admin entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class AdminDAO extends BaseDAO<Admin, Long> {
    
    @Override
    protected String getTableName() {
        return "admins";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected Admin mapResultSet(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getLong("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setFullName(rs.getString("full_name"));
        admin.setEmail(rs.getString("email"));
        admin.setPhone(rs.getString("phone"));
        admin.setActive(rs.getBoolean("is_active"));
        admin.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        admin.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        admin.setLastLogin(getLocalDateTimeOrNull(rs, "last_login"));
        return admin;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Admin admin) throws SQLException {
        stmt.setString(1, admin.getUsername());
        stmt.setString(2, admin.getPasswordHash());
        stmt.setString(3, admin.getFullName());
        stmt.setString(4, admin.getEmail());
        stmt.setString(5, admin.getPhone());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Admin admin) throws SQLException {
        stmt.setString(1, admin.getFullName());
        stmt.setString(2, admin.getEmail());
        stmt.setString(3, admin.getPhone());
        stmt.setBoolean(4, admin.isActive());
        stmt.setLong(5, admin.getId());
    }
    
    /**
     * Finds an admin by username.
     * 
     * @param username the username
     * @return Optional containing the admin if found
     */
    public Optional<Admin> findByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ? AND is_active = TRUE";
        return executeQuerySingle(sql, username);
    }
    
    /**
     * Finds an admin by email.
     * 
     * @param email the email
     * @return Optional containing the admin if found
     */
    public Optional<Admin> findByEmail(String email) {
        String sql = "SELECT * FROM admins WHERE email = ? AND is_active = TRUE";
        return executeQuerySingle(sql, email);
    }
    
    /**
     * Updates the last login timestamp.
     * 
     * @param adminId the admin ID
     * @return true if updated
     */
    public boolean updateLastLogin(Long adminId) {
        String sql = "UPDATE admins SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, adminId) > 0;
    }
    
    /**
     * Updates admin profile (without password).
     * 
     * @param admin the admin to update
     * @return true if updated
     */
    public boolean updateProfile(Admin admin) {
        String sql = "UPDATE admins SET full_name = ?, email = ?, phone = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, admin.getFullName(), admin.getEmail(), admin.getPhone(), admin.getId()) > 0;
    }
    
    /**
     * Updates admin password.
     * 
     * @param adminId the admin ID
     * @param newPasswordHash the new password hash
     * @return true if updated
     */
    public boolean updatePassword(Long adminId, String newPasswordHash) {
        String sql = "UPDATE admins SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, newPasswordHash, adminId) > 0;
    }
    
    /**
     * Checks if a username exists.
     * 
     * @param username the username
     * @return true if exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM admins WHERE username = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking username existence", e);
        }
    }
    
    /**
     * Checks if an email exists.
     * 
     * @param email the email
     * @return true if exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM admins WHERE email = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking email existence", e);
        }
    }
}
