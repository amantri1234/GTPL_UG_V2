# Railway Deployment Fixes Summary

## Issues Fixed

### 1. Missing Dockerfile
**Problem:** Railway needs a Dockerfile to build Java applications.

**Solution:** Created `backend/Dockerfile` with multi-stage build:
- Stage 1: Build using Gradle
- Stage 2: Runtime using Eclipse Temurin JRE 17
- Includes health check
- Runs as non-root user for security

---

### 2. Missing Health Check Endpoint
**Problem:** Railway requires a health check endpoint for deployment verification.

**Solution:** Added `/health` endpoint in `Application.java`:
```java
app.get("/health", ctx -> {
    ctx.json(new HealthResponse("UP", "1.0.0", System.currentTimeMillis()));
});
```

---

### 3. Database Connection Issues
**Problem:** Railway provides database credentials differently than local development.

**Solution:** Updated `DatabaseConfig.java` to handle Railway's environment variables:
- `DATABASE_URL` (Railway's full JDBC URL)
- `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`
- `MYSQLUSER`, `MYSQLPASSWORD`
- Falls back to `DB_URL`, `DB_USER`, `DB_PASSWORD` for other platforms

---

### 4. Port Configuration
**Problem:** Railway dynamically assigns ports via `PORT` environment variable.

**Solution:** 
- `Application.java` already reads `PORT` env variable
- `Dockerfile` uses `${PORT:-8080}` with fallback
- `railway.json` specifies correct start command

---

### 5. Build Configuration
**Problem:** Need consistent JAR file naming for Docker.

**Solution:** Updated `build.gradle`:
```gradle
shadowJar {
    archiveBaseName.set('gtpl-ug-system')
    archiveClassifier.set('')
    archiveVersion.set('')
    // ...
}
```

---

## Files Created/Modified

### New Files
1. `backend/Dockerfile` - Multi-stage Docker build
2. `backend/railway.json` - Railway deployment config
3. `backend/nixpacks.toml` - Alternative build config
4. `backend/system.properties` - Java version spec
5. `backend/.dockerignore` - Docker ignore patterns
6. `backend/Procfile` - Alternative deployment method
7. `RAILWAY_DEPLOYMENT.md` - Complete deployment guide

### Modified Files
1. `backend/src/main/java/com/gtpl/Application.java`
   - Added `/health` endpoint
   - Added `HealthResponse` class

2. `backend/src/main/java/com/gtpl/utils/DatabaseConfig.java`
   - Added Railway environment variable support
   - Added `DATABASE_URL` parsing
   - Added individual MySQL variable support

3. `backend/build.gradle`
   - Fixed archive base name

---

## Deployment Steps

1. **Push code to GitHub**
   ```bash
   git add .
   git commit -m "Add Railway deployment config"
   git push origin main
   ```

2. **Create Railway project**
   - New Project → Deploy from GitHub repo

3. **Add MySQL database**
   - New → Database → MySQL

4. **Import database schema**
   ```bash
   railway connect mysql
   source database/schema.sql
   ```

5. **Deploy**
   - Railway auto-deploys on push
   - Check logs for any issues

---

## Environment Variables

Railway will automatically set these when you add MySQL:
- `DATABASE_URL` or `MYSQL_URL`
- `MYSQLHOST`
- `MYSQLPORT`
- `MYSQLDATABASE`
- `MYSQLUSER`
- `MYSQLPASSWORD`
- `PORT` (set by Railway)

---

## Troubleshooting Checklist

- [ ] Dockerfile exists in `backend/` directory
- [ ] `system.properties` specifies Java 17
- [ ] MySQL service is added to project
- [ ] Database schema is imported
- [ ] Environment variables are set
- [ ] `/health` endpoint returns 200 OK
- [ ] Application binds to `0.0.0.0` (not just localhost)
- [ ] Port uses `PORT` environment variable

---

## Common Errors & Fixes

### "No Dockerfile found"
- Ensure `Dockerfile` is in `backend/` directory
- Or use Nixpacks (auto-detected)

### "Cannot connect to database"
- Check MySQL service is provisioned
- Verify `DATABASE_URL` or MySQL variables exist
- Check DatabaseConfig logs

### "Health check failed"
- Verify `/health` endpoint is accessible
- Check application starts within 60 seconds
- Review application logs

### "Port already in use"
- Ensure using `PORT` environment variable
- Check no hardcoded port in application

---

## Success Indicators

✅ Build completes without errors  
✅ Health check returns `{"status":"UP"}`  
✅ Application logs show "Started successfully"  
✅ Can access login page via Railway domain  
✅ Can login with admin credentials  
