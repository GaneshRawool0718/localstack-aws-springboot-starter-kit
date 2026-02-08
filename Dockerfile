# =============================================================================
# Stage 1: Build — compile the Spring Boot application with Maven
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy POM and resolve dependencies first (cached layer — only re-runs when pom.xml changes)
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -B || \
    mvn dependency:resolve dependency:resolve-plugins -B

# Copy source code and build the JAR (skip tests — they run in CI)
COPY src ./src
RUN mvn package -DskipTests -B

# =============================================================================
# Stage 2: Runtime — minimal JRE image to run the application
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Health check using wget (curl is not available in Alpine by default)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
