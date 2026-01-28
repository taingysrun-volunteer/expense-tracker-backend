# ---------- Build stage ----------
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper + configs first
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Warm up dependency cache (ONLY ONCE)
RUN ./gradlew build --no-daemon -x test || true

# Copy source code last
COPY src src

# Build jar (incremental, cached)
RUN ./gradlew bootJar --no-daemon -x test --parallel --build-cache

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
