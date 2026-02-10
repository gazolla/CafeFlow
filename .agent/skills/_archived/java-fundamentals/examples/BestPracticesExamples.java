package com.example.fundamentals;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Examples of Java 21 Best Practices
 *
 * Covers: immutability, Optional usage, effective enums,
 * resource management, and common patterns.
 */
public class BestPracticesExamples {

    // =============================================
    // 1. IMMUTABILITY
    // =============================================

    // Immutable record for configuration
    public record DatabaseConfig(String host, int port, String database, Duration timeout) {
        public DatabaseConfig {
            Objects.requireNonNull(host, "host cannot be null");
            Objects.requireNonNull(database, "database cannot be null");
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("invalid port: " + port);
            }
            if (timeout == null) {
                timeout = Duration.ofSeconds(30);
            }
        }

        public String jdbcUrl() {
            return "jdbc:postgresql://%s:%d/%s".formatted(host, port, database);
        }
    }

    // Unmodifiable collections
    static void immutableCollections() {
        System.out.println("=== Immutable Collections ===");

        // Factory methods (truly immutable)
        List<String> items = List.of("a", "b", "c");
        Map<String, Integer> scores = Map.of("alice", 100, "bob", 95);
        Set<String> tags = Set.of("java", "spring");

        System.out.println("Items: " + items);
        System.out.println("Scores: " + scores);
        System.out.println("Tags: " + tags);

        // Map.ofEntries for larger maps
        Map<String, String> config = Map.ofEntries(
                Map.entry("db.host", "localhost"),
                Map.entry("db.port", "5432"),
                Map.entry("db.name", "myapp"),
                Map.entry("app.env", "development")
        );
        System.out.println("Config: " + config);
    }

    // Defensive copy in records
    public record Order(String id, List<Item> items) {
        public Order {
            Objects.requireNonNull(id);
            items = List.copyOf(items); // defensive copy — callers can't mutate
        }

        public Order addItem(Item item) {
            var newItems = new ArrayList<>(items);
            newItems.add(item);
            return new Order(id, newItems); // return new instance
        }
    }

    public record Item(String name, int quantity) {}

    // =============================================
    // 2. OPTIONAL USAGE
    // =============================================

    public record Address(String street, String city, String zipCode) {}
    public record User(String id, String name, Address address) {}

    // Good: Optional as return type for "might not exist"
    static Optional<User> findById(String id) {
        var users = Map.of(
                "1", new User("1", "Alice", new Address("123 Main St", "New York", "10001")),
                "2", new User("2", "Bob", null)
        );
        return Optional.ofNullable(users.get(id));
    }

    static void optionalPatterns() {
        System.out.println("=== Optional Patterns ===");

        // Chain operations
        String city = findById("1")
                .map(User::address)
                .map(Address::city)
                .orElse("Unknown");
        System.out.println("City: " + city);

        // Handle absence with action
        findById("99")
                .ifPresentOrElse(
                        user -> System.out.println("Found: " + user.name()),
                        () -> System.out.println("User not found")
                );

        // or() to provide alternative Optional
        Optional<User> user = findById("99")
                .or(() -> findById("1"));
        user.ifPresent(u -> System.out.println("Fallback user: " + u.name()));

        // filter
        Optional<User> withAddress = findById("1")
                .filter(u -> u.address() != null);
        withAddress.ifPresent(u ->
                System.out.println("User with address: " + u.name()));

        // stream() for flatMap in collections
        List<String> userIds = List.of("1", "2", "99");
        List<String> cities = userIds.stream()
                .map(BestPracticesExamples::findById)
                .flatMap(Optional::stream)
                .map(User::address)
                .filter(Objects::nonNull)
                .map(Address::city)
                .toList();
        System.out.println("Cities: " + cities);
    }

    // =============================================
    // 3. EFFECTIVE ENUMS
    // =============================================

    public enum PaymentMethod {
        CREDIT_CARD("Credit Card", true),
        DEBIT_CARD("Debit Card", true),
        PIX("PIX", false),
        BANK_TRANSFER("Bank Transfer", false);

        private final String displayName;
        private final boolean supportsRefund;

        PaymentMethod(String displayName, boolean supportsRefund) {
            this.displayName = displayName;
            this.supportsRefund = supportsRefund;
        }

        public String displayName() { return displayName; }
        public boolean supportsRefund() { return supportsRefund; }

        // Safe parse from string
        public static Optional<PaymentMethod> fromString(String value) {
            try {
                return Optional.of(valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    // Enum with behavior
    public enum HttpStatus {
        OK(200, "OK"),
        CREATED(201, "Created"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        NOT_FOUND(404, "Not Found"),
        INTERNAL_ERROR(500, "Internal Server Error");

        private final int code;
        private final String reason;

        HttpStatus(int code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        public int code() { return code; }
        public String reason() { return reason; }

        public boolean isSuccess() { return code >= 200 && code < 300; }
        public boolean isClientError() { return code >= 400 && code < 500; }
        public boolean isServerError() { return code >= 500; }

        public static Optional<HttpStatus> fromCode(int code) {
            return Arrays.stream(values())
                    .filter(s -> s.code == code)
                    .findFirst();
        }
    }

    // Enum implementing interface
    public interface Describable {
        String describe();
    }

    public enum Season implements Describable {
        SPRING { public String describe() { return "Flowers bloom"; } },
        SUMMER { public String describe() { return "Sun shines"; } },
        AUTUMN { public String describe() { return "Leaves fall"; } },
        WINTER { public String describe() { return "Snow falls"; } };
    }

    static void enumExamples() {
        System.out.println("=== Enum Patterns ===");

        // Safe parsing
        PaymentMethod.fromString("pix")
                .ifPresent(pm -> System.out.println(
                        "%s (refund: %s)".formatted(pm.displayName(), pm.supportsRefund())));

        PaymentMethod.fromString("bitcoin")
                .ifPresentOrElse(
                        pm -> System.out.println("Found: " + pm),
                        () -> System.out.println("Unknown payment method: bitcoin")
                );

        // HttpStatus
        HttpStatus.fromCode(404)
                .ifPresent(s -> System.out.println(
                        "%d %s (client error: %s)".formatted(s.code(), s.reason(), s.isClientError())));

        // Enum with switch
        for (Season season : Season.values()) {
            System.out.println(season + ": " + season.describe());
        }

        // Enum in pattern matching
        var method = PaymentMethod.PIX;
        String message = switch (method) {
            case CREDIT_CARD, DEBIT_CARD -> "Card payment selected";
            case PIX                     -> "Instant payment via PIX";
            case BANK_TRANSFER           -> "Bank transfer (1-3 business days)";
        };
        System.out.println(message);
    }

    // =============================================
    // 4. RESOURCE MANAGEMENT
    // =============================================

    // Custom AutoCloseable resource
    public static class DatabaseConnection implements AutoCloseable {
        private final String url;
        private boolean closed = false;

        public DatabaseConnection(String url) {
            this.url = url;
            System.out.println("  Opened connection to " + url);
        }

        public String query(String sql) {
            if (closed) throw new IllegalStateException("Connection is closed");
            return "Result from: " + sql;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                System.out.println("  Closed connection to " + url);
            }
        }
    }

    static void resourceManagement() throws Exception {
        System.out.println("=== Resource Management ===");

        // try-with-resources (single)
        System.out.println("Single resource:");
        try (var conn = new DatabaseConnection("jdbc:postgresql://localhost/mydb")) {
            System.out.println("  " + conn.query("SELECT 1"));
        }

        // try-with-resources (multiple)
        System.out.println("Multiple resources:");
        try (var conn1 = new DatabaseConnection("jdbc:postgresql://localhost/db1");
             var conn2 = new DatabaseConnection("jdbc:postgresql://localhost/db2")) {
            System.out.println("  " + conn1.query("SELECT * FROM users"));
            System.out.println("  " + conn2.query("SELECT * FROM orders"));
        }

        // File I/O with try-with-resources
        System.out.println("File operations:");
        Path tempFile = Files.createTempFile("example", ".txt");
        try {
            Files.writeString(tempFile, "Hello, Java 21!");
            String content = Files.readString(tempFile);
            System.out.println("  File content: " + content);

            // Reading lines as stream
            Files.writeString(tempFile, "line1\nline2\nline3\n");
            try (var lines = Files.lines(tempFile)) {
                List<String> upperLines = lines
                        .map(String::toUpperCase)
                        .toList();
                System.out.println("  Upper lines: " + upperLines);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // =============================================
    // 5. GENERAL PATTERNS
    // =============================================

    // Builder pattern (when records aren't enough)
    public static class HttpRequest {
        private final String method;
        private final String url;
        private final Map<String, String> headers;
        private final String body;

        private HttpRequest(Builder builder) {
            this.method = builder.method;
            this.url = builder.url;
            this.headers = Map.copyOf(builder.headers);
            this.body = builder.body;
        }

        public String method() { return method; }
        public String url() { return url; }
        public Map<String, String> headers() { return headers; }
        public String body() { return body; }

        @Override
        public String toString() {
            return "%s %s headers=%s body=%s".formatted(method, url, headers, body);
        }

        public static Builder builder(String method, String url) {
            return new Builder(method, url);
        }

        public static class Builder {
            private final String method;
            private final String url;
            private final Map<String, String> headers = new LinkedHashMap<>();
            private String body;

            private Builder(String method, String url) {
                this.method = method;
                this.url = url;
            }

            public Builder header(String key, String value) {
                headers.put(key, value);
                return this;
            }

            public Builder body(String body) {
                this.body = body;
                return this;
            }

            public HttpRequest build() {
                return new HttpRequest(this);
            }
        }
    }

    static void generalPatterns() {
        System.out.println("=== General Patterns ===");

        // Builder
        var request = HttpRequest.builder("POST", "/api/users")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer token123")
                .body("""
                        {"name": "Alice", "email": "alice@example.com"}
                        """)
                .build();
        System.out.println("Request: " + request);

        // Immutable record with add/remove returning new instance
        var order = new Order("ORD-001", List.of(
                new Item("Laptop", 1),
                new Item("Mouse", 2)
        ));
        System.out.println("Order: " + order);

        var updated = order.addItem(new Item("Keyboard", 1));
        System.out.println("Updated: " + updated);
        System.out.println("Original unchanged: " + order);
    }

    public static void main(String[] args) throws Exception {
        immutableCollections();
        System.out.println();
        optionalPatterns();
        System.out.println();
        enumExamples();
        System.out.println();
        resourceManagement();
        System.out.println();
        generalPatterns();
    }
}
