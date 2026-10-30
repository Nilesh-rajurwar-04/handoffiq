# ==============================================================================
# HandoffIQ Frontend - Multi-Stage Production Dockerfile (React + Vite + Nginx)
# Optimized for AWS EC2 Deployment
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build React Production Bundle
# ------------------------------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app

# Install dependencies (cached if package files unchanged)
COPY package*.json ./
RUN npm ci --silent || npm install --silent

# Copy frontend source code
COPY . .

# Set default API base URL for same-origin Nginx reverse proxy
ENV VITE_API_BASE_URL=/api

# Build minified production bundle
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 2: Production Nginx Web Server
# ------------------------------------------------------------------------------
FROM nginx:1.27-alpine

# Remove default Nginx welcome configuration
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# Copy built static assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy custom Nginx reverse proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose HTTP port 80 for public access via EC2
EXPOSE 80

# Health check to ensure Nginx is responsive
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost/health || exit 1

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
