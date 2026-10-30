# ==============================================================================
# HandoffIQ Backend - Production Multi-Stage Dockerfile (Java 21)
# Optimized for Render Cloud Web Services (< 200MB memory footprint)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Executable JAR with Maven
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Minimalist Lightweight Production JRE Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for cloud security
RUN addgroup -S handoffiq && adduser -S handoffiq -G handoffiq

# Copy the built jar from builder stage
COPY --from=builder /app/target/handoffiq-backend-1.0.0.jar app.jar
RUN chown -R handoffiq:handoffiq /app

USER handoffiq

# Render passes dynamic PORT environment variable (defaults to 8085)
ENV PORT=8085
EXPOSE ${PORT}

# Optimized JVM memory flags for Render free tier (512MB RAM cap)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=40.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
