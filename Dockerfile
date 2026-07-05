# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/healthmate-ai.jar app.jar

# Render sets $PORT dynamically; the app already reads server.port=${PORT:8080}
EXPOSE 8080

# Runs with the h2 profile so no external database is required.
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=h2"]
