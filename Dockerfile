
# Build the Spring Boot application
FROM maven:3.9.16-eclipse-temurin-25 AS build

WORKDIR /app
COPY . .

RUN mvn -B -DskipTests clean package

# Run the application
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
