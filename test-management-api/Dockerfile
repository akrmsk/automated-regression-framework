# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file to leverage Docker's build cache
COPY pom.xml .

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests. This will create the .jar file.
RUN mvn clean package -DskipTests


# --- Stage 2: Create the final, smaller image ---
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the .jar file from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port that the application runs on
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]