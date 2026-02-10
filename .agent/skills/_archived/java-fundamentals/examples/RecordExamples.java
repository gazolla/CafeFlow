package com.example.fundamentals;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Examples of Java 21 Records
 */
public class RecordExamples {

    // Simple record
    public record Point(int x, int y) {}

    // Record with validation
    public record Email(String value) {
        public Email {
            Objects.requireNonNull(value, "email cannot be null");
            if (!value.contains("@")) {
                throw new IllegalArgumentException("invalid email format");
            }
            value = value.toLowerCase().trim();
        }
    }

    // Record with factory method
    public record User(UUID id, String name, Email email, LocalDateTime createdAt) {
        
        public User {
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(email, "email cannot be null");
        }
        
        public static User create(String name, String email) {
            return new User(
                UUID.randomUUID(),
                name,
                new Email(email),
                LocalDateTime.now()
            );
        }
    }

    // Record as API response
    public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        LocalDateTime timestamp
    ) {
        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, data, null, LocalDateTime.now());
        }
        
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, null, message, LocalDateTime.now());
        }
    }

    // Sealed interface with record implementations
    public sealed interface Result<T> permits Success, Failure {
    }
    
    public record Success<T>(T value) implements Result<T> {}
    public record Failure<T>(String error, Exception cause) implements Result<T> {
        public Failure(String error) {
            this(error, null);
        }
    }

    public static void main(String[] args) {
        // Using records
        var user = User.create("John Doe", "john@example.com");
        System.out.println("User: " + user.name());
        System.out.println("Email: " + user.email().value());
        
        // Pattern matching with records
        Result<String> result = new Success<>("Hello");
        String message = switch (result) {
            case Success<String>(var value) -> "Got: " + value;
            case Failure<String>(var error, var cause) -> "Error: " + error;
        };
        System.out.println(message);
    }
}
