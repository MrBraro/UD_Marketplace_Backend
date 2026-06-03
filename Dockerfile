# ==========================================
#  Stage 1: Build the application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy Maven POM and dependency descriptors to leverage Docker layer caching
COPY pom.xml .

# Fetch all Maven dependencies (offline mode cache)
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src/ ./src/

# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Download dependencies first to leverage Docker cache
RUN mvn dependency:go-offline -B

# Copy project source code
COPY src/ ./src/
COPY config/ ./config/

# Compile and package application without tests
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime container
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy generated JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

USER appuser

# Application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]