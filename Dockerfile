# ---- Build stage ----
FROM gradle:8.8-jdk21 AS build
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
# (If you use version catalogs, also copy gradle/libs.versions.toml)
# COPY gradle/libs.versions.toml gradle/libs.versions.toml
RUN ./gradlew --no-daemon clean build -x test || true

# Copy sources last to maximize caching
COPY src ./src
RUN ./gradlew --no-daemon clean build -x test
# After build you'll usually have build/libs/your-app-*.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Adjust the JAR name if needed:
COPY --from=build /app/build/libs/*-all.jar /app/app.jar  || true
COPY --from=build /app/build/libs/*.jar  /app/app.jar
EXPOSE 8080
# Container-friendly JVM settings
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-XX:+UseContainerSupport","-jar","/app/app.jar"]
