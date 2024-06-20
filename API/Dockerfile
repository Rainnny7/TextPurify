# Stage 1: Build
FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /home/container

# Copy only the files needed for the build
COPY pom.xml ./
COPY src ./src

# Build the app
RUN mvn clean package -q -DskipTests -T4C

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

WORKDIR /home/container

# Copy the jar from the build stage
COPY --from=build /home/container/target/TextPurify-API.jar ./TextPurify-API.jar

# Expose port
EXPOSE 80

# Set environment variables
ENV HOSTNAME="0.0.0.0"
ENV PORT=80
ENV APP_ENV="production"

# Start the app
CMD ["java", "-jar", "TextPurify-API.jar"]