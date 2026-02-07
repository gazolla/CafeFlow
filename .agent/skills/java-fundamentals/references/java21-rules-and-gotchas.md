# Java 21 Rules & Gotchas

Quick reference for constraints, limitations, and common mistakes.

## Records

| Rule | Detail |
|---|---|
| Implicitly `final` | Cannot be extended |
| Cannot extend classes | Can only implement interfaces |
| All fields are `final` | No mutable state |
| Canonical constructor | Must assign all fields; compact form omits parameter list |
| Compact constructor | Assigns fields automatically at the end — don't use `this.field =` |
| Custom accessors | Override `name()` not `getName()` — no `get` prefix |
| Serializable | Records work with `Serializable` — fields are deserialized via canonical constructor |
| No JPA entities | Hibernate/JPA need mutable proxies — use classes for `@Entity` |

```java
// Compact constructor — do NOT assign this.field manually
public record Email(String value) {
    public Email {          // no (String value) — that's the compact form
        value = value.trim().toLowerCase();  // reassign parameter, not this.value
    }
}
```

## Sealed Types

| Rule | Detail |
|---|---|
| `permits` required | Unless all subtypes are in the same file |
| Subtypes must be `final`, `sealed`, or `non-sealed` | Every permitted type must declare its extensibility |
| Same package/module | Permitted types must be in the same package (or module) |
| Enables exhaustive switch | Compiler verifies all cases — no `default` needed |

## Pattern Matching

| Rule | Detail |
|---|---|
| `null` must be explicit | `case null ->` is required or null throws NPE on switch |
| Dominance ordering | Specific cases before general — `case String s when s.isBlank()` before `case String s` |
| Guard syntax | Use `when`, not `if` — `case String s when s.length() > 5` |
| `default` optional | Only optional with sealed types; required for unsealed |
| Record patterns | Destructure with `case Point(var x, var y)` — types or `var` |
| Unnamed variables | Use `_` for unused bindings (Java 21) — `case Point(var x, var _)` |

## Virtual Threads

| Rule | Detail |
|---|---|
| Not for CPU-bound work | Use platform threads or `ForkJoinPool` for computation |
| Avoid `synchronized` | Use `ReentrantLock` instead — `synchronized` pins the carrier thread |
| No thread pooling | Don't pool virtual threads — create new ones per task |
| Thread-local caution | Virtual threads support ThreadLocal but millions of threads = memory pressure |
| No `Thread.stop()`/`suspend()` | Deprecated methods don't work on virtual threads |
| `isVirtual()` | Check with `Thread.currentThread().isVirtual()` |

```java
// BAD — pins carrier thread
synchronized (lock) {
    blockingIO();
}

// GOOD — virtual-thread friendly
lock.lock();
try {
    blockingIO();
} finally {
    lock.unlock();
}
```

## Stream API

| Gotcha | Detail |
|---|---|
| Streams are single-use | Cannot reuse after terminal operation |
| `toList()` returns unmodifiable | Use `collect(Collectors.toCollection(ArrayList::new))` for mutable |
| `findFirst()` vs `findAny()` | `findAny()` is faster in parallel but non-deterministic |
| `peek()` is for debugging | Not guaranteed to execute on short-circuiting operations |
| Parallel streams | Only for CPU-bound + large data; adds overhead for small collections |
| `toMap` throws on duplicates | Provide merge function: `toMap(k, v, (a, b) -> a)` |
| `flatMap` flattens nulls | If mapper returns null stream, it throws NPE — return `Stream.empty()` |

## Text Blocks

| Feature | Syntax |
|---|---|
| Trailing whitespace preserved | `\s` (space fence) at end of line |
| Line continuation | `\` at end of line — no newline in output |
| Indentation stripping | Common leading whitespace removed based on closing `"""` position |
| `formatted()` | Same as `String.format()` — `"""...""".formatted(args)` |
| `indent(n)` | Adjusts indentation by n spaces (negative removes) |
| `stripIndent()` | Removes common leading whitespace (already done by compiler for text blocks) |

## Optional

| Rule | Detail |
|---|---|
| Return types only | Never use as field, parameter, or collection element |
| Never `Optional.get()` without check | Use `orElse`, `orElseGet`, `orElseThrow`, `ifPresent` |
| `orElse` vs `orElseGet` | `orElse(compute())` always evaluates; `orElseGet(() -> compute())` is lazy |
| Not serializable | Another reason not to use as fields |
| `stream()` | Converts to 0-or-1 element stream — useful in `flatMap` |

## Collections

| Method | Returns | Mutable? | Null elements? |
|---|---|---|---|
| `List.of(...)` | Unmodifiable list | No | No |
| `List.copyOf(c)` | Unmodifiable copy | No | No |
| `stream.toList()` | Unmodifiable list | No | Yes |
| `Collectors.toList()` | ArrayList | Yes | Yes |
| `Collections.unmodifiableList(l)` | Unmodifiable view | View only | Yes |
