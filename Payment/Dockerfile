# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-slim

# Set the working directory in the container
WORKDIR /app

# Copy the jar file into the container at /app
COPY target/Payment-0.0.1-SNAPSHOT.jar /app/payment-app.jar

# Make port 9001 available to the world outside this container
EXPOSE 9001

# Run the jar file
CMD ["java", "-jar", "/app/payment-app.jar"]
