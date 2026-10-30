#!/usr/bin/env bash
# ==============================================================================
# HandoffIQ - Automated AWS EC2 Provisioning & Deployment Script
# Supports: Ubuntu 22.04 / 24.04 LTS & Amazon Linux 2023
# Target: Single-node or Auto-Scaling EC2 with AWS RDS MySQL and S3
# ==============================================================================

set -euo pipefail

echo "================================================================="
echo "  Starting HandoffIQ Production Provisioning on AWS EC2          "
echo "================================================================="

# 1. Detect OS
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
else
    echo "Unsupported operating system"
    exit 1
fi

echo "[1/7] Updating system packages..."
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    sudo apt-get update -y
    sudo apt-get upgrade -y
    sudo apt-get install -y curl wget git unzip jq nginx certbot python3-certbot-nginx
elif [ "$OS" = "amzn" ] || [ "$OS" = "fedora" ] || [ "$OS" = "rhel" ]; then
    sudo dnf update -y
    sudo dnf install -y curl wget git unzip jq nginx certbot python3-certbot-nginx
fi

# 2. Install Java 21 (Amazon Corretto)
echo "[2/7] Installing Amazon Corretto Java 21..."
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    wget -O - https://apt.corretto.aws/corretto.key | sudo gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg
    echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | sudo tee /etc/apt/sources.list.d/corretto.list
    sudo apt-get update -y
    sudo apt-get install -y java-21-amazon-corretto-jdk
elif [ "$OS" = "amzn" ]; then
    sudo dnf install -y java-21-amazon-corretto-devel
fi

java -version

# 3. Create Service User and Directory Structure
echo "[3/7] Setting up dedicated handoffiq user and directory hierarchy..."
if ! id "handoffiq" &>/dev/null; then
    sudo useradd --system --no-create-home --shell /bin/false handoffiq
fi

sudo mkdir -p /opt/handoffiq/backend
sudo mkdir -p /opt/handoffiq/backend/logs
sudo mkdir -p /var/www/handoffiq/frontend/dist
sudo mkdir -p /etc/handoffiq
sudo mkdir -p /var/log/handoffiq

# 4. Configure Application Secrets Template
echo "[4/7] Generating secrets file at /etc/handoffiq/secrets.env..."
if [ ! -f /etc/handoffiq/secrets.env ]; then
    sudo tee /etc/handoffiq/secrets.env > /dev/null << 'EOF'
# ==============================================================================
# HandoffIQ Production Secrets (Read only by handoffiq user)
# ==============================================================================

# Database Credentials (AWS RDS MySQL)
DB_HOST=handoffiq-mysql.cxxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=3306
DB_NAME=handoffiq
DB_USERNAME=handoffiq_prod
DB_PASSWORD=REPLACE_WITH_RDS_MASTER_PASSWORD

# Security Secrets
JWT_SECRET=4f8b92c7d1e0a3f5b6c8d7e9a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0
JWT_EXPIRATION_MS=86400000

# AI Provider Configuration (Configure Gemini or OpenAI)
AI_PROVIDER=GEMINI
GEMINI_API_KEY=REPLACE_WITH_YOUR_GEMINI_API_KEY
OPENAI_API_KEY=
AI_MODEL_NAME=gemini-1.5-flash

# AWS S3 Storage for Documents (Optional, uses S3 if configured)
AWS_REGION=us-east-1
AWS_S3_BUCKET=handoffiq-knowledge-docs-prod
EOF
    sudo chmod 600 /etc/handoffiq/secrets.env
    sudo chown handoffiq:handoffiq /etc/handoffiq/secrets.env
    echo "Generated template /etc/handoffiq/secrets.env. Please edit with real credentials."
fi

# 5. Copy Systemd Service & Nginx Config
echo "[5/7] Installing Systemd Service and Nginx Configuration..."
if [ -f "./deployment/handoffiq.service" ]; then
    sudo cp ./deployment/handoffiq.service /etc/systemd/system/handoffiq.service
fi

if [ -f "./deployment/nginx.conf" ]; then
    sudo cp ./deployment/nginx.conf /etc/nginx/nginx.conf
fi

sudo systemctl daemon-reload

# 6. Copy Built Application Artifacts
echo "[6/7] Deploying application artifacts..."
if [ -f "./backend/target/handoffiq-backend-1.0.0.jar" ]; then
    sudo cp ./backend/target/handoffiq-backend-1.0.0.jar /opt/handoffiq/backend/
    sudo chown -R handoffiq:handoffiq /opt/handoffiq
fi

if [ -d "./frontend/dist" ]; then
    sudo cp -r ./frontend/dist/* /var/www/handoffiq/frontend/dist/
    sudo chown -R www-data:www-data /var/www/handoffiq/frontend/dist || sudo chown -R nginx:nginx /var/www/handoffiq/frontend/dist
fi

# 7. Start & Enable Services
echo "[7/7] Starting services..."
sudo systemctl enable nginx
sudo systemctl restart nginx

sudo systemctl enable handoffiq
sudo systemctl restart handoffiq || true

echo "================================================================="
echo "  HandoffIQ Provisioning Complete!                               "
echo "  Check Service Status: sudo systemctl status handoffiq          "
echo "  View Logs: sudo journalctl -u handoffiq -f                     "
echo "================================================================="
