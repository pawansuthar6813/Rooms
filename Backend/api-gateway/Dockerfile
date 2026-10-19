# We need Maven to build the project. We use a multi-stage build.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy the entire backend source code
COPY . .

# Package the api-gateway module
# We run this from the root directory so it can resolve the parent POM
RUN mvn clean package -pl api-gateway -am -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Expose the API Gateway port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
