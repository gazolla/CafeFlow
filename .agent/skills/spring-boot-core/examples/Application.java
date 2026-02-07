package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot 3.x Application Entry Point
 *
 * @SpringBootApplication combines:
 *   - @Configuration     (this class is a config source)
 *   - @EnableAutoConfiguration (auto-configure based on classpath)
 *   - @ComponentScan     (scan this package and subpackages)
 *
 * @ConfigurationPropertiesScan auto-detects @ConfigurationProperties classes.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
