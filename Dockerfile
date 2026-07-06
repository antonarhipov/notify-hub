# syntax=docker/dockerfile:1

# ---- Build stage: compile the app with the committed Maven wrapper ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Resolve dependencies first so this layer is cached across source-only changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline

# Build the fat jar.
COPY src/ src/
RUN ./mvnw -q clean package -DskipTests

# ---- Runtime stage: slim JRE, non-root ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl is used by the compose healthcheck; create an unprivileged user to run as.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app --home /app app
USER app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
