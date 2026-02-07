package com.example.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom domain exceptions.
 *
 * Patterns:
 * - Extend RuntimeException (unchecked) for cleaner service code
 * - @ResponseStatus as fallback if no @ExceptionHandler matches
 * - Meaningful constructors with context (entity type, id, etc.)
 *
 * Note: In a real project, each exception would be in its own file.
 *       Combined here for reference.
 */
public class CustomExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class EntityNotFoundException extends RuntimeException {
        public EntityNotFoundException(String entity, Object id) {
            super("%s not found with id: %s".formatted(entity, id));
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateEntityException extends RuntimeException {
        public DuplicateEntityException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidRequestException extends RuntimeException {
        public InvalidRequestException(String message) {
            super(message);
        }
    }
}
