# Stage 1: Build the application
# This stage uses a Maven image to build the .jar file
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy the pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the project
COPY src ./src
RUN mvn clean package spring-boot:repackage -DskipTests

# Stage 2: Create the final, small image
# This stage uses a lightweight JRE (Java Runtime) image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# --- FIX: Force DNS resolution for internet access ---
RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf && \
    echo "nameserver 1.1.1.1" >> /etc/resolv.conf

# Copy the built .jar file from the 'builder' stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the worker runs on (as defined in its application.properties)
EXPOSE 8081

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
