package com.gtpl.daos;

import com.gtpl.utils.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base DAO Class
 * Provides common CRUD operations and database access utilities.
 * All entity-specific DAOs should extend this class.
 * 
 * @param <T> the entity type
 * @param <ID> the ID type (usually Long)
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public abstract class BaseDAO<T, ID> {
    
    /**
     * Gets the table name for this entity.
     * 
     * @return the table name
     */
    protected abstract String getTableName();
    
    /**
     * Gets the primary key column name.
     * 
     * @return the primary key column name
     */
    protected abstract String getPrimaryKeyColumn();
    
    /**
     * Maps a ResultSet row to an entity.
     * 
     * @param rs the ResultSet
     * @return the mapped entity
     * @throws SQLException if mapping fails
     */
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    
    /**
     * Sets parameters for an insert statement.
     * 
     * @param stmt the PreparedStatement
     * @param entity the entity to insert
     * @throws SQLException if parameter setting fails
     */
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Sets parameters for an update statement.
     * 
     * @param stmt the PreparedStatement
     * @param entity the entity to update
     * @throws SQLException if parameter setting fails
     */
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Finds an entity by its ID.
     * 
     * @param id the entity ID
     * @return Optional containing the entity if found
     */
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding entity by ID: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all entities.
     * 
     * @return list of all entities
     */
    public List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName();
        List<T> entities = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                entities.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all entities", e);
        }
        
        return entities;
    }
    
    /**
     * Finds entities with pagination.
     * 
     * @param page the page number (0-based)
     * @param size the page size
     * @return list of entities for the page
     */
    public List<T> findAll(int page, int size) {
        String sql = "SELECT * FROM " + getTableName() + " LIMIT ? OFFSET ?";
        List<T> entities = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding entities with pagination", e);
        }
        
        return entities;
    }
    
    /**
     * Counts all entities.
     * 
     * @return the count
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting entities", e);
        }
        
        return 0;
    }
    
    /**
     * Checks if an entity exists by ID.
     * 
     * @param id the entity ID
     * @return true if exists
     */
    public boolean existsById(ID id) {
        String sql = "SELECT 1 FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ? LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking entity existence: " + id, e);
        }
    }
    
    /**
     * Deletes an entity by ID.
     * 
     * @param id the entity ID
     * @return true if deleted
     */
    public boolean deleteById(ID id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getPrimaryKeyColumn() + " = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting entity: " + id, e);
        }
    }
    
    /**
     * Executes a query with parameters and returns a list of entities.
     * 
     * @param sql the SQL query
     * @param params the query parameters
     * @return list of entities
     */
    protected List<T> executeQuery(String sql, Object... params) {
        List<T> entities = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
        
        return entities;
    }
    
    /**
     * Executes a query and returns a single optional result.
     * 
     * @param sql the SQL query
     * @param params the query parameters
     * @return Optional containing the entity if found
     */
    protected Optional<T> executeQuerySingle(String sql, Object... params) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Executes an update (INSERT, UPDATE, DELETE) and returns affected rows.
     * 
     * @param sql the SQL statement
     * @param params the statement parameters
     * @return number of affected rows
     */
    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing update", e);
        }
    }
    
    /**
     * Executes an insert and returns the generated key.
     * 
     * @param sql the INSERT statement
     * @param entity the entity to insert
     * @return the generated key
     */
    protected Long executeInsert(String sql, T entity) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setInsertParameters(stmt, entity);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing insert", e);
        }
        
        return null;
    }
    
    /**
     * Sets parameters for a PreparedStatement.
     * 
     * @param stmt the PreparedStatement
     * @param params the parameters
     * @throws SQLException if parameter setting fails
     */
    protected void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
    
    /**
     * Gets a Long value from ResultSet, handling null.
     * 
     * @param rs the ResultSet
     * @param column the column name
     * @return the Long value or null
     * @throws SQLException if reading fails
     */
    protected Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
    
    /**
     * Gets an Integer value from ResultSet, handling null.
     * 
     * @param rs the ResultSet
     * @param column the column name
     * @return the Integer value or null
     * @throws SQLException if reading fails
     */
    protected Integer getIntOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
    
    /**
     * Gets a java.time.LocalDate from ResultSet, handling null.
     * 
     * @param rs the ResultSet
     * @param column the column name
     * @return the LocalDate value or null
     * @throws SQLException if reading fails
     */
    protected java.time.LocalDate getLocalDateOrNull(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date != null ? date.toLocalDate() : null;
    }
    
    /**
     * Gets a java.time.LocalDateTime from ResultSet, handling null.
     * 
     * @param rs the ResultSet
     * @param column the column name
     * @return the LocalDateTime value or null
     * @throws SQLException if reading fails
     */
    protected java.time.LocalDateTime getLocalDateTimeOrNull(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    
    /**
     * Gets a BigDecimal from ResultSet, handling null.
     * 
     * @param rs the ResultSet
     * @param column the column name
     * @return the BigDecimal value or null
     * @throws SQLException if reading fails
     */
    protected java.math.BigDecimal getBigDecimalOrNull(ResultSet rs, String column) throws SQLException {
        java.math.BigDecimal value = rs.getBigDecimal(column);
        return rs.wasNull() ? null : value;
    }
}
