# syntax=docker/dockerfile:1

################################################################################
# Build stage: compile and package the application.
# Uses a Maven image with a bundled Maven install rather than the project's
# mvnw wrapper, since the wrapper's .mvn/ directory isn't checked into this repo.
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build

# Resolve dependencies first so Docker can cache this layer between builds.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests && \
    cp target/*.jar target/app.jar

# Split the fat jar into layers (dependencies, resources, app classes) so that
# rebuilding the image after a code-only change doesn't re-push dependency layers.
FROM eclipse-temurin:17-jre-jammy AS extract
WORKDIR /build
COPY --from=build /build/target/app.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract --destination extracted

################################################################################
# Final runtime image.
FROM eclipse-temurin:17-jre-jammy AS final

ARG UID=10001
RUN adduser --disabled-password --gecos "" --home "/nonexistent" --shell "/sbin/nologin" \
    --no-create-home --uid "${UID}" appuser

WORKDIR /app

# The H2 file database and its lock file live here; give the app user ownership
# so a bind-mounted volume at this path persists data across container restarts.
RUN mkdir -p /app/data && chown -R appuser:appuser /app

COPY --from=extract --chown=appuser:appuser /build/extracted/dependencies/ ./
COPY --from=extract --chown=appuser:appuser /build/extracted/spring-boot-loader/ ./
COPY --from=extract --chown=appuser:appuser /build/extracted/snapshot-dependencies/ ./
COPY --from=extract --chown=appuser:appuser /build/extracted/application/ ./

USER appuser

EXPOSE 8080

# JWT_SECRET should always be overridden in real deployments, e.g.:
#   docker run -e JWT_SECRET=$(openssl rand -base64 48) ...
ENV JWT_SECRET="change-this-secret-in-production-please-make-it-long-and-random"

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
