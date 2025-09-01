# ---- Build stage ----
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# Gradle wrapper & config
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Kotlin DSL build files
COPY build.gradle* settings.gradle* ./

# App sources
COPY src ./src

# Build a runnable jar (Spring Boot)
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat jar built by bootJar (name includes version)
ARG JAR_FILE=/app/build/libs/*.jar
COPY --from=build ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
