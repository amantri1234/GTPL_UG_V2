package com.gtpl.daos;

import com.gtpl.models.Vendor;
import com.gtpl.utils.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Vendor DAO Class
 * Data access operations for Vendor entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class VendorDAO extends BaseDAO<Vendor, Long> {
    
    @Override
    protected String getTableName() {
        return "vendors";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected Vendor mapResultSet(ResultSet rs) throws SQLException {
        Vendor vendor = new Vendor();
        vendor.setId(rs.getLong("id"));
        vendor.setVendorName(rs.getString("vendor_name"));
        vendor.setCompanyName(rs.getString("company_name"));
        vendor.setEmail(rs.getString("email"));
        vendor.setPhoneNumber(rs.getString("phone_number"));
        vendor.setPasswordHash(rs.getString("password_hash"));
        vendor.setAddress(rs.getString("address"));
        vendor.setCity(rs.getString("city"));
        vendor.setState(rs.getString("state"));
        vendor.setPincode(rs.getString("pincode"));
        vendor.setGstNumber(rs.getString("gst_number"));
        vendor.setActive(rs.getBoolean("is_active"));
        vendor.setVerified(rs.getBoolean("is_verified"));
        vendor.setEmailVerified(rs.getBoolean("email_verified"));
        vendor.setResetToken(rs.getString("reset_token"));
        vendor.setResetTokenExpiry(getLocalDateTimeOrNull(rs, "reset_token_expiry"));
        vendor.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        vendor.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        vendor.setLastLogin(getLocalDateTimeOrNull(rs, "last_login"));
        return vendor;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Vendor vendor) throws SQLException {
        stmt.setString(1, vendor.getVendorName());
        stmt.setString(2, vendor.getCompanyName());
        stmt.setString(3, vendor.getEmail());
        stmt.setString(4, vendor.getPhoneNumber());
        stmt.setString(5, vendor.getPasswordHash());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Vendor vendor) throws SQLException {
        stmt.setString(1, vendor.getVendorName());
        stmt.setString(2, vendor.getCompanyName());
        stmt.setString(3, vendor.getEmail());
        stmt.setString(4, vendor.getPhoneNumber());
        stmt.setString(5, vendor.getAddress());
        stmt.setString(6, vendor.getCity());
        stmt.setString(7, vendor.getState());
        stmt.setString(8, vendor.getPincode());
        stmt.setString(9, vendor.getGstNumber());
        stmt.setLong(10, vendor.getId());
    }
    
    /**
     * Finds a vendor by email.
     * 
     * @param email the email
     * @return Optional containing the vendor if found
     */
    public Optional<Vendor> findByEmail(String email) {
        String sql = "SELECT * FROM vendors WHERE email = ?";
        return executeQuerySingle(sql, email);
    }
    
    /**
     * Finds a vendor by reset token.
     * 
     * @param token the reset token
     * @return Optional containing the vendor if found
     */
    public Optional<Vendor> findByResetToken(String token) {
        String sql = "SELECT * FROM vendors WHERE reset_token = ? AND reset_token_expiry > CURRENT_TIMESTAMP";
        return executeQuerySingle(sql, token);
    }
    
    /**
     * Finds all active vendors.
     * 
     * @return list of active vendors
     */
    public List<Vendor> findAllActive() {
        String sql = "SELECT * FROM vendors WHERE is_active = TRUE ORDER BY company_name";
        return executeQuery(sql);
    }
    
    /**
     * Creates a new vendor.
     * 
     * @param vendor the vendor to create
     * @return the created vendor ID
     */
    public Long create(Vendor vendor) {
        String sql = "INSERT INTO vendors (vendor_name, company_name, email, phone_number, password_hash, " +
                     "is_active, is_verified, email_verified, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, vendor);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating vendor", e);
        }
        
        return null;
    }
    
    /**
     * Updates vendor profile.
     * 
     * @param vendor the vendor to update
     * @return true if updated
     */
    public boolean updateProfile(Vendor vendor) {
        String sql = "UPDATE vendors SET vendor_name = ?, company_name = ?, email = ?, phone_number = ?, " +
                     "address = ?, city = ?, state = ?, pincode = ?, gst_number = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        return executeUpdate(sql, vendor.getVendorName(), vendor.getCompanyName(), vendor.getEmail(),
                vendor.getPhoneNumber(), vendor.getAddress(), vendor.getCity(), vendor.getState(),
                vendor.getPincode(), vendor.getGstNumber(), vendor.getId()) > 0;
    }
    
    /**
     * Updates vendor password.
     * 
     * @param vendorId the vendor ID
     * @param newPasswordHash the new password hash
     * @return true if updated
     */
    public boolean updatePassword(Long vendorId, String newPasswordHash) {
        String sql = "UPDATE vendors SET password_hash = ?, reset_token = NULL, reset_token_expiry = NULL, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, newPasswordHash, vendorId) > 0;
    }
    
    /**
     * Sets password reset token.
     * 
     * @param vendorId the vendor ID
     * @param token the reset token
     * @param expiry the token expiry time
     * @return true if updated
     */
    public boolean setResetToken(Long vendorId, String token, LocalDateTime expiry) {
        String sql = "UPDATE vendors SET reset_token = ?, reset_token_expiry = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, token, Timestamp.valueOf(expiry), vendorId) > 0;
    }
    
    /**
     * Clears password reset token.
     * 
     * @param vendorId the vendor ID
     * @return true if cleared
     */
    public boolean clearResetToken(Long vendorId) {
        String sql = "UPDATE vendors SET reset_token = NULL, reset_token_expiry = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, vendorId) > 0;
    }
    
    /**
     * Updates the last login timestamp.
     * 
     * @param vendorId the vendor ID
     * @return true if updated
     */
    public boolean updateLastLogin(Long vendorId) {
        String sql = "UPDATE vendors SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, vendorId) > 0;
    }
    
    /**
     * Toggles vendor active status.
     * 
     * @param vendorId the vendor ID
     * @return true if toggled
     */
    public boolean toggleActiveStatus(Long vendorId) {
        String sql = "UPDATE vendors SET is_active = NOT is_active, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, vendorId) > 0;
    }
    
    /**
     * Verifies a vendor.
     * 
     * @param vendorId the vendor ID
     * @return true if verified
     */
    public boolean verifyVendor(Long vendorId) {
        String sql = "UPDATE vendors SET is_verified = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, vendorId) > 0;
    }
    
    /**
     * Checks if an email exists.
     * 
     * @param email the email
     * @return true if exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM vendors WHERE email = ? LIMIT 1";
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
    
    /**
     * Checks if a phone number exists.
     * 
     * @param phone the phone number
     * @return true if exists
     */
    public boolean phoneExists(String phone) {
        String sql = "SELECT 1 FROM vendors WHERE phone_number = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking phone existence", e);
        }
    }
    
    /**
     * Gets vendor count by status.
     * 
     * @return array: [total, active, inactive]
     */
    public int[] getVendorCounts() {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN is_active THEN 1 ELSE 0 END) as active, " +
                     "SUM(CASE WHEN NOT is_active THEN 1 ELSE 0 END) as inactive FROM vendors";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new int[]{rs.getInt("total"), rs.getInt("active"), rs.getInt("inactive")};
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting vendor counts", e);
        }
        return new int[]{0, 0, 0};
    }
}
