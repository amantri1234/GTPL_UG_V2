package com.gtpl.daos;

import com.gtpl.models.MaterialRequest;
import com.gtpl.utils.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * MaterialRequest DAO Class
 * Data access operations for MaterialRequest entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class MaterialRequestDAO extends BaseDAO<MaterialRequest, Long> {
    
    @Override
    protected String getTableName() {
        return "material_requests";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected MaterialRequest mapResultSet(ResultSet rs) throws SQLException {
        MaterialRequest request = new MaterialRequest();
        request.setId(rs.getLong("id"));
        request.setRequestCode(rs.getString("request_code"));
        request.setProjectId(rs.getLong("project_id"));
        request.setVendorId(rs.getLong("vendor_id"));
        request.setMaterialId(rs.getLong("material_id"));
        request.setQuantityRequired(getBigDecimalOrNull(rs, "quantity_required"));
        request.setReason(rs.getString("reason"));
        
        String urgencyStr = rs.getString("urgency");
        if (urgencyStr != null) {
            request.setUrgency(MaterialRequest.Urgency.valueOf(urgencyStr));
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            request.setStatus(MaterialRequest.Status.valueOf(statusStr));
        }
        
        request.setApprovedQuantity(getBigDecimalOrNull(rs, "approved_quantity"));
        request.setApprovedByAdminId(getLongOrNull(rs, "approved_by_admin_id"));
        request.setApprovedAt(getLocalDateTimeOrNull(rs, "approved_at"));
        request.setRejectionReason(rs.getString("rejection_reason"));
        request.setRequestedAt(getLocalDateTimeOrNull(rs, "requested_at"));
        request.setFulfilledAt(getLocalDateTimeOrNull(rs, "fulfilled_at"));
        request.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        request.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        return request;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, MaterialRequest request) throws SQLException {
        stmt.setString(1, request.getRequestCode());
        stmt.setLong(2, request.getProjectId());
        stmt.setLong(3, request.getVendorId());
        stmt.setLong(4, request.getMaterialId());
        stmt.setBigDecimal(5, request.getQuantityRequired());
        stmt.setString(6, request.getReason());
        stmt.setString(7, request.getUrgency() != null ? request.getUrgency().name() : "MEDIUM");
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, MaterialRequest request) throws SQLException {
        stmt.setString(1, request.getStatus() != null ? request.getStatus().name() : "PENDING");
        stmt.setBigDecimal(2, request.getApprovedQuantity());
        stmt.setLong(3, request.getApprovedByAdminId());
        stmt.setString(4, request.getRejectionReason());
        stmt.setLong(5, request.getId());
    }
    
    /**
     * Creates a new material request with auto-generated code.
     * 
     * @param request the request to create
     * @return the created request ID
     */
    public Long create(MaterialRequest request) {
        // Generate request code: MR-YYYY-XXXXX
        String requestCode = generateRequestCode();
        request.setRequestCode(requestCode);
        
        String sql = "INSERT INTO material_requests (request_code, project_id, vendor_id, material_id, " +
                     "quantity_required, reason, urgency, status, requested_at, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, request);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating material request", e);
        }
        
        return null;
    }
    
    /**
     * Generates a unique request code.
     */
    private String generateRequestCode() {
        String year = String.valueOf(java.time.Year.now().getValue());
        String sql = "SELECT COUNT(*) FROM material_requests WHERE YEAR(created_at) = YEAR(CURRENT_DATE)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            
            return String.format("MR-%s-%05d", year, count + 1);
        } catch (SQLException e) {
            throw new RuntimeException("Error generating request code", e);
        }
    }
    
    /**
     * Finds requests by vendor ID.
     * 
     * @param vendorId the vendor ID
     * @return list of requests
     */
    public List<MaterialRequest> findByVendorId(Long vendorId) {
        String sql = "SELECT mr.*, p.project_name, p.project_code, v.vendor_name, " +
                     "m.material_name, m.material_code, m.unit " +
                     "FROM material_requests mr " +
                     "JOIN projects p ON mr.project_id = p.id " +
                     "JOIN vendors v ON mr.vendor_id = v.id " +
                     "JOIN materials m ON mr.material_id = m.id " +
                     "WHERE mr.vendor_id = ? ORDER BY mr.created_at DESC";
        return executeQueryWithJoins(sql, vendorId);
    }
    
    /**
     * Finds requests by project ID.
     * 
     * @param projectId the project ID
     * @return list of requests
     */
    public List<MaterialRequest> findByProjectId(Long projectId) {
        String sql = "SELECT mr.*, p.project_name, p.project_code, v.vendor_name, " +
                     "m.material_name, m.material_code, m.unit " +
                     "FROM material_requests mr " +
                     "JOIN projects p ON mr.project_id = p.id " +
                     "JOIN vendors v ON mr.vendor_id = v.id " +
                     "JOIN materials m ON mr.material_id = m.id " +
                     "WHERE mr.project_id = ? ORDER BY mr.created_at DESC";
        return executeQueryWithJoins(sql, projectId);
    }
    
    /**
     * Finds requests by status.
     * 
     * @param status the request status
     * @return list of requests
     */
    public List<MaterialRequest> findByStatus(MaterialRequest.Status status) {
        String sql = "SELECT mr.*, p.project_name, p.project_code, v.vendor_name, " +
                     "m.material_name, m.material_code, m.unit, a.full_name as admin_name " +
                     "FROM material_requests mr " +
                     "JOIN projects p ON mr.project_id = p.id " +
                     "JOIN vendors v ON mr.vendor_id = v.id " +
                     "JOIN materials m ON mr.material_id = m.id " +
                     "LEFT JOIN admins a ON mr.approved_by_admin_id = a.id " +
                     "WHERE mr.status = ? ORDER BY mr.urgency DESC, mr.created_at";
        return executeQueryWithJoins(sql, status.name());
    }
    
    /**
     * Finds pending requests.
     * 
     * @return list of pending requests
     */
    public List<MaterialRequest> findPending() {
        return findByStatus(MaterialRequest.Status.PENDING);
    }
    
    /**
     * Approves a material request.
     * 
     * @param requestId the request ID
     * @param approvedQuantity the approved quantity
     * @param adminId the approving admin ID
     * @return true if approved
     */
    public boolean approve(Long requestId, BigDecimal approvedQuantity, Long adminId) {
        String sql = "UPDATE material_requests SET status = 'APPROVED', approved_quantity = ?, " +
                     "approved_by_admin_id = ?, approved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        return executeUpdate(sql, approvedQuantity, adminId, requestId) > 0;
    }
    
    /**
     * Rejects a material request.
     * 
     * @param requestId the request ID
     * @param reason the rejection reason
     * @param adminId the rejecting admin ID
     * @return true if rejected
     */
    public boolean reject(Long requestId, String reason, Long adminId) {
        String sql = "UPDATE material_requests SET status = 'REJECTED', rejection_reason = ?, " +
                     "approved_by_admin_id = ?, approved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        return executeUpdate(sql, reason, adminId, requestId) > 0;
    }
    
    /**
     * Marks a request as fulfilled.
     * 
     * @param requestId the request ID
     * @return true if fulfilled
     */
    public boolean fulfill(Long requestId) {
        String sql = "UPDATE material_requests SET status = 'FULFILLED', fulfilled_at = CURRENT_TIMESTAMP, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, requestId) > 0;
    }
    
    /**
     * Gets request statistics.
     * 
     * @return array: [total, pending, approved, rejected, fulfilled]
     */
    public int[] getRequestStats() {
        String sql = "SELECT " +
                     "COUNT(*) as total, " +
                     "SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                     "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approved, " +
                     "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected, " +
                     "SUM(CASE WHEN status = 'FULFILLED' THEN 1 ELSE 0 END) as fulfilled " +
                     "FROM material_requests";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new int[]{
                    rs.getInt("total"),
                    rs.getInt("pending"),
                    rs.getInt("approved"),
                    rs.getInt("rejected"),
                    rs.getInt("fulfilled")
                };
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting request stats", e);
        }
        return new int[]{0, 0, 0, 0, 0};
    }
    
    /**
     * Gets pending requests count for a vendor.
     * 
     * @param vendorId the vendor ID
     * @return count of pending requests
     */
    public int getPendingCountByVendor(Long vendorId) {
        String sql = "SELECT COUNT(*) FROM material_requests WHERE vendor_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vendorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting pending requests count", e);
        }
        return 0;
    }
    
    /**
     * Executes a query with joins and maps results.
     */
    private List<MaterialRequest> executeQueryWithJoins(String sql, Object... params) {
        List<MaterialRequest> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MaterialRequest request = mapResultSet(rs);
                    request.setProjectName(rs.getString("project_name"));
                    request.setProjectCode(rs.getString("project_code"));
                    request.setVendorName(rs.getString("vendor_name"));
                    request.setMaterialName(rs.getString("material_name"));
                    request.setMaterialCode(rs.getString("material_code"));
                    request.setUnit(rs.getString("unit"));
                    try {
                        request.setAdminName(rs.getString("admin_name"));
                    } catch (SQLException e) {
                        // Column might not exist in all queries
                    }
                    results.add(request);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query with joins", e);
        }
        
        return results;
    }
}
