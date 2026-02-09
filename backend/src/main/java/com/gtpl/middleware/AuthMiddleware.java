package com.gtpl.middleware;

import com.gtpl.utils.SessionUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

/**
 * Authentication Middleware Class
 * Provides middleware handlers for route protection and authentication checks.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class AuthMiddleware {
    
    /**
     * Middleware to require admin role.
     * Redirects to login if not authenticated or not admin.
     */
    public void requireAdmin(Context ctx) {
        if (!SessionUtils.isLoggedIn(ctx)) {
            ctx.sessionAttribute("redirectAfterLogin", ctx.path());
            ctx.redirect("/login");
            return;
        }
        
        if (!SessionUtils.isAdmin(ctx)) {
            // User is logged in but not an admin
            if (isApiRequest(ctx)) {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.json(new ErrorResponse("Admin access required", 403));
            } else {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.render("error/403.html");
            }
            return;
        }
        
        // Continue to the handler
    }
    
    /**
     * Middleware to require vendor role.
     * Redirects to login if not authenticated or not vendor.
     */
    public void requireVendor(Context ctx) {
        if (!SessionUtils.isLoggedIn(ctx)) {
            ctx.sessionAttribute("redirectAfterLogin", ctx.path());
            ctx.redirect("/login");
            return;
        }
        
        if (!SessionUtils.isVendor(ctx)) {
            // User is logged in but not a vendor
            if (isApiRequest(ctx)) {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.json(new ErrorResponse("Vendor access required", 403));
            } else {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.render("error/403.html");
            }
            return;
        }
        
        // Continue to the handler
    }
    
    /**
     * Middleware to require any authenticated user.
     * Redirects to login if not authenticated.
     */
    public void requireAuthenticated(Context ctx) {
        if (!SessionUtils.isLoggedIn(ctx)) {
            if (isApiRequest(ctx)) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                ctx.json(new ErrorResponse("Authentication required", 401));
            } else {
                ctx.sessionAttribute("redirectAfterLogin", ctx.path());
                ctx.redirect("/login");
            }
            return;
        }
        
        // Continue to the handler
    }
    
    /**
     * Checks if the request is an API request.
     */
    private boolean isApiRequest(Context ctx) {
        String path = ctx.path();
        String acceptHeader = ctx.header("Accept");
        
        return path.startsWith("/api/") || 
               (acceptHeader != null && acceptHeader.contains("application/json"));
    }
    
    /**
     * Error response class for JSON errors.
     */
    public static class ErrorResponse {
        public final String error;
        public final int code;
        public final long timestamp;
        
        public ErrorResponse(String error, int code) {
            this.error = error;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
