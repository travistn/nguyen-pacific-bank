# Use Java 21 
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy everything into container
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the app (skip tests for faster deploy)
RUN ./mvnw clean package -DskipTests

# Run the app using Render's PORT
CMD ["java", "-jar", "target/bankingapp-0.0.1-SNAPSHOT.jar"]