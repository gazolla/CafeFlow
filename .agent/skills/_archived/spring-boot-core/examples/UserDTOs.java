package com.example.app.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Records for User API.
 *
 * Patterns:
 * - Records as DTOs (immutable, no boilerplate)
 * - Jakarta Validation annotations on record components
 * - Separate Create/Update request records (Update allows nulls for partial updates)
 * - Response record decoupled from internal entity/model
 *
 * Note: In a real project, each record would be in its own file.
 *       Combined here for reference.
 */
public class UserDTOs {

    // --- Create request: all required fields ---
    public record CreateUserRequest(
            @NotBlank(message = "Name is required")
            @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email
    ) {}

    // --- Update request: all optional (partial update) ---
    public record UpdateUserRequest(
            @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
            String name,

            @Email(message = "Invalid email format")
            String email
    ) {}

    // --- Response: what the API returns ---
    public record UserResponse(
            UUID id,
            String name,
            String email,
            LocalDateTime createdAt
    ) {}

    // --- Paginated response wrapper ---
    public record PageResponse<T>(
            java.util.List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static <T> PageResponse<T> of(
                java.util.List<T> content, int page, int size, long totalElements) {
            int totalPages = (int) Math.ceil((double) totalElements / size);
            return new PageResponse<>(content, page, size, totalElements, totalPages);
        }
    }

    // --- API error response (alternative to ProblemDetail) ---
    public record ErrorResponse(
            int status,
            String title,
            String detail,
            String path,
            LocalDateTime timestamp,
            java.util.Map<String, String> fieldErrors
    ) {
        public ErrorResponse(int status, String title, String detail, String path) {
            this(status, title, detail, path, LocalDateTime.now(), null);
        }
    }
}
