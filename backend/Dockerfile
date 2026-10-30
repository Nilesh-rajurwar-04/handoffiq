# ==============================================================================
# HandoffIQ Backend - Production Multi-Stage Dockerfile (Java 21 Spring Boot)
# Optimized for AWS EC2 Deployment & Amazon ECR Container Registry
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Executable JAR with Maven & Eclipse Temurin JDK 21
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production package (skipping tests for container build)
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Minimalist Lightweight Production JRE Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for Docker health check
RUN apk add --no-cache wget

# Run as non-root user for security best practices
RUN addgroup -S handoffiq && adduser -S handoffiq -G handoffiq

# Copy the built jar from builder stage
COPY --from=builder /app/target/handoffiq-backend-1.0.0.jar app.jar
RUN chown -R handoffiq:handoffiq /app

USER handoffiq

# Configure internal backend port
ENV PORT=8085
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8085

# Health check to ensure Spring Boot and database connection are live
HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8085/api/dashboard/stats || exit 1

# Production JVM memory tuning: container-aware RAM allocation
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=40.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dserver.address=0.0.0.0", \
  "-Dserver.port=8085", \
  "-jar", "app.jar"]
