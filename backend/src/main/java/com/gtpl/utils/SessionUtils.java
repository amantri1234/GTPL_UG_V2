package com.gtpl.utils;

import io.javalin.http.Context;

/**
 * Session Utility Class
 * Manages user session data and provides helper methods for session operations.
 * 
 * Session Attributes:
 * - userId: The user's ID
 * - role: The user's role (ADMIN or VENDOR)
 * - username: The user's username/email
 * - fullName: The user's full name
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class SessionUtils {
    
    // Session attribute keys
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_ROLE = "role";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_FULL_NAME = "fullName";
    public static final String SESSION_LAST_ACTIVITY = "lastActivity";
    
    // User roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_VENDOR = "VENDOR";
    
    // Session timeout in milliseconds (2 hours)
    private static final long SESSION_TIMEOUT = 2 * 60 * 60 * 1000;
    
    /**
     * Private constructor to prevent instantiation
     */
    private SessionUtils() {
    }
    
    /**
     * Creates a session for an admin user.
     * 
     * @param ctx the Javalin context
     * @param userId the admin's ID
     * @param username the admin's username
     * @param fullName the admin's full name
     */
    public static void createAdminSession(Context ctx, Long userId, String username, String fullName) {
        ctx.sessionAttribute(SESSION_USER_ID, userId);
        ctx.sessionAttribute(SESSION_ROLE, ROLE_ADMIN);
        ctx.sessionAttribute(SESSION_USERNAME, username);
        ctx.sessionAttribute(SESSION_FULL_NAME, fullName);
        ctx.sessionAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
    }
    
    /**
     * Creates a session for a vendor user.
     * 
     * @param ctx the Javalin context
     * @param userId the vendor's ID
     * @param email the vendor's email
     * @param fullName the vendor's full name
     */
    public static void createVendorSession(Context ctx, Long userId, String email, String fullName) {
        ctx.sessionAttribute(SESSION_USER_ID, userId);
        ctx.sessionAttribute(SESSION_ROLE, ROLE_VENDOR);
        ctx.sessionAttribute(SESSION_USERNAME, email);
        ctx.sessionAttribute(SESSION_FULL_NAME, fullName);
        ctx.sessionAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
    }
    
    /**
     * Invalidates the current session.
     * 
     * @param ctx the Javalin context
     */
    public static void invalidateSession(Context ctx) {
        ctx.consumeSessionAttribute(SESSION_USER_ID);
        ctx.consumeSessionAttribute(SESSION_ROLE);
        ctx.consumeSessionAttribute(SESSION_USERNAME);
        ctx.consumeSessionAttribute(SESSION_FULL_NAME);
        ctx.consumeSessionAttribute(SESSION_LAST_ACTIVITY);
        ctx.req().getSession().invalidate();
    }
    
    /**
     * Checks if the user is logged in.
     * 
     * @param ctx the Javalin context
     * @return true if user is logged in, false otherwise
     */
    public static boolean isLoggedIn(Context ctx) {
        Long userId = ctx.sessionAttribute(SESSION_USER_ID);
        String role = ctx.sessionAttribute(SESSION_ROLE);
        
        if (userId == null || role == null) {
            return false;
        }
        
        // Check session timeout
        Long lastActivity = ctx.sessionAttribute(SESSION_LAST_ACTIVITY);
        if (lastActivity != null) {
            long inactiveTime = System.currentTimeMillis() - lastActivity;
            if (inactiveTime > SESSION_TIMEOUT) {
                invalidateSession(ctx);
                return false;
            }
        }
        
        // Update last activity
        ctx.sessionAttribute(SESSION_LAST_ACTIVITY, System.currentTimeMillis());
        
        return true;
    }
    
    /**
     * Checks if the logged-in user is an admin.
     * 
     * @param ctx the Javalin context
     * @return true if user is admin, false otherwise
     */
    public static boolean isAdmin(Context ctx) {
        return ROLE_ADMIN.equals(ctx.sessionAttribute(SESSION_ROLE));
    }
    
    /**
     * Checks if the logged-in user is a vendor.
     * 
     * @param ctx the Javalin context
     * @return true if user is vendor, false otherwise
     */
    public static boolean isVendor(Context ctx) {
        return ROLE_VENDOR.equals(ctx.sessionAttribute(SESSION_ROLE));
    }
    
    /**
     * Gets the current user's ID from session.
     * 
     * @param ctx the Javalin context
     * @return the user ID, or null if not logged in
     */
    public static Long getUserId(Context ctx) {
        return ctx.sessionAttribute(SESSION_USER_ID);
    }
    
    /**
     * Gets the current user's role from session.
     * 
     * @param ctx the Javalin context
     * @return the role, or null if not logged in
     */
    public static String getRole(Context ctx) {
        return ctx.sessionAttribute(SESSION_ROLE);
    }
    
    /**
     * Gets the current user's username/email from session.
     * 
     * @param ctx the Javalin context
     * @return the username, or null if not logged in
     */
    public static String getUsername(Context ctx) {
        return ctx.sessionAttribute(SESSION_USERNAME);
    }
    
    /**
     * Gets the current user's full name from session.
     * 
     * @param ctx the Javalin context
     * @return the full name, or null if not logged in
     */
    public static String getFullName(Context ctx) {
        return ctx.sessionAttribute(SESSION_FULL_NAME);
    }
    
    /**
     * Updates the user's full name in session.
     * 
     * @param ctx the Javalin context
     * @param fullName the new full name
     */
    public static void updateFullName(Context ctx, String fullName) {
        ctx.sessionAttribute(SESSION_FULL_NAME, fullName);
    }
    
    /**
     * Requires authentication - throws exception if not logged in.
     * 
     * @param ctx the Javalin context
     * @throws SecurityException if not logged in
     */
    public static void requireAuth(Context ctx) {
        if (!isLoggedIn(ctx)) {
            throw new SecurityException("Authentication required");
        }
    }
    
    /**
     * Requires admin role - throws exception if not admin.
     * 
     * @param ctx the Javalin context
     * @throws SecurityException if not admin
     */
    public static void requireAdmin(Context ctx) {
        requireAuth(ctx);
        if (!isAdmin(ctx)) {
            throw new SecurityException("Admin access required");
        }
    }
    
    /**
     * Requires vendor role - throws exception if not vendor.
     * 
     * @param ctx the Javalin context
     * @throws SecurityException if not vendor
     */
    public static void requireVendor(Context ctx) {
        requireAuth(ctx);
        if (!isVendor(ctx)) {
            throw new SecurityException("Vendor access required");
        }
    }
    
    /**
     * Gets session info for debugging/logging.
     * 
     * @param ctx the Javalin context
     * @return session info string
     */
    public static String getSessionInfo(Context ctx) {
        return String.format("Session[userId=%s, role=%s, username=%s]",
                getUserId(ctx), getRole(ctx), getUsername(ctx));
    }
}
