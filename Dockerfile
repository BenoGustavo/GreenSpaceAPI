# Start from a Maven image with OpenJDK 21
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy the rest of the application code to the container
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Start a new image for the final deployment
FROM eclipse-temurin:21

# Set the working directory inside the container
WORKDIR /app

# Copy the built application from the previous stage
COPY --from=build /app/target/*.jar app.jar

COPY .env .

# Expose the application port
EXPOSE 8080

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
