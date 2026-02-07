package com.example.app.service;

import com.example.app.dto.CreateUserRequest;
import com.example.app.dto.UpdateUserRequest;
import com.example.app.dto.UserResponse;
import com.example.app.exception.DuplicateEntityException;
import com.example.app.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for User business logic.
 *
 * Patterns:
 * - @Service marks this as a Spring-managed bean (specialization of @Component)
 * - Uses in-memory store (replace with Repository in real apps)
 * - Throws domain exceptions (handled by GlobalExceptionHandler)
 * - Returns DTOs, never exposes internal structures
 * - SLF4J logging (no Lombok here to show explicit pattern)
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // In-memory store (replace with JPA Repository)
    private final Map<UUID, UserRecord> users = new ConcurrentHashMap<>();

    // Internal record (not exposed)
    private record UserRecord(UUID id, String name, String email, LocalDateTime createdAt) {}

    public List<UserResponse> findAll() {
        log.debug("Finding all users, count={}", users.size());
        return users.values().stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(UserResponse::name))
                .toList();
    }

    public UserResponse findById(UUID id) {
        log.debug("Finding user by id={}", id);
        return Optional.ofNullable(users.get(id))
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    public UserResponse create(CreateUserRequest request) {
        log.info("Creating user: name={}, email={}", request.name(), request.email());

        // Check for duplicate email
        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(request.email()));
        if (emailExists) {
            throw new DuplicateEntityException("Email already in use: " + request.email());
        }

        var user = new UserRecord(
                UUID.randomUUID(),
                request.name(),
                request.email().toLowerCase(),
                LocalDateTime.now()
        );
        users.put(user.id(), user);

        log.info("User created: id={}", user.id());
        return toResponse(user);
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        log.info("Updating user: id={}", id);

        var existing = Optional.ofNullable(users.get(id))
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        var updated = new UserRecord(
                existing.id(),
                request.name() != null ? request.name() : existing.name(),
                request.email() != null ? request.email().toLowerCase() : existing.email(),
                existing.createdAt()
        );
        users.put(id, updated);

        log.info("User updated: id={}", id);
        return toResponse(updated);
    }

    public void delete(UUID id) {
        log.info("Deleting user: id={}", id);
        if (users.remove(id) == null) {
            throw new EntityNotFoundException("User", id);
        }
    }

    public List<UserResponse> search(String name, int page, int size) {
        log.debug("Searching users: name={}, page={}, size={}", name, page, size);
        return users.values().stream()
                .filter(u -> name == null || u.name().toLowerCase().contains(name.toLowerCase()))
                .sorted(Comparator.comparing(UserRecord::name))
                .skip((long) page * size)
                .limit(size)
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(UserRecord user) {
        return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
    }
}
