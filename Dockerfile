# Multi-stage Dockerfile: build with Maven, run with a slim OpenJDK image
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace

# Copy project files and build
COPY pom.xml mvnw .
COPY .mvn .mvn
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jdk-noble
LABEL maintainer=""
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Copy the start script (kept isolated so the runtime command is not embedded in Dockerfile)
COPY docker/start-app.sh /app/start-app.sh
RUN chmod +x /app/start-app.sh

EXPOSE 8080

USER root
ENTRYPOINT ["/app/start-app.sh"]

