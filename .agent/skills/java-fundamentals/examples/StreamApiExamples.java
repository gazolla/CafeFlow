package com.example.fundamentals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Examples of Java 21 Stream API Patterns
 *
 * Covers: collectors, grouping, teeing, flatMap,
 * Optional with streams, and practical use cases.
 */
public class StreamApiExamples {

    // --- Domain records ---

    public enum Status { PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED }

    public record Product(String name, BigDecimal price, List<String> tags) {}

    public record OrderItem(Product product, int quantity) {
        public BigDecimal total() {
            return product.price().multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Order(
            String id,
            String customerId,
            Status status,
            List<OrderItem> items,
            LocalDate createdAt
    ) {
        public BigDecimal total() {
            return items.stream()
                    .map(OrderItem::total)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public boolean isPending() {
            return status == Status.PENDING;
        }
    }

    public record Customer(String id, String name, String city) {}

    // --- Grouping collectors ---

    static void groupingExamples(List<Order> orders) {
        System.out.println("=== Grouping ===");

        // Group by status
        Map<Status, List<Order>> byStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::status));
        byStatus.forEach((status, list) ->
                System.out.println(status + ": " + list.size() + " orders"));

        System.out.println();

        // Count by status
        Map<Status, Long> countByStatus = orders.stream()
                .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
        System.out.println("Count by status: " + countByStatus);

        // Sum by status
        Map<Status, BigDecimal> totalByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::status,
                        Collectors.reducing(BigDecimal.ZERO, Order::total, BigDecimal::add)
                ));
        System.out.println("Total by status: " + totalByStatus);

        // Group by customer, then count
        Map<String, Long> orderCountByCustomer = orders.stream()
                .collect(Collectors.groupingBy(Order::customerId, Collectors.counting()));
        System.out.println("Orders per customer: " + orderCountByCustomer);
    }

    // --- Teeing collector ---

    record Stats(long count, double average) {}

    static void teeingExample(List<Order> orders) {
        System.out.println("=== Teeing collector ===");

        Stats stats = orders.stream()
                .map(Order::total)
                .map(BigDecimal::doubleValue)
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.averagingDouble(Double::doubleValue),
                        Stats::new
                ));
        System.out.println("Order stats: " + stats);

        // Min and max in one pass
        record Range(Optional<BigDecimal> min, Optional<BigDecimal> max) {}

        Range range = orders.stream()
                .map(Order::total)
                .collect(Collectors.teeing(
                        Collectors.minBy(Comparator.naturalOrder()),
                        Collectors.maxBy(Comparator.naturalOrder()),
                        Range::new
                ));
        System.out.println("Price range: " + range);
    }

    // --- flatMap for nested structures ---

    static void flatMapExamples(List<Order> orders) {
        System.out.println("=== flatMap ===");

        // All unique product tags across all orders
        List<String> allTags = orders.stream()
                .flatMap(order -> order.items().stream())
                .flatMap(item -> item.product().tags().stream())
                .distinct()
                .sorted()
                .toList();
        System.out.println("All tags: " + allTags);

        // All products ordered (unique by name)
        List<String> allProducts = orders.stream()
                .flatMap(order -> order.items().stream())
                .map(item -> item.product().name())
                .distinct()
                .sorted()
                .toList();
        System.out.println("All products: " + allProducts);

        // Total quantity per product
        Map<String, Integer> quantityByProduct = orders.stream()
                .flatMap(order -> order.items().stream())
                .collect(Collectors.groupingBy(
                        item -> item.product().name(),
                        Collectors.summingInt(OrderItem::quantity)
                ));
        System.out.println("Quantity per product: " + quantityByProduct);
    }

    // --- Optional with streams ---

    static void optionalExamples(List<Order> orders, Map<String, Customer> customers) {
        System.out.println("=== Optional with streams ===");

        // Find first pending order
        Optional<Order> firstPending = orders.stream()
                .filter(Order::isPending)
                .findFirst();
        firstPending.ifPresent(o -> System.out.println("First pending: " + o.id()));

        // Chain optional operations
        String customerCity = firstPending
                .map(Order::customerId)
                .map(customers::get)
                .map(Customer::city)
                .orElse("Unknown");
        System.out.println("Customer city: " + customerCity);

        // Stream from optional (useful for flatMap in streams)
        List<String> pendingCustomerCities = orders.stream()
                .filter(Order::isPending)
                .map(Order::customerId)
                .map(id -> Optional.ofNullable(customers.get(id)))
                .flatMap(Optional::stream)
                .map(Customer::city)
                .distinct()
                .toList();
        System.out.println("Pending order cities: " + pendingCustomerCities);
    }

    // --- Partitioning ---

    static void partitioningExample(List<Order> orders) {
        System.out.println("=== Partitioning ===");

        Map<Boolean, List<Order>> partitioned = orders.stream()
                .collect(Collectors.partitioningBy(
                        o -> o.total().compareTo(new BigDecimal("100")) > 0
                ));
        System.out.println("High-value orders: " + partitioned.get(true).size());
        System.out.println("Low-value orders: " + partitioned.get(false).size());
    }

    // --- Reducing and summarizing ---

    static void reducingExamples(List<Order> orders) {
        System.out.println("=== Reducing ===");

        // Total revenue
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.status() != Status.CANCELLED)
                .map(Order::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Total revenue: " + totalRevenue);

        // Most expensive order
        orders.stream()
                .max(Comparator.comparing(Order::total))
                .ifPresent(o -> System.out.println(
                        "Most expensive: %s ($%s)".formatted(o.id(), o.total())));

        // Summary statistics
        DoubleSummaryStatistics stats = orders.stream()
                .mapToDouble(o -> o.total().doubleValue())
                .summaryStatistics();
        System.out.println("Stats: count=%d, avg=%.2f, min=%.2f, max=%.2f".formatted(
                stats.getCount(), stats.getAverage(), stats.getMin(), stats.getMax()));
    }

    // --- Collecting to specific collection types ---

    static void collectingExamples(List<Order> orders) {
        System.out.println("=== Collecting ===");

        // To unmodifiable set
        Set<String> customerIds = orders.stream()
                .map(Order::customerId)
                .collect(Collectors.toUnmodifiableSet());
        System.out.println("Unique customers: " + customerIds);

        // To map (order id -> total)
        Map<String, BigDecimal> orderTotals = orders.stream()
                .collect(Collectors.toMap(Order::id, Order::total));
        System.out.println("Order totals: " + orderTotals);

        // To sorted map
        TreeMap<String, BigDecimal> sortedTotals = orders.stream()
                .collect(Collectors.toMap(
                        Order::id,
                        Order::total,
                        BigDecimal::add,
                        TreeMap::new
                ));
        System.out.println("Sorted totals: " + sortedTotals);

        // Joining strings
        String orderSummary = orders.stream()
                .map(o -> "%s(%s)".formatted(o.id(), o.status()))
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Orders: " + orderSummary);
    }

    // --- Stream.of, Stream.concat, Stream.iterate ---

    static void streamCreation() {
        System.out.println("=== Stream creation ===");

        // Merge streams
        var list1 = List.of("a", "b");
        var list2 = List.of("c", "d");
        var merged = Stream.concat(list1.stream(), list2.stream()).toList();
        System.out.println("Merged: " + merged);

        // Generate sequence
        var fibonacci = Stream.iterate(
                new long[]{0, 1},
                f -> new long[]{f[1], f[0] + f[1]}
        ).limit(10).map(f -> f[0]).toList();
        System.out.println("Fibonacci: " + fibonacci);

        // takeWhile / dropWhile
        var sorted = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        var prefix = sorted.stream().takeWhile(n -> n <= 5).toList();
        var suffix = sorted.stream().dropWhile(n -> n <= 5).toList();
        System.out.println("Take while <= 5: " + prefix);
        System.out.println("Drop while <= 5: " + suffix);
    }

    // --- Sample data ---

    static List<Order> sampleOrders() {
        var laptop = new Product("Laptop", new BigDecimal("999.99"), List.of("electronics", "computer"));
        var mouse = new Product("Mouse", new BigDecimal("29.99"), List.of("electronics", "accessory"));
        var book = new Product("Java Book", new BigDecimal("49.99"), List.of("books", "programming"));
        var keyboard = new Product("Keyboard", new BigDecimal("79.99"), List.of("electronics", "accessory"));

        return List.of(
                new Order("ORD-001", "C1", Status.DELIVERED,
                        List.of(new OrderItem(laptop, 1), new OrderItem(mouse, 2)),
                        LocalDate.of(2024, 1, 15)),
                new Order("ORD-002", "C2", Status.PENDING,
                        List.of(new OrderItem(book, 3)),
                        LocalDate.of(2024, 2, 1)),
                new Order("ORD-003", "C1", Status.SHIPPED,
                        List.of(new OrderItem(keyboard, 1), new OrderItem(mouse, 1)),
                        LocalDate.of(2024, 2, 10)),
                new Order("ORD-004", "C3", Status.PENDING,
                        List.of(new OrderItem(laptop, 2)),
                        LocalDate.of(2024, 2, 20)),
                new Order("ORD-005", "C2", Status.CANCELLED,
                        List.of(new OrderItem(book, 1)),
                        LocalDate.of(2024, 3, 1))
        );
    }

    public static void main(String[] args) {
        var orders = sampleOrders();
        var customers = Map.of(
                "C1", new Customer("C1", "Alice", "New York"),
                "C2", new Customer("C2", "Bob", "London"),
                "C3", new Customer("C3", "Carlos", "São Paulo")
        );

        groupingExamples(orders);
        System.out.println();
        teeingExample(orders);
        System.out.println();
        flatMapExamples(orders);
        System.out.println();
        optionalExamples(orders, customers);
        System.out.println();
        partitioningExample(orders);
        System.out.println();
        reducingExamples(orders);
        System.out.println();
        collectingExamples(orders);
        System.out.println();
        streamCreation();
    }
}
