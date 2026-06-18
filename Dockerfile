# syntax=docker/dockerfile:1

# Etapa de build
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copiamos primero archivos de Gradle para aprovechar cache de capas
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts* ./
RUN chmod +x gradlew

# Copiamos codigo fuente
COPY src src

# Genera el bootJar ejecutable
RUN ./gradlew clean bootJar -x test --no-daemon

# Etapa de runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copiamos el jar generado desde la etapa builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Render expone el puerto por la variable PORT
ENV PORT=8080
EXPOSE 8080

# Spring tomara server.port desde application.properties (${PORT:8080})
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
