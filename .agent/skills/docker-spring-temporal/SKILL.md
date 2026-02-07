---
name: docker-spring-temporal
description: Docker and Docker Compose configuration for Spring Boot and Temporal.
---

# Docker Spring-Temporal Skill

This skill provides patterns for containerizing a Spring Boot application that uses Temporal, including the setup for a local Temporal development environment.

## 1. Spring Boot Dockerfile

Multi-stage Dockerfile for a Maven-based project.

```dockerfile
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 2. Docker Compose (Full Stack)

Includes the application, Temporal Server, UI, Admin Tools, and a PostgreSQL database.

> [!IMPORTANT]
> **Always include healthchecks** for the database to prevent Temporal from starting before PostgreSQL is ready.

```yaml
version: '3.8'

services:
  # The Application
  app:
    build: .
    environment:
      - SPRING_TEMPORAL_CONNECTION_TARGET=temporal:7233
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/myapp
    depends_on:
      temporal:
        condition: service_started

  # Temporal Server
  temporal:
    image: temporalio/auto-setup:1.24.0
    ports:
      - "7233:7233"
      - "8233:8233"
    environment:
      - DB=postgresql
      - DB_PORT=5432
      - POSTGRES_USER=temporal
      - POSTGRES_PWD=temporal
      - POSTGRES_SEEDS=db
      - DYNAMIC_CONFIG_FILE_PATH=config/dynamicconfig/development-sql.yaml
    depends_on:
      db:
        condition: service_healthy

  # Temporal UI (use port 8081 to avoid conflicts)
  temporal-ui:
    image: temporalio/ui:latest
    ports:
      - "8081:8080"
    environment:
      - TEMPORAL_ADDRESS=temporal:7233
    depends_on:
      - temporal

  # Temporal Admin Tools (CLI access)
  temporal-admin-tools:
    image: temporalio/admin-tools:1.24.0
    container_name: temporal-admin-tools
    stdin_open: true
    tty: true
    environment:
      - TEMPORAL_ADDRESS=temporal:7233
    depends_on:
      - temporal

  # Database with healthcheck
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=myapp
      - POSTGRES_USER=temporal
      - POSTGRES_PASSWORD=temporal
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U temporal"]
      interval: 5s
      timeout: 5s
      retries: 10
```

> [!TIP]
> **Use temporal-admin-tools for CLI access:**
> ```bash
> docker exec temporal-admin-tools temporal workflow list
> docker exec temporal-admin-tools temporal workflow show --workflow-id <id>
> ```

## References
- See `examples/` for full `docker-compose.yml.example`.
- See `examples/` for `advanced-docker-compose.yml` (includes Admin Tools).
- See `references/official-postgres.yml` for the standard Temporal Postgres configuration.
- See `references/temporal-docker-config.md` for troubleshooting.

