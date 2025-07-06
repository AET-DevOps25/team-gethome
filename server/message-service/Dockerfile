# Stage 1: Build the application
FROM gradle:8.14-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy gradle configuration files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8083

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]