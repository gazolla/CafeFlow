package com.example.fundamentals;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Examples of Java 21 Virtual Threads (Project Loom)
 *
 * Virtual threads are lightweight threads managed by the JVM,
 * ideal for I/O-bound tasks with high concurrency.
 */
public class VirtualThreadExamples {

    // --- Simple virtual thread ---

    static void simpleVirtualThread() throws InterruptedException {
        Thread vt = Thread.startVirtualThread(() -> {
            System.out.println("Running on: " + Thread.currentThread());
            System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
        });
        vt.join();
    }

    // --- Virtual thread with Thread.Builder ---

    static void namedVirtualThread() throws InterruptedException {
        Thread vt = Thread.ofVirtual()
                .name("my-worker")
                .start(() -> {
                    System.out.println("Thread name: " + Thread.currentThread().getName());
                    simulateIO(Duration.ofMillis(100));
                    System.out.println("Work complete on: " + Thread.currentThread().getName());
                });
        vt.join();
    }

    // --- ExecutorService with virtual threads ---

    static void virtualThreadExecutor() throws Exception {
        List<String> urls = List.of(
                "https://api.example.com/users",
                "https://api.example.com/orders",
                "https://api.example.com/products",
                "https://api.example.com/inventory",
                "https://api.example.com/reports"
        );

        var start = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = urls.stream()
                    .map(url -> executor.submit(() -> fetchUrl(url)))
                    .toList();

            for (Future<String> future : futures) {
                System.out.println(future.get());
            }
        }

        var elapsed = Duration.between(start, Instant.now());
        System.out.println("Total time: " + elapsed.toMillis() + "ms (concurrent)");
    }

    // --- High concurrency with virtual threads ---

    static void highConcurrency() throws Exception {
        int taskCount = 10_000;
        var start = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, taskCount)
                    .mapToObj(i -> executor.submit(() -> {
                        simulateIO(Duration.ofMillis(100));
                        return i;
                    }))
                    .toList();

            long completed = 0;
            for (var future : futures) {
                future.get();
                completed++;
            }
            System.out.println("Completed " + completed + " tasks");
        }

        var elapsed = Duration.between(start, Instant.now());
        System.out.println("10,000 tasks with 100ms I/O each: " + elapsed.toMillis() + "ms");
    }

    // --- Virtual thread factory ---

    static void threadFactory() throws Exception {
        ThreadFactory factory = Thread.ofVirtual()
                .name("worker-", 0)  // worker-0, worker-1, worker-2...
                .factory();

        try (var executor = Executors.newThreadPerTaskExecutor(factory)) {
            for (int i = 0; i < 5; i++) {
                executor.submit(() -> {
                    System.out.println("Task on: " + Thread.currentThread().getName());
                    simulateIO(Duration.ofMillis(50));
                });
            }
        }
    }

    // --- Comparing virtual vs platform threads ---

    static void compareThreadTypes() throws Exception {
        int taskCount = 1_000;

        // Platform threads (limited by OS threads)
        var start = Instant.now();
        try (var executor = Executors.newFixedThreadPool(100)) {
            var futures = IntStream.range(0, taskCount)
                    .mapToObj(i -> executor.submit(() -> simulateIO(Duration.ofMillis(50))))
                    .toList();
            for (var f : futures) f.get();
        }
        var platformTime = Duration.between(start, Instant.now());

        // Virtual threads (no thread pool limit)
        start = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, taskCount)
                    .mapToObj(i -> executor.submit(() -> simulateIO(Duration.ofMillis(50))))
                    .toList();
            for (var f : futures) f.get();
        }
        var virtualTime = Duration.between(start, Instant.now());

        System.out.println("Platform threads (pool=100): " + platformTime.toMillis() + "ms");
        System.out.println("Virtual threads:             " + virtualTime.toMillis() + "ms");
    }

    // --- Practical pattern: parallel data fetching ---

    record UserProfile(String name, List<String> orders, int notificationCount) {}

    static UserProfile fetchUserProfile(String userId) throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> nameFuture = executor.submit(
                    () -> fetchUserName(userId));
            Future<List<String>> ordersFuture = executor.submit(
                    () -> fetchUserOrders(userId));
            Future<Integer> notifFuture = executor.submit(
                    () -> fetchNotificationCount(userId));

            return new UserProfile(
                    nameFuture.get(),
                    ordersFuture.get(),
                    notifFuture.get()
            );
        }
    }

    // --- Simulated I/O operations ---

    private static void simulateIO(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String fetchUrl(String url) {
        simulateIO(Duration.ofMillis(200));
        return "Response from " + url + " [200 OK]";
    }

    private static String fetchUserName(String userId) {
        simulateIO(Duration.ofMillis(100));
        return "User " + userId;
    }

    private static List<String> fetchUserOrders(String userId) {
        simulateIO(Duration.ofMillis(150));
        return List.of("ORD-001", "ORD-002", "ORD-003");
    }

    private static int fetchNotificationCount(String userId) {
        simulateIO(Duration.ofMillis(80));
        return 7;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Simple virtual thread ===");
        simpleVirtualThread();

        System.out.println();
        System.out.println("=== Named virtual thread ===");
        namedVirtualThread();

        System.out.println();
        System.out.println("=== Virtual thread executor ===");
        virtualThreadExecutor();

        System.out.println();
        System.out.println("=== Thread factory ===");
        threadFactory();

        System.out.println();
        System.out.println("=== Parallel data fetching ===");
        var profile = fetchUserProfile("user-42");
        System.out.println("Profile: " + profile);

        System.out.println();
        System.out.println("=== Virtual vs Platform comparison ===");
        compareThreadTypes();

        System.out.println();
        System.out.println("=== High concurrency (10,000 tasks) ===");
        highConcurrency();
    }
}
