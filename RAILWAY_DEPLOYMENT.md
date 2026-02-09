# Railway Deployment Guide - GTPL_UG Management System

This guide will help you deploy the GTPL_UG Management System to Railway.

---

## Prerequisites

1. [Railway account](https://railway.app/)
2. [GitHub account](https://github.com/) (to push your code)

---

## Step 1: Push Code to GitHub

```bash
# Initialize git (if not already done)
cd /mnt/okcomputer/output/gtpl_ug_system
git init
git add .
git commit -m "Initial commit"

# Create a new repository on GitHub and push
git remote add origin https://github.com/YOUR_USERNAME/gtpl-ug-system.git
git branch -M main
git push -u origin main
```

---

## Step 2: Create Railway Project

1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your `gtpl-ug-system` repository
5. Click **"Add Variables"** (we'll add them in the next step)

---

## Step 3: Add MySQL Database

1. In your Railway project, click **"New"**
2. Select **"Database"** → **"Add MySQL"**
3. Railway will automatically create the database and add environment variables

**Railway will automatically set these variables:**
- `DATABASE_URL` or `MYSQL_URL`
- `MYSQLHOST`
- `MYSQLPORT`
- `MYSQLDATABASE`
- `MYSQLUSER`
- `MYSQLPASSWORD`

---

## Step 4: Configure Environment Variables

Go to your service variables and add/modify these:

### Required Variables

| Variable | Value | Description |
|----------|-------|-------------|
| `PORT` | `8080` | Application port (Railway overrides this) |

### Optional Variables (for local development fallback)

| Variable | Value | Description |
|----------|-------|-------------|
| `DB_URL` | `jdbc:mysql://...` | Direct JDBC URL (if not using Railway MySQL) |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `password` | Database password |

---

## Step 5: Import Database Schema

### Option A: Using Railway CLI

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Link to your project
railway link

# Connect to MySQL and import schema
railway connect mysql
# Then run: source schema.sql
```

### Option B: Using MySQL Workbench / CLI

1. Get MySQL connection details from Railway Dashboard
2. Connect using the provided host, port, user, password
3. Run the `database/schema.sql` file

```bash
mysql -h YOUR_RAILWAY_HOST -P YOUR_RAILWAY_PORT -u YOUR_RAILWAY_USER -p
# Enter password when prompted
# Then: source database/schema.sql
```

---

## Step 6: Deploy

1. Railway will automatically detect the `Dockerfile` and build your app
2. If build fails, check the **"Deploy"** logs
3. Once deployed, click on the generated domain to access your app

---

## Troubleshooting

### Build Failures

**Issue:** `Dockerfile not found`
- **Fix:** Make sure `Dockerfile` is in the `backend/` directory
- Railway should detect it automatically

**Issue:** `Could not resolve dependencies`
- **Fix:** Check your internet connection and try redeploying

**Issue:** `Permission denied` for gradlew
- **Fix:** The Dockerfile handles this, but locally run: `chmod +x gradlew`

### Database Connection Failures

**Issue:** `Failed to initialize database connection pool`
- **Fix:** Check that MySQL service is provisioned
- **Fix:** Verify environment variables are set correctly
- **Fix:** Check Railway logs for connection details

**Issue:** `Access denied for user`
- **Fix:** Railway MySQL variables should be auto-populated
- **Fix:** Check that `MYSQLUSER` and `MYSQLPASSWORD` are set

### Application Startup Failures

**Issue:** `Port already in use`
- **Fix:** Railway sets `PORT` env variable automatically
- **Fix:** Application uses `PORT` env var (handled in code)

**Issue:** `Health check failed`
- **Fix:** Check `/health` endpoint is accessible
- **Fix:** Verify application starts within timeout (60s)

---

## Deployment Files Reference

### Files for Railway Deployment

| File | Purpose |
|------|---------|
| `backend/Dockerfile` | Multi-stage Docker build |
| `backend/railway.json` | Railway deployment configuration |
| `backend/nixpacks.toml` | Alternative build configuration |
| `backend/system.properties` | Java version specification |
| `backend/.dockerignore` | Files to exclude from Docker build |

---

## Health Check Endpoint

The application exposes a health check endpoint at:
```
GET /health
```

Response:
```json
{
  "status": "UP",
  "version": "1.0.0",
  "timestamp": 1234567890
}
```

---

## Post-Deployment Steps

1. **Access your app** using the Railway-provided domain
2. **Login with admin credentials:**
   - Username: `admin1`
   - Password: `Admin@123`
3. **Verify database** - Check that pre-loaded data exists
4. **Test vendor signup** - Register a test vendor account

---

## Updating the Deployment

To update your deployed application:

```bash
# Make changes to code
git add .
git commit -m "Update description"
git push origin main
```

Railway will automatically detect the push and redeploy.

---

## Custom Domain (Optional)

1. In Railway Dashboard, go to your service
2. Click **"Settings"** → **"Domains"**
3. Click **"Custom Domain"**
4. Follow the DNS configuration instructions

---

## Monitoring & Logs

- **Logs:** Railway Dashboard → Your Service → "Deploy" tab → "View Logs"
- **Metrics:** Railway Dashboard → Your Service → "Metrics" tab
- **Health:** Check `/health` endpoint periodically

---

## Support

If you encounter issues:
1. Check Railway documentation: https://docs.railway.app/
2. Check application logs in Railway Dashboard
3. Verify all environment variables are set correctly
4. Ensure database schema is imported
