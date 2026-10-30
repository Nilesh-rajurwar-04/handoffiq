# HandoffIQ - AWS Docker Production Deployment Runbook
## Architecture: Amazon EC2 (Docker Host) + Amazon RDS MySQL + Amazon ECR

This production runbook details how to build, containerize, and deploy **HandoffIQ** on **Amazon Web Services (AWS)** using Docker, Docker Compose, Amazon Elastic Container Registry (ECR), and Amazon Relational Database Service (RDS).

---

## 1. Project Architecture

HandoffIQ uses an isolated, two-container architecture orchestrated via Docker Compose:

```
                                    +-------------------------------------------------------------+
                                    |                         AWS VPC                             |
                                    |                                                             |
                                    |  +-------------------------------------------------------+  |
[ Web Browser / Client ]            |  |             Amazon EC2 (Docker Host)                  |  |
           |                        |  |                                                       |  |
           | HTTP :80               |  |  +-------------------------------------------------+  |  |
           +------------------------+---->|               frontend Container                |  |  |
                                    |  |  |  - Nginx 1.27 Web Server                        |  |  |
                                    |  |  |  - Serves React + Vite Static SPA (/*)          |  |  |
                                    |  |  |  - Reverse Proxies /api/ to backend:8085        |  |  |
                                    |  |  +-------------------------------------------------+  |  |
                                    |  |                           |                           |  |
                                    |  |                           | Docker Bridge Network     |  |
                                    |  |                           | (handoffiq-network)       |  |
                                    |  |                           v                           |  |
                                    |  |  +-------------------------------------------------+  |  |
                                    |  |  |               backend Container                 |  |  |
                                    |  |  |  - Java 21 (Eclipse Temurin JRE)                |  |  |
                                    |  |  |  - Spring Boot 3.2.5 REST API (:8085)           |  |  |
                                    |  |  |  - Runs Flyway Migrations on Boot               |  |  |
                                    |  |  +-------------------------------------------------+  |  |
                                    |  +---------------------------|---------------------------+  |
                                    |                              |                              |
                                    |                              | Private VPC Subnet           |
                                    |                              | Port 3306 (MySQL)            |
                                    |                              v                              |
                                    |  +-------------------------------------------------------+  |
                                    |  |               Amazon RDS MySQL 8.0                    |  |
                                    |  |  - 24 Normalized Tables                               |  |
                                    |  |  - Auto-Seeded with Projects, Tasks & Handovers       |  |
                                    |  +-------------------------------------------------------+  |
                                    +-------------------------------------------------------------+
```

### Key Architectural Decisions:
1. **Frontend Container (Port 80 Public)**:
   - Built with Node 20 and served using high-performance Nginx.
   - Handles React Router SPA routing (`try_files $uri $uri/ /index.html`).
   - Reverse proxies all `/api/` traffic directly to `http://backend:8085/api/` over the internal Docker network.
   - **Zero CORS Configuration Required**: Browsers only communicate with Port 80 on the EC2 host.
2. **Backend Container (Internal Network Only)**:
   - Port `8085` is exposed **only inside** the private Docker network (`handoffiq-network`).
   - Not accessible directly from the public internet, preventing unauthorized API exposure.
   - Executes 24 Flyway database migrations upon first boot against Amazon RDS MySQL.
3. **Amazon RDS MySQL (Managed Database)**:
   - Hosted in private database subnets.
   - Accessible only from the EC2 Security Group on MySQL port 3306.

---

## 2. Docker Installation Prerequisites

### For Windows Local Development:
1. Install **Docker Desktop for Windows** with WSL 2 backend enabled.
2. Verify Docker and Compose installations in PowerShell:
   ```powershell
   docker --version
   docker-compose --version
   ```

### For AWS EC2 (Amazon Linux 2023):
```bash
# Update system packages
sudo dnf update -y

# Install Docker
sudo dnf install -y docker

# Enable and start Docker service
sudo systemctl enable --now docker

# Add ec2-user to docker group to run without sudo
sudo usermod -aG docker ec2-user

# Install Docker Compose Plugin
sudo dnf install -y docker-compose-plugin

# Log out and log back in for group membership to take effect
newgrp docker

# Verify installation
docker --version
docker compose version
```

### For AWS EC2 (Ubuntu 24.04 / 22.04 LTS):
```bash
# Update apt repository
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

# Add Docker GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine and Compose
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Enable Docker and add user
sudo systemctl enable --now docker
sudo usermod -aG docker ubuntu
newgrp docker
```

---

## 3. How to Build Frontend and Backend Images

### Building the Frontend Image:
```bash
# Navigate to the project root
cd d:\projects\campus\handoffiq   # Windows
# or cd /home/ec2-user/handoffiq  # Linux

# Build frontend container image
docker build -t handoffiq-frontend:latest ./frontend
```
*The multi-stage build uses `node:20-alpine` to install dependencies and run `npm run build`, then copies the production assets into `nginx:1.27-alpine`.*

### Building the Backend Image:
```bash
# Build backend container image
docker build -t handoffiq-backend:latest ./backend
```
*The multi-stage build uses `maven:3.9.6-eclipse-temurin-21-alpine` to package the executable JAR, then packages it into a hardened, non-root `eclipse-temurin:21-jre-alpine` runtime container.*

---

## 4. How to Run the Application Using Docker Compose

### 1. Create your Environment Configuration (`.env`):
```bash
# Copy the template
cp .env.example .env
```

### 2. Start all Containers in Background Mode:
```bash
docker-compose up -d --build
```

### 3. Verify Container Status:
```bash
docker-compose ps
```
**Expected Output:**
```
NAME                 IMAGE                      COMMAND                  SERVICE    STATUS              PORTS
handoffiq-backend    handoffiq-backend:latest   "java -XX:+UseContai…"   backend    running (healthy)   8085/tcp
handoffiq-frontend   handoffiq-frontend:latest  "nginx -g 'daemon of…"   frontend   running (healthy)   0.0.0.0:80->80/tcp
```

---

## 5. How to Configure Environment Variables

The project uses `.env` in the root directory. Configure the following variables:

| Environment Variable | Example Value | Description |
| :--- | :--- | :--- |
| `FRONTEND_PORT` | `80` | Public HTTP port exposed on the host |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<RDS_HOST>:3306/handoffiq?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true` | RDS MySQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `admin` | RDS Master Username |
| `SPRING_DATASOURCE_PASSWORD` | `YourSecurePassword123!` | RDS Master Password |
| `SPRING_FLYWAY_ENABLED` | `true` | Automatically creates tables and seeds demo data |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | 64-char Hex JWT signing secret |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiration in ms (24 hours) |
| `AI_PROVIDER` | `gemini` | `gemini` \| `openai` \| `fallback` |
| `GEMINI_API_KEY` | `AIzaSy...` | Optional: Gemini API key |
| `STORAGE_TYPE` | `local` | `local` or `s3` |
| `AWS_S3_BUCKET` | `handoffiq-documents` | Optional S3 bucket name |
| `AWS_REGION` | `us-east-1` | AWS region |

---

## 6. How to Connect to AWS RDS MySQL

### Step 1: Create the Amazon RDS MySQL Database
1. Open the **AWS RDS Console** $\rightarrow$ Click **Create database**.
2. Select **Standard create** $\rightarrow$ **MySQL** (Version 8.0.x).
3. Under **Templates**, select **Free tier** (or **Production**).
4. Set **DB instance identifier**: `handoffiq-db`.
5. Set **Master username**: `admin`.
6. Set **Master password**: e.g., `HandoffIqSecurePass2026!`.
7. Under **Connectivity**:
   - **VPC**: Select the same VPC as your EC2 instance.
   - **Public access**: **No** (keeps database isolated from internet).
   - **VPC security group**: Create new or select an existing security group (e.g. `handoffiq-rds-sg`).
8. Under **Initial database name**: Enter `handoffiq`.
9. Click **Create database** (takes 5-10 minutes to become available).

### Step 2: Configure RDS Security Group
1. Open the Security Group attached to your RDS instance (`handoffiq-rds-sg`).
2. Add an **Inbound Rule**:
   - **Type**: `MYSQL/Aurora` (Port 3306)
   - **Source**: Select **Custom** $\rightarrow$ enter the Security Group ID of your EC2 instance (e.g., `sg-0123456789abcdef0`).
   - *Never allow 0.0.0.0/0 to your database!*

### Step 3: Configure `.env` on EC2
Once RDS status is **Available**, copy the **Endpoint** from the RDS dashboard:
```env
SPRING_DATASOURCE_URL=jdbc:mysql://handoffiq-db.cxxxxxxxx.us-east-1.rds.amazonaws.com:3306/handoffiq?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=HandoffIqSecurePass2026!
SPRING_FLYWAY_ENABLED=true
```

---

## 7. How to Push Docker Images to Amazon ECR

### Step 1: Create ECR Repositories (AWS CLI)
```bash
aws ecr create-repository --repository-name handoffiq-backend --region us-east-1
aws ecr create-repository --repository-name handoffiq-frontend --region us-east-1
```

### Step 2: Authenticate Docker to Amazon ECR
```bash
# Windows PowerShell
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Linux / Mac
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

### Step 3: Tag and Push Images
```bash
# Tag Backend
docker tag handoffiq-backend:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/handoffiq-backend:latest

# Push Backend
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/handoffiq-backend:latest

# Tag Frontend
docker tag handoffiq-frontend:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/handoffiq-frontend:latest

# Push Frontend
docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/handoffiq-frontend:latest
```

---

## 8. How to Deploy the Application on EC2

### Step 1: Launch EC2 Instance
- **AMI**: Amazon Linux 2023 or Ubuntu 24.04 LTS.
- **Instance Type**: `t3.small` (2 vCPU, 2GB RAM) or `t3.medium`.
- **Security Group Inbound Rules**:
  - `SSH` (Port 22) $\rightarrow$ Your IP
  - `HTTP` (Port 80) $\rightarrow$ `0.0.0.0/0`
  - `HTTPS` (Port 443) $\rightarrow$ `0.0.0.0/0`
- **IAM Role**: Attach an IAM role with `AmazonEC2ContainerRegistryReadOnly` policy so EC2 can pull from ECR without storing AWS access keys.

### Step 2: Connect and Clone Project on EC2
```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>

# Clone repository
git clone https://github.com/Nilesh-rajurwar-04/handoffiq.git
cd handoffiq

# Create production .env file
cp .env.example .env
nano .env  # Edit RDS credentials and JWT secret
```

### Step 3: Launch Containers
```bash
docker compose up -d --build
```

---

## 9. How to View Container Logs

```bash
# View combined live tail of all containers
docker-compose logs -f

# View Backend logs only (Spring Boot & Flyway migrations)
docker-compose logs -f backend

# View Frontend logs only (Nginx access and error logs)
docker-compose logs -f frontend

# View last 100 lines of backend logs
docker-compose logs --tail=100 backend
```

---

## 10. How to Restart and Stop Containers

```bash
# Stop containers gracefully
docker-compose stop

# Start stopped containers
docker-compose start

# Restart containers (e.g. after editing .env)
docker-compose restart

# Stop and remove containers and networks
docker-compose down

# Rebuild and restart containers in background
docker-compose up -d --build --force-recreate
```

### Ensuring Automatic Startup on EC2 Boot:
Because both containers specify `restart: unless-stopped`, they will **automatically start whenever the EC2 instance reboots or recovers**, provided the Docker daemon is enabled via `sudo systemctl enable docker`.

---

## 11. How to Troubleshoot Common Deployment Errors

### Issue 1: Backend Fails to Connect to RDS (`CommunicationsException`)
- **Symptom**: `Communications link failure. The last packet sent successfully to the server was 0 milliseconds ago.`
- **Diagnosis**: The EC2 instance cannot reach the RDS database on port 3306.
- **Fix**:
  1. Verify EC2 Security Group is listed in RDS Inbound Rules on Port 3306.
  2. Test connection from EC2 container host:
     ```bash
     nc -zv <RDS_ENDPOINT> 3306
     ```
  3. Ensure both EC2 and RDS reside in the same AWS VPC.

### Issue 2: Frontend Returns `502 Bad Gateway`
- **Symptom**: Opening `http://<EC2_IP>` in browser returns 502 Bad Gateway.
- **Diagnosis**: Spring Boot backend has not finished booting or failed its healthcheck.
- **Fix**:
  1. Check backend status:
     ```bash
     docker-compose ps
     ```
  2. Inspect backend error logs:
     ```bash
     docker-compose logs backend
     ```
  3. Spring Boot takes ~25–35 seconds on `t3.small` to compile JPA and run Flyway. Wait until `docker-compose ps` shows `running (healthy)`.

### Issue 3: EC2 Runs Out of Memory (OOM Kill)
- **Symptom**: Container abruptly exits with code 137.
- **Fix**: Create a 2GB swap file on your EC2 instance:
  ```bash
  sudo dd if=/dev/zero of=/swapfile bs=128M count=16
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
  ```

---

## 12. How to Update the Application After Pushing New Code to GitHub

When new changes are pushed to GitHub, update your live EC2 deployment with zero downtime using this procedure:

```bash
# 1. SSH into EC2
ssh -i your-key.pem ec2-user@<EC2_PUBLIC_IP>
cd ~/handoffiq

# 2. Pull latest code from GitHub
git pull origin main

# 3. Rebuild and launch containers with zero downtime
docker compose up -d --build

# 4. Verify healthy status
docker compose ps
docker compose logs --tail=50 backend
```
*(Docker Compose will build new container images, perform health checks, and swap containers smoothly!)*

---

## Default Seeded Credentials for Testing

Once deployed, access `http://<EC2_PUBLIC_IP>` and log in with any seeded account:

| Role | Username / Email | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Admin** | `admin` | `password123` | Full enterprise control, system metrics, employee management |
| **Manager** | `manager` | `password123` | Create meetings, delegate tasks, initiate employee handovers |
| **Employee** | `employee` | `password123` | View assigned tasks, generate catch-up briefs, accept handovers |
| **Personal Account** | `nilesh` | `password123` | Registered engineer profile |
