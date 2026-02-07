package com.example.fundamentals;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Examples of Java 21 Sealed Classes and Interfaces
 *
 * Sealed types restrict which classes can implement/extend them,
 * enabling exhaustive pattern matching in switch expressions.
 */
public class SealedClassExamples {

    // --- Sealed interface for payment results ---

    public sealed interface PaymentResult
            permits PaymentSuccess, PaymentFailure, PaymentPending {
    }

    public record PaymentSuccess(String transactionId, BigDecimal amount)
            implements PaymentResult {}

    public record PaymentFailure(String errorCode, String message)
            implements PaymentResult {}

    public record PaymentPending(String pendingId, Duration estimatedTime)
            implements PaymentResult {}

    // --- Sealed interface for shapes (classic example) ---

    public sealed interface Shape permits Circle, Rectangle, Triangle {
        double area();
    }

    public record Circle(double radius) implements Shape {
        public Circle {
            if (radius <= 0) throw new IllegalArgumentException("radius must be positive");
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public record Rectangle(double width, double height) implements Shape {
        public Rectangle {
            if (width <= 0 || height <= 0)
                throw new IllegalArgumentException("dimensions must be positive");
        }

        @Override
        public double area() {
            return width * height;
        }
    }

    public record Triangle(double base, double height) implements Shape {
        public Triangle {
            if (base <= 0 || height <= 0)
                throw new IllegalArgumentException("dimensions must be positive");
        }

        @Override
        public double area() {
            return (base * height) / 2;
        }
    }

    // --- Sealed class hierarchy with abstract class ---

    public sealed abstract class Notification
            permits EmailNotification, SmsNotification, PushNotification {

        private final String recipient;
        private final String content;

        protected Notification(String recipient, String content) {
            this.recipient = recipient;
            this.content = content;
        }

        public String recipient() { return recipient; }
        public String content() { return content; }

        public abstract String send();
    }

    public static final class EmailNotification extends Notification {
        private final String subject;

        public EmailNotification(String recipient, String subject, String content) {
            super(recipient, content);
            this.subject = subject;
        }

        public String subject() { return subject; }

        @Override
        public String send() {
            return "Email sent to %s: [%s] %s".formatted(recipient(), subject, content());
        }
    }

    public static final class SmsNotification extends Notification {
        public SmsNotification(String phoneNumber, String content) {
            super(phoneNumber, content);
        }

        @Override
        public String send() {
            return "SMS sent to %s: %s".formatted(recipient(), content());
        }
    }

    public static final class PushNotification extends Notification {
        private final String deviceToken;

        public PushNotification(String deviceToken, String content) {
            super(deviceToken, content);
            this.deviceToken = deviceToken;
        }

        @Override
        public String send() {
            return "Push sent to device %s: %s".formatted(deviceToken, content());
        }
    }

    // --- Using sealed types with exhaustive switch ---

    static String describePayment(PaymentResult result) {
        return switch (result) {
            case PaymentSuccess s -> "Paid %s (tx: %s)".formatted(s.amount(), s.transactionId());
            case PaymentFailure f -> "Failed: %s (%s)".formatted(f.message(), f.errorCode());
            case PaymentPending p -> "Pending: %s (est. %s)".formatted(p.pendingId(), p.estimatedTime());
        };
    }

    static String describeShape(Shape shape) {
        return switch (shape) {
            case Circle c    -> "Circle with radius %.2f, area=%.2f".formatted(c.radius(), c.area());
            case Rectangle r -> "Rectangle %sx%s, area=%.2f".formatted(r.width(), r.height(), r.area());
            case Triangle t  -> "Triangle base=%.2f h=%.2f, area=%.2f".formatted(t.base(), t.height(), t.area());
        };
    }

    public static void main(String[] args) {
        // Payment results
        var success = new PaymentSuccess("TX-001", new BigDecimal("99.90"));
        var failure = new PaymentFailure("DECLINED", "Insufficient funds");
        var pending = new PaymentPending("PND-001", Duration.ofMinutes(5));

        System.out.println(describePayment(success));
        System.out.println(describePayment(failure));
        System.out.println(describePayment(pending));

        System.out.println();

        // Shapes
        var shapes = java.util.List.of(
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 8)
        );

        shapes.forEach(s -> System.out.println(describeShape(s)));

        System.out.println();

        // Notifications
        var notifications = java.util.List.of(
                new EmailNotification("user@example.com", "Welcome!", "Thanks for signing up."),
                new SmsNotification("+5511999999999", "Your code is 1234"),
                new PushNotification("device-abc-123", "New message received")
        );

        notifications.forEach(n -> System.out.println(n.send()));
    }
}
