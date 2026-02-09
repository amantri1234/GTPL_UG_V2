# Railway Deployment - Final Fixed Version

## Problem Fixed
Railpack couldn't detect the build because the Java project was in a subdirectory (`backend/`). 

## Solution
Created root-level configuration files that tell Railway how to build the project.

---

## Files Added at Root Level

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage Docker build that builds from `backend/` |
| `railway.toml` | Railway deployment configuration |
| `system.properties` | Java 17 version specification |
| `nixpacks.toml` | Alternative Nixpacks build configuration |
| `.dockerignore` | Files to exclude from Docker context |

---

## Deployment Steps

### 1. Push Code to GitHub

```bash
cd /mnt/okcomputer/output/gtpl_ug_system

git init
git add .
git commit -m "Initial commit with Railway deployment config"

git remote add origin https://github.com/YOUR_USERNAME/gtpl-ug-system.git
git branch -M main
git push -u origin main
```

### 2. Create Railway Project

1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your `gtpl-ug-system` repository
5. Click **"Deploy"**

### 3. Add MySQL Database

1. In your Railway project, click **"New"**
2. Select **"Database"** → **"Add MySQL"**
3. Wait for MySQL to provision (takes ~1 minute)

### 4. Import Database Schema

**Option A: Using Railway CLI**
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login and link
railway login
railway link

# Connect to MySQL and run schema
railway connect mysql
# Then paste the contents of database/schema.sql
```

**Option B: Using MySQL Client**
```bash
# Get connection details from Railway Dashboard
# Variables: MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD

mysql -h $MYSQLHOST -P $MYSQLPORT -u $MYSQLUSER -p$MYSQLPASSWORD $MYSQLDATABASE < database/schema.sql
```

### 5. Redeploy (if needed)

If the app deployed before the database was ready:
1. Go to your service in Railway Dashboard
2. Click **"Redeploy"**

---

## Environment Variables (Auto-Set)

Railway automatically sets these when you add MySQL:
- `DATABASE_URL` - Full JDBC connection string
- `MYSQLHOST` - Database hostname
- `MYSQLPORT` - Database port
- `MYSQLDATABASE` - Database name
- `MYSQLUSER` - Database username
- `MYSQLPASSWORD` - Database password
- `PORT` - Dynamic port for the application

---

## Verify Deployment

1. **Check build logs:** Railway Dashboard → Your Service → "Deploy" tab
2. **Check health endpoint:** Visit `https://your-app.railway.app/health`
   - Should return: `{"status":"UP","version":"1.0.0","timestamp":...}`
3. **Access app:** Visit `https://your-app.railway.app/`
4. **Login:** Use admin credentials (admin1 / Admin@123)

---

## Troubleshooting

### "Railpack could not determine how to build"
**Fix:** Root-level `Dockerfile` and `railway.toml` are now present. Railway should auto-detect.

### "Failed to connect to database"
**Fix:** 
1. Ensure MySQL service is added
2. Check `DatabaseConfig.java` handles Railway env vars
3. Verify schema is imported

### "Health check failed"
**Fix:** 
1. Check `/health` endpoint is accessible
2. Verify app starts within timeout (100s)
3. Check logs for startup errors

### "Port already in use"
**Fix:** Application uses `PORT` env var (already configured)

---

## Project Structure for Railway

```
gtpl_ug_system/           # ← Railway analyzes this directory
├── Dockerfile            # ← Root-level Docker config
├── railway.toml          # ← Railway deployment config
├── system.properties     # ← Java version
├── nixpacks.toml         # ← Alternative build config
├── .dockerignore         # ← Docker ignore rules
├── backend/              # ← Java application code
│   ├── build.gradle
│   ├── src/
│   └── ...
├── database/
│   └── schema.sql        # ← Database schema
└── README.md
```

---

## Build Process

1. Railway detects `Dockerfile` at root
2. Docker builds the app:
   - Stage 1: Uses Gradle to build JAR from `backend/`
   - Stage 2: Creates runtime image with JRE 17
3. Railway deploys the image
4. Health check at `/health` verifies startup

---

## Success Checklist

- [ ] Code pushed to GitHub
- [ ] Railway project created from GitHub repo
- [ ] MySQL database added
- [ ] Database schema imported
- [ ] Build completes successfully
- [ ] Health check returns `{"status":"UP"}`
- [ ] Can access login page
- [ ] Can login with admin1/Admin@123

---

## Support

If issues persist:
1. Check Railway build logs
2. Verify all files are committed to GitHub
3. Ensure `Dockerfile` is at root level (not in `backend/`)
4. Check that MySQL service is fully provisioned before deploying
