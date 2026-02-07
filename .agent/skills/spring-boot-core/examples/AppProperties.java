package com.example.app.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Type-safe configuration properties.
 *
 * Binds to application.yml:
 *   app:
 *     name: My App
 *     api:
 *       base-url: https://api.example.com
 *       timeout: 30s
 *       retry-attempts: 3
 *     cors:
 *       allowed-origins:
 *         - http://localhost:3000
 *
 * Patterns:
 * - Record-based @ConfigurationProperties (Spring Boot 3.x)
 * - @Validated for startup validation
 * - Nested records for structured config
 * - Duration and List binding
 */
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @NotBlank String name,
        Api api,
        Cors cors
) {
    public record Api(
            @NotBlank String baseUrl,
            Duration timeout,
            @Min(0) @Max(10) int retryAttempts
    ) {
        public Api {
            if (timeout == null) timeout = Duration.ofSeconds(30);
        }
    }

    public record Cors(
            List<String> allowedOrigins
    ) {
        public Cors {
            if (allowedOrigins == null) allowedOrigins = List.of("http://localhost:3000");
        }
    }
}
