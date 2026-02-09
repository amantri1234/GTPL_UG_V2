# GTPL_UG Management System - Dockerfile for Railway Deployment
# Multi-stage build for optimized image size

# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Copy build files first for better caching
COPY backend/build.gradle backend/settings.gradle backend/gradlew ./
COPY backend/gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY backend/src ./src

# Build the application
RUN ./gradlew shadowJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Install necessary utilities
RUN apk add --no-cache curl

WORKDIR /app

# Create non-root user for security
RUN addgroup -S gtpl && adduser -S gtpl -G gtpl

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/gtpl-ug-system.jar app.jar

# Change ownership to non-root user
RUN chown -R gtpl:gtpl /app

# Switch to non-root user
USER gtpl

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run the application
# Railway provides PORT env variable, default to 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
