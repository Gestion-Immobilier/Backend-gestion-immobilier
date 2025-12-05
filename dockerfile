# ---- Étape 1 : Build avec Gradle + Java 21 ----
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY . .

RUN gradle clean build -x test

# ---- Étape 2 : Image finale légère avec Java 21 ----
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
