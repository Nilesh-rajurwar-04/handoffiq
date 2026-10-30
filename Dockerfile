# ==============================================================================
# HandoffIQ - Unified Full-Stack Production Container (React + Spring Boot)
# Builds React frontend and bundles it into Spring Boot Java 21 executable JAR
# Single Web Service on Render = 100% Free Tier Compatible & Zero CORS Overhead
# ==============================================================================

# Stage 1: Build Frontend Assets with Node 20
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --silent || npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot JAR with Maven & Eclipse Temurin JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app
COPY backend/pom.xml ./backend/
RUN cd backend && (mvn dependency:go-offline -B || true)

COPY backend/src ./backend/src
# Copy built frontend static files into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist ./backend/src/main/resources/static/

RUN cd backend && mvn clean package -DskipTests

# Stage 3: Lightweight Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S handoffiq && adduser -S handoffiq -G handoffiq

COPY --from=backend-builder /app/backend/target/handoffiq-backend-1.0.0.jar app.jar
RUN chown -R handoffiq:handoffiq /app

USER handoffiq

ENV PORT=8085
EXPOSE ${PORT}

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=40.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
