# We need Maven to build the project. We use a multi-stage build.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the entire backend source code
COPY . .

# Package the service-registry module
# We run this from the root directory so it can resolve the parent POM
RUN mvn clean package -pl service-registry -am -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/service-registry/target/*.jar app.jar

# Expose the default Eureka port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
