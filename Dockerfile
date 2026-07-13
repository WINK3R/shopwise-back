# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn --batch-mode dependency:go-offline

COPY src/main ./src/main
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup --system shopwise && adduser --system --ingroup shopwise shopwise

WORKDIR /app
COPY --from=build /workspace/target/Shopwise-api-*.jar app.jar

USER shopwise
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=5 \
    CMD nc -z localhost 8080 || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
