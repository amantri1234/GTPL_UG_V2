package com.gtpl.daos;

import com.gtpl.models.Material;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Material DAO Class
 * Data access operations for Material entities.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class MaterialDAO extends BaseDAO<Material, Long> {
    
    @Override
    protected String getTableName() {
        return "materials";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "id";
    }
    
    @Override
    protected Material mapResultSet(ResultSet rs) throws SQLException {
        Material material = new Material();
        material.setId(rs.getLong("id"));
        material.setMaterialCode(rs.getString("material_code"));
        material.setMaterialName(rs.getString("material_name"));
        material.setDescription(rs.getString("description"));
        
        String unitStr = rs.getString("unit");
        if (unitStr != null) {
            material.setUnit(Material.Unit.valueOf(unitStr));
        }
        
        material.setCategory(rs.getString("category"));
        material.setStandardQuantity(getBigDecimalOrNull(rs, "standard_quantity"));
        material.setActive(rs.getBoolean("is_active"));
        material.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        material.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        return material;
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Material material) throws SQLException {
        stmt.setString(1, material.getMaterialCode());
        stmt.setString(2, material.getMaterialName());
        stmt.setString(3, material.getDescription());
        stmt.setString(4, material.getUnit() != null ? material.getUnit().name() : null);
        stmt.setString(5, material.getCategory());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Material material) throws SQLException {
        stmt.setString(1, material.getMaterialName());
        stmt.setString(2, material.getDescription());
        stmt.setString(3, material.getUnit() != null ? material.getUnit().name() : null);
        stmt.setString(4, material.getCategory());
        stmt.setBigDecimal(5, material.getStandardQuantity());
        stmt.setBoolean(6, material.isActive());
        stmt.setLong(7, material.getId());
    }
    
    /**
     * Creates a new material.
     * 
     * @param material the material to create
     * @return the created material ID
     */
    public Long create(Material material) {
        String sql = "INSERT INTO materials (material_code, material_name, description, unit, category, " +
                     "is_active, created_at) VALUES (?, ?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP)";
        return executeInsert(sql, material);
    }
    
    /**
     * Updates a material.
     * 
     * @param material the material to update
     * @return true if updated
     */
    public boolean update(Material material) {
        String sql = "UPDATE materials SET material_name = ?, description = ?, unit = ?, category = ?, " +
                     "standard_quantity = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, material.getMaterialName(), material.getDescription(),
                material.getUnit() != null ? material.getUnit().name() : null,
                material.getCategory(), material.getStandardQuantity(), material.isActive(), material.getId()) > 0;
    }
    
    /**
     * Finds a material by its code.
     * 
     * @param materialCode the material code
     * @return Optional containing the material if found
     */
    public Optional<Material> findByMaterialCode(String materialCode) {
        String sql = "SELECT * FROM materials WHERE material_code = ?";
        return executeQuerySingle(sql, materialCode);
    }
    
    /**
     * Finds all active materials.
     * 
     * @return list of active materials
     */
    public List<Material> findAllActive() {
        String sql = "SELECT * FROM materials WHERE is_active = TRUE ORDER BY category, material_name";
        return executeQuery(sql);
    }
    
    /**
     * Finds materials by category.
     * 
     * @param category the category
     * @return list of materials
     */
    public List<Material> findByCategory(String category) {
        String sql = "SELECT * FROM materials WHERE category = ? AND is_active = TRUE ORDER BY material_name";
        return executeQuery(sql, category);
    }
    
    /**
     * Finds materials by name (partial match).
     * 
     * @param name the name to search
     * @return list of materials
     */
    public List<Material> findByNameContaining(String name) {
        String sql = "SELECT * FROM materials WHERE material_name LIKE ? AND is_active = TRUE ORDER BY material_name";
        return executeQuery(sql, "%" + name + "%");
    }
    
    /**
     * Gets all unique categories.
     * 
     * @return list of categories
     */
    public List<String> findAllCategories() {
        String sql = "SELECT DISTINCT category FROM materials WHERE category IS NOT NULL ORDER BY category";
        List<String> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding categories", e);
        }
        
        return categories;
    }
    
    /**
     * Toggles material active status.
     * 
     * @param materialId the material ID
     * @return true if toggled
     */
    public boolean toggleActiveStatus(Long materialId) {
        String sql = "UPDATE materials SET is_active = NOT is_active, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql, materialId) > 0;
    }
    
    /**
     * Checks if a material code exists.
     * 
     * @param materialCode the material code
     * @return true if exists
     */
    public boolean materialCodeExists(String materialCode) {
        String sql = "SELECT 1 FROM materials WHERE material_code = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, materialCode);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking material code existence", e);
        }
    }
    
    /**
     * Gets material count by category.
     * 
     * @return list of [category, count] pairs
     */
    public List<Object[]> getMaterialCountsByCategory() {
        String sql = "SELECT category, COUNT(*) as count FROM materials WHERE is_active = TRUE GROUP BY category ORDER BY category";
        List<Object[]> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                results.add(new Object[]{rs.getString("category"), rs.getInt("count")});
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting material counts by category", e);
        }
        
        return results;
    }
}
