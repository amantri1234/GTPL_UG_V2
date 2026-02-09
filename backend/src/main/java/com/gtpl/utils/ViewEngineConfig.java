package com.gtpl.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * View Engine Configuration Class
 * Configures Thymeleaf template engine for server-side HTML rendering.
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */
public class ViewEngineConfig {
    
    private static TemplateEngine templateEngine;
    
    /**
     * Gets the configured Thymeleaf template engine.
     * Creates it if not already initialized.
     * 
     * @return Configured TemplateEngine instance
     */
    public static TemplateEngine getTemplateEngine() {
        if (templateEngine == null) {
            templateEngine = createTemplateEngine();
        }
        return templateEngine;
    }
    
    /**
     * Creates and configures the Thymeleaf template engine.
     */
    private static TemplateEngine createTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        
        // Add template resolver
        engine.addTemplateResolver(createTemplateResolver());
        
        // Add Java 8 time dialect for date/time formatting
        engine.addDialect(new Java8TimeDialect());
        
        // Add custom dialects if needed
        // engine.addDialect(new CustomDialect());
        
        return engine;
    }
    
    /**
     * Creates the template resolver for Thymeleaf.
     */
    private static ITemplateResolver createTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        
        // Template location (in resources/templates directory)
        resolver.setPrefix("/templates/");
        
        // Template suffix
        resolver.setSuffix(".html");
        
        // Template mode (HTML5)
        resolver.setTemplateMode(TemplateMode.HTML);
        
        // Character encoding
        resolver.setCharacterEncoding("UTF-8");
        
        // Cache settings
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(3600000L); // 1 hour cache
        
        // Check for template updates in development
        String env = System.getenv("APP_ENV");
        if ("development".equalsIgnoreCase(env) || "dev".equalsIgnoreCase(env)) {
            resolver.setCacheable(false);
        }
        
        return resolver;
    }
    
    /**
     * Reloads the template engine (useful in development).
     */
    public static void reload() {
        templateEngine = null;
        getTemplateEngine();
    }
}
