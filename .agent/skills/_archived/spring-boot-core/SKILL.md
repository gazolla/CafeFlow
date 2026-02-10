---
name: spring-boot-core
description: Spring Boot 3.x core setup, configuration, profiles, properties, actuator, and application structure. Foundation for all Spring-based skills. Use when creating Spring Boot projects or configuring Spring applications.
version: "1.0.0"
author: GazApps
tags: [spring-boot, spring, java, configuration, profiles, actuator]
dependencies: [java-fundamentals]
compatibility: [antigravity, claude-code, gemini-cli]
---

# Spring Boot 3.x Core

Foundation skill for Spring Boot application development.

## Use this skill when

- Creating a new Spring Boot project
- Configuring application.yml or application.properties
- Setting up profiles (dev, prod, test)
- Adding Spring Boot Actuator for monitoring
- Structuring a Spring Boot application
- Configuring logging, server settings, or environment variables
- User mentions "Spring Boot", "Spring", or needs a Java web application

## Do not use this skill when

- User needs JPA/database (use spring-data-jpa)
- User needs Thymeleaf/frontend (use spring-thymeleaf-bootstrap)
- User needs security/OAuth (use spring-security-oauth2)
- User wants non-Spring frameworks (Quarkus, Micronaut)

## Project Setup

### Dependencies (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    
    <properties>
        <java.version>21</java.version>
    </properties>
    
    <dependencies>
        <!-- Core Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Actuator (monitoring) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Lombok (optional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- DevTools (dev only) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Load .env files -->
        <dependency>
            <groupId>me.paulschwarz</groupId>
            <artifactId>spring-dotenv</artifactId>
            <version>4.0.0</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Application Structure

```
src/main/java/com/example/app/
    Application.java              # @SpringBootApplication
    config/                       # @Configuration classes
        WebConfig.java
        AsyncConfig.java
    controller/                   # REST controllers
        api/                      # @RestController
        web/                      # @Controller (if using Thymeleaf)
    service/                      # Business logic
        impl/                     # Implementations
    dto/                          # Request/Response records
    exception/                    # Custom exceptions + handler
    util/                         # Utility classes

src/main/resources/
    application.yml               # Main config
    application-dev.yml           # Dev profile
    application-prod.yml          # Prod profile
    
src/test/java/                    # Tests mirror main structure
```

### Main Application Class

```java
package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Configuration

### application.yml (Main)

```yaml
spring:
  application:
    name: my-app
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

server:
  port: ${PORT:8080}
  error:
    include-message: always
    include-binding-errors: always

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized

# Logging
logging:
  level:
    root: INFO
    com.example.app: ${LOG_LEVEL:INFO}
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### application-dev.yml

```yaml
spring:
  config:
    activate:
      on-profile: dev

server:
  error:
    include-stacktrace: always

logging:
  level:
    com.example.app: DEBUG
    org.springframework.web: DEBUG
```

### application-prod.yml

```yaml
spring:
  config:
    activate:
      on-profile: prod

server:
  error:
    include-stacktrace: never
    include-message: never

logging:
  level:
    root: WARN
    com.example.app: INFO
```

### Environment Variables (.env)

```properties
# .env - DO NOT COMMIT
SPRING_PROFILES_ACTIVE=dev
PORT=8080
LOG_LEVEL=DEBUG

# Add to .gitignore
# .env
```

## REST Controllers

### Basic Controller Pattern

```java
package com.example.app.controller.api;

import com.example.app.dto.*;
import com.example.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
```

### DTOs with Validation

```java
package com.example.app.dto;

import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    String name,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {}

public record UpdateUserRequest(
    @Size(min = 2, max = 100)
    String name,
    
    @Email
    String email
) {}

public record UserResponse(
    UUID id,
    String name,
    String email,
    LocalDateTime createdAt
) {}
```

## Exception Handling

### Global Exception Handler

```java
package com.example.app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.example.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage() != null 
                    ? error.getDefaultMessage() 
                    : "Invalid value",
                (a, b) -> a
            ));
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed"
        );
        problem.setTitle("Validation Error");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"
        );
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
```

### Custom Exceptions

```java
package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Object id) {
        super(String.format("%s not found with id: %s", entity, id));
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
```

## Configuration Classes

### Web Configuration

```java
package com.example.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### Async Configuration

```java
package com.example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

## Common Commands

```bash
# Run application
./mvnw spring-boot:run

# Run with profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Package as JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/my-app-1.0.0-SNAPSHOT.jar

# Run JAR with profile
java -jar -Dspring.profiles.active=prod target/my-app-1.0.0-SNAPSHOT.jar
```

## Actuator Endpoints

When actuator is enabled:

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics list
- `GET /actuator/metrics/{name}` - Specific metric

## Code Quality Checklist

- [ ] Application has clear package structure
- [ ] Configuration uses environment variables for secrets
- [ ] Profiles configured for dev/prod
- [ ] Global exception handler in place
- [ ] DTOs use validation annotations
- [ ] Controllers follow REST conventions
- [ ] Actuator configured for monitoring
- [ ] Logging levels appropriate per profile

## References

- See `references/configuration-properties.md` for all config options
- See `examples/` for complete code examples
