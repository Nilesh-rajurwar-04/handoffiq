# HandoffIQ - Production Deployment Guide for Render (render.com)

This guide provides step-by-step instructions for deploying **HandoffIQ** (Java 21 Spring Boot + React + Vite + MySQL 8) onto [Render](https://render.com).

---

## Architecture Overview

You have two deployment strategies for Render:

| Strategy | Description | Best For | Render Free Tier Usage |
| :--- | :--- | :--- | :--- |
| **Option 1: Unified Container (Recommended)** | Builds React frontend and Spring Boot into **one single Docker web service**. Spring Boot serves both the API (`/api/*`) and React SPA UI (`/*`). | Easiest setup, zero CORS headaches, fastest to deploy. | **1 Web Service** (100% Free) |
| **Option 2: Split Micro-Services** | Backend deployed as a **Docker Web Service**, Frontend deployed as a **Static Site**. | Distributed scaling, independent frontend and backend updates. | **1 Web Service + 1 Static Site** |

Both strategies use automated Flyway migrations (`SPRING_FLYWAY_ENABLED=true`) to create all 24 database tables and seed realistic sample data on first boot.

---

## Prerequisites

1. A free account on [Render.com](https://render.com).
2. A free account on a cloud MySQL provider (e.g. [Aiven](https://aiven.io), [TiDB Cloud](https://tidbcloud.com), or [Railway](https://railway.app)).
3. A GitHub or GitLab repository containing the HandoffIQ code.
4. (Optional) A Google Gemini API key or OpenAI API key for live AI features.

---

## Step 1: Set Up a Free Cloud MySQL Database

Render provides managed PostgreSQL natively, but HandoffIQ is built for MySQL 8. The easiest and most reliable solution is to spin up a free cloud MySQL instance:

### Option A: Aiven for MySQL (Recommended - Free Tier)
1. Sign up at [aiven.io](https://aiven.io).
2. Click **Create Service** -> select **MySQL** -> choose **Free Tier**.
3. Choose any cloud region (e.g., `us-east-1` or `eu-west-1`).
4. Once the service is running, find the **Connection Details**:
   - **Host**: `mysql-xxxxx.aivencloud.com`
   - **Port**: `12345`
   - **User**: `avnadmin`
   - **Password**: `AVNS_...`
   - **Database**: `defaultdb` (or create `handoffiq`)
5. Your JDBC URL format will be:
   ```
   jdbc:mysql://<HOST>:<PORT>/<DATABASE>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```

### Option B: TiDB Cloud Serverless (Free Forever)
1. Sign up at [tidbcloud.com](https://tidbcloud.com).
2. Create a free **Serverless Cluster** (fully MySQL 8 wire-compatible).
3. Click **Connect** -> choose **Java (JDBC)** and copy the JDBC URL, username, and password.

---

## Step 2: Push Code to GitHub

Initialize your Git repository and push your project to GitHub:

```bash
cd d:\projects\campus\handoffiq
git init
git add .
git commit -m "feat: complete HandoffIQ full-stack platform with Render configuration"
git branch -M main
git remote add origin https://github.com/<your-username>/handoffiq.git
git push -u origin main
```

---

## Step 3: Deploy to Render

### Method A: 1-Click Blueprint (Recommended)

HandoffIQ includes a preconfigured [`render.yaml`](file:///d:/projects/campus/handoffiq/render.yaml) blueprint file at the repository root.

1. Log in to [dashboard.render.com](https://dashboard.render.com).
2. Click the **New +** button in the top navigation bar.
3. Select **Blueprint**.
4. Connect your GitHub repository: `https://github.com/<your-username>/handoffiq`.
5. Render will detect the `render.yaml` file automatically and prompt you for the required environment variables:
   - `SPRING_DATASOURCE_URL`: Your cloud MySQL JDBC URL (from Step 1)
   - `SPRING_DATASOURCE_USERNAME`: Your MySQL username
   - `SPRING_DATASOURCE_PASSWORD`: Your MySQL password
   - `GEMINI_API_KEY`: *(Optional)* Your Gemini API key
6. Click **Apply**.
7. Render will automatically:
   - Build the React frontend with Node 20.
   - Build the Spring Boot executable JAR with Maven & Java 21.
   - Run the lightweight container.
   - Execute Flyway migrations to create all 24 MySQL tables and demo records.

---

### Method B: Manual Web Service Setup (Alternative)

If you prefer to configure the Web Service manually via the Render UI:

1. In the Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. Configure the service settings:
   - **Name**: `handoffiq-app`
   - **Region**: Oregon (or nearest to your database)
   - **Branch**: `main`
   - **Root Directory**: Leave blank (root directory)
   - **Runtime**: **Docker**
   - **Dockerfile Path**: `Dockerfile`
   - **Instance Type**: **Free** (512 MB RAM, 0.1 CPU)
4. Add the following **Environment Variables** under the *Environment* tab:

| Environment Variable | Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8085` | Internal port (Render maps public HTTPS to this) |
| `SPRING_FLYWAY_ENABLED` | `true` | Runs initial schema and seed migrations automatically |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<host>:<port>/<db>?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` | Cloud MySQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `<db_username>` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `<db_password>` | Database password |
| `AI_PROVIDER` | `gemini` | Configurable AI engine (`gemini`, `openai`, or `fallback`) |
| `GEMINI_API_KEY` | *(Your API Key)* | Optional (falls back to rule-based analysis if blank) |
| `JWT_SECRET` | *(64-char random string or generate)* | Secret key for signing bearer tokens |

5. Under **Health Check Path**, enter:
   ```
   /api/dashboard/stats
   ```
6. Click **Create Web Service**.

---

## Step 4: Verify Your Live Deployment

Once the build finishes and the service status displays **Live**:

1. Click on your Render service URL (e.g. `https://handoffiq-app.onrender.com`).
2. The HandoffIQ login screen will appear with instant demo access.
3. Sign in using any of the seeded credentials:

| Role | Username / Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` *(or `admin@handoffiq.com`)* | `password123` | Full enterprise control, system metrics, employee management |
| **Manager** | `manager` *(or `sarah.jenkins@handoffiq.com`)* | `password123` | Initiate handovers, schedule meetings, delegate tasks |
| **Employee** | `employee` *(or `rahul.sharma@handoffiq.com`)* | `password123` | View assigned tasks, generate catch-up briefs, acknowledge handovers |
| **Your Custom User** | `nilesh` *(or `nileshrajurwar9403@gmail.com`)* | `password123` | Registered employee account |

---

## Step 5: Render Free Tier Best Practices & Tips

1. **Cold Starts**: Render's free tier spins down web services after 15 minutes of inactivity. When the first request comes in, it takes approximately 45–60 seconds to spin back up. Subsequent requests are instant.
2. **Memory Optimization**: The included `Dockerfile` includes JVM container memory flags (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`) to keep total memory under 280MB, well below Render's 512MB free tier limit.
3. **Automated Continuous Deployment**: Any future commits pushed to your GitHub `main` branch will automatically trigger a new zero-downtime deployment on Render.
4. **Live Logs**: You can inspect real-time logs, Flyway migration progress, and structured application events directly in the **Logs** tab of your Render service.
