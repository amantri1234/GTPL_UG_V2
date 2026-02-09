package com.gtpl.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Configuration Class
 * Manages database connection pooling using HikariCP.
 * 
 * Configuration can be provided via:
 * 1. Environment variables (for production)
 * 2. System properties
 * 3. Default values (for development)
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class DatabaseConfig {
    
    // Default configuration values
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/gtpl_ug_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "password";
    private static final String DEFAULT_DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings
    private static final int MAX_POOL_SIZE = 20;
    private static final int MIN_IDLE = 5;
    private static final long CONNECTION_TIMEOUT = 30000; // 30 seconds
    private static final long IDLE_TIMEOUT = 600000; // 10 minutes
    private static final long MAX_LIFETIME = 1800000; // 30 minutes
    
    private static HikariDataSource dataSource;
    
    /**
     * Initializes the database connection pool.
     * Should be called once at application startup.
     */
    public static void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return; // Already initialized
        }
        
        try {
            HikariConfig config = new HikariConfig();
            
            // Database connection settings
            config.setJdbcUrl(getDbUrl());
            config.setUsername(getDbUser());
            config.setPassword(getDbPassword());
            config.setDriverClassName(getDbDriver());
            
            // Connection pool settings
            config.setMaximumPoolSize(MAX_POOL_SIZE);
            config.setMinimumIdle(MIN_IDLE);
            config.setConnectionTimeout(CONNECTION_TIMEOUT);
            config.setIdleTimeout(IDLE_TIMEOUT);
            config.setMaxLifetime(MAX_LIFETIME);
            config.setPoolName("GTPL_UG_ConnectionPool");
            
            // Performance optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // Connection test query
            config.setConnectionTestQuery("SELECT 1");
            
            // Create the data source
            dataSource = new HikariDataSource(config);
            
            System.out.println("Database connection pool initialized successfully");
            System.out.println("Database URL: " + getDbUrl());
            System.out.println("Pool Size: " + MAX_POOL_SIZE);
            
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Gets a connection from the pool.
     * 
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes the connection pool.
     * Should be called at application shutdown.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed");
        }
    }
    
    /**
     * Gets the data source (for advanced use cases).
     */
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Checks if the database connection is healthy.
     */
    public static boolean isHealthy() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Private helper methods to get configuration values
    
    private static String getDbUrl() {
        // Check for Railway's DATABASE_URL first
        String railwayUrl = System.getenv("DATABASE_URL");
        if (railwayUrl != null && !railwayUrl.isEmpty()) {
            // Convert Railway's mysql:// URL to jdbc:mysql:// format
            if (railwayUrl.startsWith("mysql://")) {
                return railwayUrl.replace("mysql://", "jdbc:mysql://") + "?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            }
            return railwayUrl;
        }
        
        // Check for standard DB_URL
        String url = System.getenv("DB_URL");
        if (url != null && !url.isEmpty()) {
            return url;
        }
        
        // Build URL from individual Railway MySQL variables
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String database = System.getenv("MYSQLDATABASE");
        
        if (host != null && port != null && database != null) {
            return String.format("jdbc:mysql://%s:%s/%s?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
                host, port, database);
        }
        
        return System.getProperty("db.url", DEFAULT_DB_URL);
    }
    
    private static String getDbUser() {
        // Check for Railway's MYSQLUSER
        String railwayUser = System.getenv("MYSQLUSER");
        if (railwayUser != null && !railwayUser.isEmpty()) {
            return railwayUser;
        }
        
        String user = System.getenv("DB_USER");
        if (user != null && !user.isEmpty()) {
            return user;
        }
        
        return System.getProperty("db.user", DEFAULT_DB_USER);
    }
    
    private static String getDbPassword() {
        // Check for Railway's MYSQLPASSWORD
        String railwayPassword = System.getenv("MYSQLPASSWORD");
        if (railwayPassword != null && !railwayPassword.isEmpty()) {
            return railwayPassword;
        }
        
        String password = System.getenv("DB_PASSWORD");
        if (password != null && !password.isEmpty()) {
            return password;
        }
        
        return System.getProperty("db.password", DEFAULT_DB_PASSWORD);
    }
    
    private static String getDbDriver() {
        String driver = System.getenv("DB_DRIVER");
        if (driver != null && !driver.isEmpty()) {
            return driver;
        }
        return System.getProperty("db.driver", DEFAULT_DB_DRIVER);
    }
    
    /**
     * Utility class for database operations with auto-closing resources.
     */
    public static class DatabaseOperation<T> {
        
        @FunctionalInterface
        public interface ConnectionConsumer<T> {
            T accept(Connection conn) throws SQLException;
        }
        
        /**
         * Executes a database operation with proper resource management.
         * 
         * @param operation the operation to execute
         * @return the result of the operation
         * @throws SQLException if an error occurs
         */
        public static <T> T execute(ConnectionConsumer<T> operation) throws SQLException {
            try (Connection conn = getConnection()) {
                return operation.accept(conn);
            }
        }
        
        /**
         * Executes a database operation with transaction support.
         * 
         * @param operation the operation to execute
         * @return the result of the operation
         * @throws SQLException if an error occurs
         */
        public static <T> T executeTransaction(ConnectionConsumer<T> operation) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    T result = operation.accept(conn);
                    conn.commit();
                    return result;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }
    }
}
