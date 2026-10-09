# syntax=docker/dockerfile:1

# ---- build stage: compile, test-free package into one runnable jar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
# Resolve dependencies first so this layer is cached until pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- runtime stage: JRE only, non-root user ---------------------------------
FROM eclipse-temurin:17-jre
RUN useradd --system --no-create-home app
WORKDIR /app
COPY --from=build /src/target/credit-simulator.jar /app/credit-simulator.jar
USER app
# No argument: interactive (run with -it). One argument: path of an input file.
ENTRYPOINT ["java", "-jar", "/app/credit-simulator.jar"]
