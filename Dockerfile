# ---------- Build stage ----------
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy only Gradle files first (better caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy the rest of the source
COPY src src

# Build jar (skip tests)
RUN ./gradlew clean bootJar --no-daemon -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
