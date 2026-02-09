# GTPL_UG Management System

## Enterprise-Grade Underground Infrastructure Management Platform

[![Java 17](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![Javalin](https://img.shields.io/badge/Javalin-5.6.3-green.svg)](https://javalin.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Enterprise-red.svg)]()

---

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Technology Stack](#technology-stack)
4. [Architecture](#architecture)
5. [Installation](#installation)
6. [Configuration](#configuration)
7. [User Roles](#user-roles)
8. [API Documentation](#api-documentation)
9. [Database Schema](#database-schema)
10. [Security](#security)
11. [Development](#development)
12. [Deployment](#deployment)
13. [Support](#support)

---

## Overview

GTPL_UG Management System is a comprehensive enterprise-grade web application designed for underground (UG) infrastructure companies to manage vendors, assign work, track kilometer-wise progress, monitor material usage, enforce daily reporting, and visualize performance through dashboards and analytics.

### Key Capabilities

- **Project Management**: Create, assign, and track UG projects with detailed work specifications
- **Vendor Management**: Complete vendor lifecycle from registration to performance evaluation
- **KM Tracking**: Real-time progress tracking with cumulative and daily KM completion
- **Material Management**: Inventory tracking with request and approval workflows
- **Daily Reporting**: Enforced daily progress submission with compliance monitoring
- **Analytics & Charts**: Visual dashboards with Chart.js integration
- **Notification System**: In-app alerts for deadlines, compliance, and status updates
- **Mobile Responsive**: Touch-friendly UI optimized for field use

---

## Features

### Admin Features

- ✅ Dashboard with project and vendor statistics
- ✅ Create and assign projects to vendors
- ✅ Manage vendor accounts (activate/deactivate)
- ✅ Material catalog management
- ✅ Approve/reject material requests
- ✅ Monitor daily progress and compliance
- ✅ Generate reports and analytics
- ✅ View vendor performance metrics

### Vendor Features

- ✅ Sign up and account management
- ✅ View assigned projects
- ✅ Submit daily progress reports
- ✅ Request materials
- ✅ Track project progress
- ✅ Receive notifications
- ✅ Password reset functionality

---

## Technology Stack

### Backend

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17 |
| Framework | Javalin | 5.6.3 |
| Template Engine | Thymeleaf | 3.1.2 |
| Database | MySQL | 8.0+ |
| Connection Pool | HikariCP | 5.1.0 |
| Password Hashing | BCrypt | 0.4 |
| JSON Processing | Jackson | 2.16.1 |
| Build Tool | Gradle | 8.5 |

### Frontend

| Component | Technology |
|-----------|------------|
| HTML | HTML5 |
| CSS | CSS3 with Custom Properties |
| JavaScript | ES6+ |
| Charts | Chart.js |
| Icons | Font Awesome 6 |
| Fonts | Google Fonts (Inter) |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Thymeleaf  │  │   Chart.js  │  │  Responsive CSS/JS  │  │
│  │  Templates  │  │   Charts    │  │   Mobile Widgets    │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                      CONTROLLER LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Auth      │  │    Admin    │  │       Vendor        │  │
│  │ Controller  │  │ Controller  │  │    Controller       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐                            │
│  │  Dashboard  │  │    API      │                            │
│  │ Controller  │  │ Controller  │                            │
│  └─────────────┘  └─────────────┘                            │
├─────────────────────────────────────────────────────────────┤
│                       SERVICE LAYER                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │    Auth     │  │    Admin    │  │       Vendor        │  │
│  │  Service    │  │  Service    │  │     Service         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              Notification Service                    │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                         DAO LAYER                            │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌────────┐ │
│  │  Admin  │ │ Vendor  │ │ Project │ │  Daily  │ │Material│ │
│  │  DAO    │ │  DAO    │ │  DAO    │ │Progress │ │  DAO   │ │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐                    │
│  │ MaterialRequest │  │  Notification   │                    │
│  │      DAO        │  │      DAO        │                    │
│  └─────────────────┘  └─────────────────┘                    │
├─────────────────────────────────────────────────────────────┤
│                      DATABASE LAYER                          │
│                    MySQL 8.0+                                │
└─────────────────────────────────────────────────────────────┘
```

---

## Installation

### Prerequisites

- Java 17 JDK
- MySQL 8.0+
- Gradle 8.5+

### Step 1: Clone the Repository

```bash
git clone https://github.com/gtpl/gtpl-ug-system.git
cd gtpl-ug-system
```

### Step 2: Database Setup

```bash
# Login to MySQL
mysql -u root -p

# Create database and import schema
source database/schema.sql
```

### Step 3: Configure Database Connection

Edit `src/main/java/com/gtpl/utils/DatabaseConfig.java` or set environment variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/gtpl_ug_system?useSSL=false&serverTimezone=UTC"
export DB_USER="root"
export DB_PASSWORD="your_password"
```

### Step 4: Build the Application

```bash
cd backend
./gradlew shadowJar
```

### Step 5: Run the Application

```bash
java -jar build/libs/gtpl-ug-system.jar
```

The application will be available at: http://localhost:8080

---

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | MySQL connection URL | `jdbc:mysql://localhost:3306/gtpl_ug_system` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `PORT` | Application port | `8080` |
| `APP_ENV` | Environment (development/production) | `production` |

### System Configuration (Database)

Configuration stored in `system_config` table:

| Key | Description | Default |
|-----|-------------|---------|
| `SESSION_TIMEOUT_MINUTES` | User session timeout | 120 |
| `PASSWORD_RESET_EXPIRY_HOURS` | Reset token validity | 24 |
| `DAILY_UPDATE_CUTOFF_TIME` | Daily report deadline | 18:00 |
| `MAX_LOGIN_ATTEMPTS` | Failed login threshold | 5 |

---

## User Roles

### Admin Credentials (Pre-configured)

| Username | Password | Role |
|----------|----------|------|
| admin1 | Admin@123 | Administrator |
| admin2 | Admin@123 | Administrator |
| admin3 | Admin@123 | Administrator |
| admin4 | Admin@123 | Administrator |

**Note**: Passwords are BCrypt hashed in the database.

### Vendor Access

- Vendors must sign up through the registration page
- Accounts require admin verification for full access
- Vendors can only access their assigned projects
- Password reset available via email token

---

## API Documentation

### Dashboard Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/dashboard/admin` | GET | Admin dashboard data | Admin |
| `/api/dashboard/vendor` | GET | Vendor dashboard data | Vendor |

### Chart Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/charts/project-progress` | GET | Project progress chart data | Any |
| `/api/charts/vendor-performance` | GET | Vendor performance data | Admin |
| `/api/charts/material-usage` | GET | Material usage statistics | Any |
| `/api/charts/daily-timeline` | GET | Daily progress timeline | Any |

### Widget Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/widgets/vendor-stats` | GET | Vendor statistics | Admin |
| `/api/widgets/project-summary` | GET | Project summary | Any |
| `/api/widgets/notifications` | GET | Notification count | Any |
| `/api/widgets/compliance-status` | GET | Compliance status | Admin |

---

## Database Schema

### Core Tables

- **admins** - System administrators (4 hardcoded)
- **vendors** - External contractors
- **projects** - UG work projects
- **work_assignments** - Project-vendor assignments
- **daily_progress** - Daily work reports (immutable)
- **materials** - Material catalog
- **project_materials** - Materials assigned to projects
- **material_requests** - Vendor material requests
- **notifications** - In-app notifications
- **audit_logs** - System audit trail
- **vendor_performance** - Aggregated performance metrics

### Entity Relationship Diagram

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    admins    │────<│   projects   │>────│   vendors    │
└──────────────┘     └──────────────┘     └──────────────┘
                            │
                            v
                     ┌──────────────┐
                     │work_assignments
                     └──────────────┘
                            │
           ┌────────────────┼────────────────┐
           v                v                v
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
    │daily_progress│ │project_materials│ │material_requests│
    └──────────────┘ └──────────────┘ └──────────────┘
                            │                │
                            v                v
                     ┌──────────────┐ ┌──────────────┐
                     │   materials  │ │ notifications│
                     └──────────────┘ └──────────────┘
```

---

## Security

### Implemented Security Measures

- ✅ BCrypt password hashing (12 rounds)
- ✅ Session-based authentication
- ✅ Role-based access control
- ✅ SQL injection prevention (parameterized queries)
- ✅ XSS protection (Thymeleaf auto-escaping)
- ✅ CSRF protection
- ✅ Session timeout handling
- ✅ Account lockout after failed attempts
- ✅ Secure password reset tokens

### Password Requirements

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (!@#$%^&*)

---

## Development

### Project Structure

```
gtpl_ug_system/
├── backend/
│   ├── src/main/java/com/gtpl/
│   │   ├── Application.java
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── daos/
│   │   ├── models/
│   │   ├── utils/
│   │   └── middleware/
│   ├── src/main/resources/
│   │   ├── templates/
│   │   └── static/
│   └── build.gradle
├── database/
│   └── schema.sql
└── README.md
```

### Running in Development Mode

```bash
# Set development environment
export APP_ENV=development

# Run with Gradle
./gradlew run
```

### Running Tests

```bash
./gradlew test
```

---

## Deployment

### Production Deployment Checklist

- [ ] Update database configuration
- [ ] Set strong admin passwords
- [ ] Configure SSL/TLS
- [ ] Set up reverse proxy (Nginx/Apache)
- [ ] Configure firewall rules
- [ ] Set up log rotation
- [ ] Configure backup strategy
- [ ] Set up monitoring

### Docker Deployment (Optional)

```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/gtpl-ug-system.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## Support

### Contact Information

- **Company**: GTPL (Underground Infrastructure Division)
- **Support Email**: support@gtpl.com
- **Technical Documentation**: https://docs.gtpl.com

### Troubleshooting

| Issue | Solution |
|-------|----------|
| Database connection failed | Check DB_URL, DB_USER, DB_PASSWORD |
| Port already in use | Change PORT environment variable |
| Session timeout too fast | Adjust SESSION_TIMEOUT_MINUTES |
| Password reset not working | Check reset token expiry time |

---

## License

Copyright © 2024 GTPL. All rights reserved.

This software is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited.

---

## Changelog

### Version 1.0.0 (2024-01-01)

- Initial release
- Project management
- Vendor management
- Daily reporting
- Material management
- Dashboard and analytics
- Notification system
- Mobile responsive design

---

**Built with ❤️ by the GTPL Development Team**
