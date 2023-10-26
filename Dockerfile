# Use the official OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Install netcat
RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

# Create a directory in the container
WORKDIR /app

# Copy the jar file and wait-for-it.sh script into the image
COPY ./build/libs/ExamApplication.jar app.jar
COPY ./wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# Command to wait for Postgres and run the Spring Boot app
CMD ["./wait-for-it.sh", "postgres", "5432", "--", "java", "-jar", "app.jar"]

