package com.example.fundamentals;

import java.time.LocalDate;
import java.util.List;

/**
 * Examples of Java 21 Pattern Matching
 *
 * Covers: instanceof patterns, switch patterns, guarded patterns,
 * record patterns (destructuring), and nested patterns.
 */
public class PatternMatchingExamples {

    // Records used in pattern matching examples
    public record Point(int x, int y) {}
    public record Line(Point start, Point end) {}

    public sealed interface Expr permits Literal, Add, Multiply, Negate {}
    public record Literal(double value) implements Expr {}
    public record Add(Expr left, Expr right) implements Expr {}
    public record Multiply(Expr left, Expr right) implements Expr {}
    public record Negate(Expr operand) implements Expr {}

    public sealed interface Event permits ClickEvent, KeyEvent, ScrollEvent {}
    public record ClickEvent(int x, int y, int button) implements Event {}
    public record KeyEvent(String key, boolean ctrl, boolean shift) implements Event {}
    public record ScrollEvent(int delta) implements Event {}

    // --- Pattern matching for instanceof ---

    static void instanceofPatterns(Object obj) {
        // Basic pattern
        if (obj instanceof String s) {
            System.out.println("String of length " + s.length());
        }

        // Pattern with guard
        if (obj instanceof String s && s.length() > 5) {
            System.out.println("Long string: " + s.toUpperCase());
        }

        // Pattern in negative condition
        if (!(obj instanceof String s)) {
            System.out.println("Not a string: " + obj);
            return;
        }
        // s is in scope here because we returned above if not String
        System.out.println("Is a string: " + s);
    }

    // --- Pattern matching for switch ---

    static String describe(Object obj) {
        return switch (obj) {
            case null                                -> "null value";
            case String s when s.isBlank()           -> "empty string";
            case String s                            -> "string: \"%s\" (len=%d)".formatted(s, s.length());
            case Integer i when i < 0                -> "negative int: " + i;
            case Integer i                           -> "int: " + i;
            case Long l                              -> "long: " + l;
            case Double d when d.isNaN()             -> "NaN";
            case Double d when d.isInfinite()        -> "Infinity";
            case Double d                            -> "double: " + d;
            case Boolean b                           -> b ? "true" : "false";
            case List<?> list when list.isEmpty()    -> "empty list";
            case List<?> list                        -> "list with %d elements".formatted(list.size());
            case int[] arr                           -> "int array [%d]".formatted(arr.length);
            default                                  -> "unknown: " + obj.getClass().getSimpleName();
        };
    }

    // --- Record patterns (destructuring) ---

    static String describePoint(Object obj) {
        // Destructure a record in instanceof
        if (obj instanceof Point(int x, int y)) {
            return "Point at (%d, %d)".formatted(x, y);
        }
        return "Not a point";
    }

    static String classifyPoint(Point p) {
        return switch (p) {
            case Point(var x, var y) when x == 0 && y == 0 -> "origin";
            case Point(var x, var y) when x == 0            -> "on Y-axis at y=" + y;
            case Point(var x, var y) when y == 0            -> "on X-axis at x=" + x;
            case Point(var x, var y) when x == y            -> "on diagonal at " + x;
            case Point(var x, var y) when x > 0 && y > 0    -> "quadrant I (%d,%d)".formatted(x, y);
            case Point(var x, var y) when x < 0 && y > 0    -> "quadrant II (%d,%d)".formatted(x, y);
            case Point(var x, var y) when x < 0 && y < 0    -> "quadrant III (%d,%d)".formatted(x, y);
            case Point(var x, var y)                         -> "quadrant IV (%d,%d)".formatted(x, y);
        };
    }

    // --- Nested record patterns ---

    static String describeLine(Line line) {
        return switch (line) {
            case Line(Point(var x1, var y1), Point(var x2, var y2))
                    when x1 == x2 && y1 == y2 -> "zero-length line at (%d,%d)".formatted(x1, y1);
            case Line(Point(var x1, var _), Point(var x2, var _))
                    when x1 == x2 -> "vertical line at x=" + x1;
            case Line(Point(var _, var y1), Point(var _, var y2))
                    when y1 == y2 -> "horizontal line at y=" + y1;
            case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
                    "line from (%d,%d) to (%d,%d)".formatted(x1, y1, x2, y2);
        };
    }

    // --- Sealed type exhaustive switch ---

    static double evaluate(Expr expr) {
        return switch (expr) {
            case Literal(var value)        -> value;
            case Add(var left, var right)  -> evaluate(left) + evaluate(right);
            case Multiply(var l, var r)    -> evaluate(l) * evaluate(r);
            case Negate(var operand)        -> -evaluate(operand);
        };
    }

    // --- Event handling with pattern matching ---

    static String handleEvent(Event event) {
        return switch (event) {
            case ClickEvent(var x, var y, var btn) when btn == 1 ->
                    "Left click at (%d, %d)".formatted(x, y);
            case ClickEvent(var x, var y, var btn) when btn == 3 ->
                    "Right click at (%d, %d)".formatted(x, y);
            case ClickEvent(var x, var y, var btn) ->
                    "Button %d click at (%d, %d)".formatted(btn, x, y);
            case KeyEvent(var key, var ctrl, var shift) when ctrl && key.equals("S") ->
                    "Save shortcut (Ctrl+S)";
            case KeyEvent(var key, var ctrl, var shift) when ctrl && shift ->
                    "Ctrl+Shift+" + key;
            case KeyEvent(var key, _, _) ->
                    "Key pressed: " + key;
            case ScrollEvent(var delta) when delta > 0 ->
                    "Scroll up by " + delta;
            case ScrollEvent(var delta) ->
                    "Scroll down by " + Math.abs(delta);
        };
    }

    public static void main(String[] args) {
        // instanceof patterns
        System.out.println("=== instanceof patterns ===");
        instanceofPatterns("Hello World");
        instanceofPatterns(42);
        instanceofPatterns(null);

        System.out.println();

        // switch patterns
        System.out.println("=== switch patterns ===");
        System.out.println(describe(null));
        System.out.println(describe("  "));
        System.out.println(describe("Hello"));
        System.out.println(describe(-5));
        System.out.println(describe(42));
        System.out.println(describe(List.of()));
        System.out.println(describe(List.of(1, 2, 3)));
        System.out.println(describe(LocalDate.now()));

        System.out.println();

        // Record patterns
        System.out.println("=== record patterns ===");
        System.out.println(classifyPoint(new Point(0, 0)));
        System.out.println(classifyPoint(new Point(3, 3)));
        System.out.println(classifyPoint(new Point(5, 0)));
        System.out.println(classifyPoint(new Point(-2, 4)));

        System.out.println();

        // Nested record patterns
        System.out.println("=== nested record patterns ===");
        System.out.println(describeLine(new Line(new Point(0, 0), new Point(0, 0))));
        System.out.println(describeLine(new Line(new Point(3, 0), new Point(3, 10))));
        System.out.println(describeLine(new Line(new Point(0, 5), new Point(10, 5))));
        System.out.println(describeLine(new Line(new Point(1, 2), new Point(3, 4))));

        System.out.println();

        // Expression evaluation
        System.out.println("=== expression evaluation ===");
        // (3 + 4) * 2
        Expr expr = new Multiply(
                new Add(new Literal(3), new Literal(4)),
                new Literal(2)
        );
        System.out.println("(3 + 4) * 2 = " + evaluate(expr));

        // -(5 + 3)
        Expr negated = new Negate(new Add(new Literal(5), new Literal(3)));
        System.out.println("-(5 + 3) = " + evaluate(negated));

        System.out.println();

        // Event handling
        System.out.println("=== event handling ===");
        var events = List.of(
                new ClickEvent(100, 200, 1),
                new ClickEvent(50, 75, 3),
                new KeyEvent("S", true, false),
                new KeyEvent("F", true, true),
                new KeyEvent("A", false, false),
                new ScrollEvent(3),
                new ScrollEvent(-5)
        );
        events.forEach(e -> System.out.println(handleEvent(e)));
    }
}
