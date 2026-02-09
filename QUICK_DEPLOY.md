# Quick Deploy to Railway

## One-Command Deploy

```bash
# 1. Push to GitHub
git add . && git commit -m "Railway deployment ready" && git push origin main

# 2. Go to Railway Dashboard
# https://railway.app/dashboard

# 3. New Project → Deploy from GitHub repo

# 4. Add MySQL Database
# New → Database → Add MySQL

# 5. Import Schema
railway connect mysql
source database/schema.sql

# 6. Done! Visit your Railway URL
```

---

## Admin Login

| Username | Password |
|----------|----------|
| admin1 | Admin@123 |
| admin2 | Admin@123 |
| admin3 | Admin@123 |
| admin4 | Admin@123 |

---

## Health Check

```
GET https://your-app.railway.app/health

Response:
{
  "status": "UP",
  "version": "1.0.0",
  "timestamp": 1234567890
}
```

---

## What's Fixed?

✅ Root-level `Dockerfile` - Railway can now detect the build  
✅ `railway.toml` - Deployment configuration  
✅ `system.properties` - Java 17 specification  
✅ Health check endpoint at `/health`  
✅ Railway database environment variable handling  

---

## Need Help?

See `RAILWAY_FINAL_DEPLOYMENT.md` for detailed instructions.
