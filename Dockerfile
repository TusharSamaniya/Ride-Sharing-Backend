# Stage 1: Build the application
# Use Maven image to compile and package the app
FROM maven:3.9.5-eclipse-temurin-21 AS build

# Set working directory inside container
WORKDIR /app

# Copy pom.xml first (for dependency caching)
COPY pom.xml .

# Download all dependencies
# This layer is cached — only re-runs if pom.xml changes
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR file — skip tests for faster build
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────────────
# Stage 2: Run the application
# Use smaller JRE image (not full JDK) to keep image small
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# The port your Spring Boot app runs on
EXPOSE 8081

# Command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]