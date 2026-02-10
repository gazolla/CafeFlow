---
name: java-fundamentals
description: Modern Java 21 development patterns including records, sealed classes, pattern matching, virtual threads, and Stream API. Use for any Java project regardless of framework. Applies when user mentions Java, needs DTOs, value objects, or modern Java syntax.
version: "1.0.0"
author: GazApps
tags: [java, java-21, records, streams, pattern-matching, virtual-threads]
dependencies: []
compatibility: [antigravity, claude-code, gemini-cli]
---

# Java 21 Fundamentals

Modern Java development patterns and best practices for Java 21 LTS.

## Use this skill when

- Writing any Java code (with or without frameworks)
- User asks about Java 21 features (records, sealed classes, pattern matching)
- Working with collections, streams, or functional programming
- Implementing DTOs, value objects, or immutable objects
- Need to choose between records, classes, or enums
- Working with Optional, Stream API, or CompletableFuture
- Using virtual threads or structured concurrency

## Do not use this skill when

- User specifically asks for Kotlin, Scala, or Groovy
- Question is about a specific framework (use spring-boot-core instead)
- User needs legacy Java (8 or 11) compatibility

## Java 21 Key Features

### Records (Immutable Data Carriers)

Records replace traditional POJOs/DTOs with minimal boilerplate:

```java
// Instead of class with getters, equals, hashCode, toString...
public record User(
    UUID id,
    String name,
    String email,
    LocalDateTime createdAt
) {
    // Compact constructor for validation
    public User {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(email, "email cannot be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("invalid email");
        }
    }
    
    // Additional factory methods
    public static User create(String name, String email) {
        return new User(UUID.randomUUID(), name, email, LocalDateTime.now());
    }
}
```

**When to use Records:**
- DTOs (Data Transfer Objects)
- API request/response objects
- Value objects in Domain-Driven Design
- Immutable configuration holders
- Compound map keys

**When NOT to use Records:**
- JPA entities (need mutability for proxies)
- Objects that need inheritance
- Objects with complex mutable state

### Sealed Classes (Controlled Inheritance)

```java
public sealed interface PaymentResult 
    permits PaymentSuccess, PaymentFailure, PaymentPending {
}

public record PaymentSuccess(String transactionId, BigDecimal amount) 
    implements PaymentResult {}

public record PaymentFailure(String errorCode, String message) 
    implements PaymentResult {}

public record PaymentPending(String pendingId, Duration estimatedTime) 
    implements PaymentResult {}
```

### Pattern Matching

```java
// Pattern matching for instanceof
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}

// Pattern matching for switch (Java 21)
String describe(Object obj) {
    return switch (obj) {
        case null -> "null value";
        case String s when s.isBlank() -> "empty string";
        case String s -> "string: " + s;
        case Integer i when i < 0 -> "negative: " + i;
        case Integer i -> "positive: " + i;
        case List<?> list when list.isEmpty() -> "empty list";
        case List<?> list -> "list with " + list.size() + " elements";
        default -> "unknown: " + obj.getClass().getSimpleName();
    };
}

// Record patterns (destructuring)
record Point(int x, int y) {}

void process(Object obj) {
    if (obj instanceof Point(int x, int y)) {
        System.out.println("x=" + x + ", y=" + y);
    }
}

// Switch with record patterns
String describePoint(Point p) {
    return switch (p) {
        case Point(var x, var y) when x == 0 && y == 0 -> "origin";
        case Point(var x, var y) when x == y -> "diagonal";
        case Point(var x, var y) -> "point at " + x + "," + y;
    };
}
```

### Virtual Threads (Project Loom)

```java
// Simple virtual thread
Thread.startVirtualThread(() -> {
    // This runs on a virtual thread
    processRequest();
});

// ExecutorService with virtual threads
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<String>> futures = urls.stream()
        .map(url -> executor.submit(() -> fetchUrl(url)))
        .toList();
    
    for (Future<String> future : futures) {
        System.out.println(future.get());
    }
}

// Structured concurrency (preview in 21)
// For production, use the ExecutorService pattern above
```

**When to use Virtual Threads:**
- I/O-bound operations (HTTP calls, database queries)
- High concurrency scenarios (thousands of concurrent tasks)
- Replacing thread pools for blocking operations

**When NOT to use:**
- CPU-bound computation (use platform threads or ForkJoinPool)
- Tasks requiring thread-local state that must not be shared

### Stream API Patterns

```java
// Collectors
Map<Status, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));

Map<Status, Long> countByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status, Collectors.counting()));

// Teeing collector (combine two collectors)
record Stats(long count, double average) {}

Stats stats = numbers.stream()
    .collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.averagingDouble(Double::doubleValue),
        Stats::new
    ));

// flatMap for nested structures
List<String> allTags = posts.stream()
    .flatMap(post -> post.tags().stream())
    .distinct()
    .sorted()
    .toList();

// Optional with streams
Optional<User> user = findUser(id);
List<Order> orders = user
    .map(User::orders)
    .orElse(List.of());

// Stream from optional
user.stream()
    .flatMap(u -> u.orders().stream())
    .filter(Order::isPending)
    .forEach(this::processOrder);
```

### Text Blocks

```java
String json = """
    {
        "name": "%s",
        "email": "%s",
        "active": true
    }
    """.formatted(name, email);

String sql = """
    SELECT u.id, u.name, u.email
    FROM users u
    WHERE u.active = true
      AND u.created_at > :since
    ORDER BY u.name
    """;
```

### Helpful NullPointerExceptions

Java 21 provides detailed NPE messages automatically:

```
Exception: Cannot invoke "String.length()" because "user.getAddress().getCity()" is null
```

## Best Practices

### 1. Prefer Immutability

```java
// Good: immutable record
public record Config(String host, int port, Duration timeout) {}

// Good: unmodifiable collections
List<String> items = List.of("a", "b", "c");
Map<String, Integer> scores = Map.of("alice", 100, "bob", 95);
Set<String> tags = Set.of("java", "spring");

// Good: defensive copy in constructor
public record Order(String id, List<Item> items) {
    public Order {
        items = List.copyOf(items); // defensive copy
    }
}
```

### 2. Use Optional Correctly

```java
// Good: return type for "might not exist"
public Optional<User> findByEmail(String email) { ... }

// Good: chain operations
String city = findUser(id)
    .map(User::address)
    .map(Address::city)
    .orElse("Unknown");

// Bad: Optional as field
public class User {
    private Optional<String> nickname; // Don't do this
}

// Bad: Optional as parameter
public void process(Optional<String> value) { // Don't do this
    ...
}

// Good: use overloading instead
public void process(String value) { ... }
public void process() { process(null); }
```

### 3. Effective Enums

```java
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
    
    // Parse from string safely
    public static Optional<PaymentMethod> fromString(String value) {
        try {
            return Optional.of(valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
```

### 4. Resource Management

```java
// Always use try-with-resources
try (var input = new FileInputStream(file);
     var reader = new BufferedReader(new InputStreamReader(input))) {
    return reader.lines().toList();
}

// For custom resources
public class Connection implements AutoCloseable {
    @Override
    public void close() {
        // cleanup
    }
}
```

## Code Quality Checklist

- [ ] Using records for immutable data carriers
- [ ] Using sealed types for controlled hierarchies
- [ ] Using pattern matching instead of instanceof chains
- [ ] Streams are readable (consider extracting complex operations)
- [ ] Optional used only for return types
- [ ] Collections are immutable where possible
- [ ] Resources managed with try-with-resources
- [ ] No raw types (always use generics)

## References

- See `references/stream-collectors.md` for collector patterns
- See `examples/` for complete code examples
