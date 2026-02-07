# Spring Boot Configuration Properties Reference

Quick-lookup for the most common `application.yml` properties.

## Server

```yaml
server:
  port: 8080                          # server port
  servlet:
    context-path: /api                # base path for all endpoints
  error:
    include-message: always           # always | never | on_param
    include-binding-errors: always    # always | never | on_param
    include-stacktrace: never         # always | never | on_param | on_trace_param
  compression:
    enabled: true
    mime-types: application/json,text/html
    min-response-size: 1024           # bytes
  shutdown: graceful                  # graceful | immediate
  tomcat:
    max-threads: 200
    connection-timeout: 20s
    max-connections: 8192
```

## Spring Core

```yaml
spring:
  application:
    name: my-app

  profiles:
    active: dev                       # comma-separated for multiple
    default: dev                      # fallback if none active
    group:
      prod: prod,monitoring           # activate multiple profiles together

  main:
    banner-mode: off                  # off | console | log
    lazy-initialization: false        # true delays bean creation
    allow-bean-definition-overriding: false

  config:
    import:                           # import additional config sources
      - optional:file:./config/
      - optional:configtree:/etc/secrets/
```

## Jackson (JSON)

```yaml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false       # ISO-8601 format
      indent-output: false                   # pretty-print
      fail-on-empty-beans: false
    deserialization:
      fail-on-unknown-properties: false      # ignore unknown JSON fields
    default-property-inclusion: non_null      # non_null | non_empty | non_absent | always
    date-format: "yyyy-MM-dd'T'HH:mm:ss"
    time-zone: UTC
```

## Logging

```yaml
logging:
  level:
    root: INFO
    com.example.app: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG                  # show SQL queries

  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

  file:
    name: /var/log/app/application.log       # log file path

  logback:
    rollingpolicy:
      max-file-size: 50MB
      max-history: 30                        # days
      total-size-cap: 1GB
```

## Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics         # "*" for all
        exclude: shutdown
      base-path: /actuator

  endpoint:
    health:
      show-details: when_authorized          # always | never | when_authorized
      show-components: always
    shutdown:
      enabled: false                         # enable POST /actuator/shutdown

  info:
    env:
      enabled: true                          # expose info.* properties
    git:
      mode: full                             # full | simple

  metrics:
    tags:
      application: ${spring.application.name}
```

## Common Actuator Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/actuator/health` | GET | Health status |
| `/actuator/health/liveness` | GET | Kubernetes liveness probe |
| `/actuator/health/readiness` | GET | Kubernetes readiness probe |
| `/actuator/info` | GET | Application info |
| `/actuator/metrics` | GET | List available metrics |
| `/actuator/metrics/{name}` | GET | Specific metric value |
| `/actuator/env` | GET | Environment properties |
| `/actuator/beans` | GET | All Spring beans |
| `/actuator/mappings` | GET | All @RequestMapping routes |
| `/actuator/configprops` | GET | All @ConfigurationProperties |
| `/actuator/loggers` | GET | Logger levels |
| `/actuator/loggers/{name}` | POST | Change logger level at runtime |
| `/actuator/prometheus` | GET | Prometheus metrics format |

## DevTools

```yaml
spring:
  devtools:
    restart:
      enabled: true
      additional-paths: src/main/java       # watch additional paths
      exclude: static/**,public/**          # don't restart for these
    livereload:
      enabled: true
      port: 35729
```

## Profiles

```yaml
# In application-{profile}.yml, always declare:
spring:
  config:
    activate:
      on-profile: dev     # which profile this file applies to
```

## Property Binding Patterns

```yaml
# Environment variables override (automatic conversion):
#   SERVER_PORT=9090         → server.port=9090
#   SPRING_PROFILES_ACTIVE   → spring.profiles.active
#   APP_API_BASE_URL         → app.api.base-url

# Placeholder syntax:
server:
  port: ${PORT:8080}                    # env var with default

# Duration syntax (for timeout, interval, etc.):
app:
  timeout: 30s          # 30 seconds
  interval: 5m          # 5 minutes
  ttl: 2h               # 2 hours
  delay: 500ms          # 500 milliseconds

# DataSize syntax:
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

## Property Source Priority (highest to lowest)

1. Command line args (`--server.port=9090`)
2. `SPRING_APPLICATION_JSON` environment variable
3. OS environment variables (`SERVER_PORT=9090`)
4. Profile-specific `application-{profile}.yml`
5. `application.yml`
6. `@PropertySource` annotations
7. Default properties
