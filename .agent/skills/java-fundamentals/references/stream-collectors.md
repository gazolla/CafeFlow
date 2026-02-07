# Stream Collectors Reference

## Basic Collectors

```java
// toList (Java 16+)
List<String> list = stream.toList(); // unmodifiable

// toList (mutable)
List<String> mutableList = stream.collect(Collectors.toCollection(ArrayList::new));

// toSet
Set<String> set = stream.collect(Collectors.toSet());

// toMap
Map<Long, User> byId = users.stream()
    .collect(Collectors.toMap(User::id, Function.identity()));

// toMap with merge function (handle duplicates)
Map<String, User> byEmail = users.stream()
    .collect(Collectors.toMap(
        User::email,
        Function.identity(),
        (existing, replacement) -> existing // keep first
    ));
```

## Grouping

```java
// groupingBy
Map<Status, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));

// groupingBy with downstream collector
Map<Status, Long> countByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.counting()
    ));

// groupingBy with mapping
Map<Status, List<String>> idsByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.mapping(Order::id, Collectors.toList())
    ));
```

## Reducing

```java
// reduce to single value
BigDecimal total = orders.stream()
    .map(Order::total)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// reducing collector (for use inside groupingBy)
Map<Status, BigDecimal> totalByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.reducing(BigDecimal.ZERO, Order::total, BigDecimal::add)
    ));
```

## Partitioning

```java
// partitioningBy (splits into true/false)
Map<Boolean, List<Order>> partitioned = orders.stream()
    .collect(Collectors.partitioningBy(Order::isPaid));

// with downstream
Map<Boolean, Long> counts = orders.stream()
    .collect(Collectors.partitioningBy(Order::isPaid, Collectors.counting()));
```

## Joining

```java
// simple join
String csv = names.stream().collect(Collectors.joining(", "));

// with prefix/suffix
String json = names.stream()
    .collect(Collectors.joining(", ", "[", "]"));
```

## Summarizing

```java
// summary statistics
DoubleSummaryStatistics stats = orders.stream()
    .collect(Collectors.summarizingDouble(o -> o.total().doubleValue()));
// stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage()

// individual aggregations
Double avg = orders.stream()
    .collect(Collectors.averagingDouble(o -> o.total().doubleValue()));
Long count = orders.stream().collect(Collectors.counting());
```

## Teeing (Java 12+)

```java
record Stats(long count, double average) {}

Stats stats = numbers.stream()
    .collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.averagingDouble(Double::doubleValue),
        Stats::new
    ));

// min and max in one pass
record Range<T>(Optional<T> min, Optional<T> max) {}

Range<BigDecimal> range = orders.stream()
    .map(Order::total)
    .collect(Collectors.teeing(
        Collectors.minBy(Comparator.naturalOrder()),
        Collectors.maxBy(Comparator.naturalOrder()),
        Range::new
    ));
```

## Filtering & FlatMapping (Java 9+)

```java
// filtering inside groupingBy
Map<Status, List<Order>> highValueByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.filtering(o -> o.total().compareTo(threshold) > 0, Collectors.toList())
    ));

// flatMapping inside groupingBy
Map<String, Set<String>> tagsByCustomer = orders.stream()
    .collect(Collectors.groupingBy(
        Order::customerId,
        Collectors.flatMapping(o -> o.tags().stream(), Collectors.toSet())
    ));
```

## Collecting to Specific Types

```java
// unmodifiable list/set/map
List<String> list = stream.collect(Collectors.toUnmodifiableList());
Set<String> set = stream.collect(Collectors.toUnmodifiableSet());
Map<K, V> map = stream.collect(Collectors.toUnmodifiableMap(k, v));

// specific collection type
TreeSet<String> sorted = stream
    .collect(Collectors.toCollection(TreeSet::new));

// toMap with specific map type
TreeMap<String, Order> sorted = orders.stream()
    .collect(Collectors.toMap(Order::id, Function.identity(), (a, b) -> a, TreeMap::new));
```
