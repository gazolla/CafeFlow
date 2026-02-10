package com.example.fundamentals;

import java.util.List;

/**
 * Examples of Java 21 Text Blocks
 *
 * Text blocks use triple-quote syntax for multi-line strings
 * with automatic indentation management.
 */
public class TextBlockExamples {

    // --- JSON text blocks ---

    static String userJson(String name, String email) {
        return """
                {
                    "name": "%s",
                    "email": "%s",
                    "active": true
                }
                """.formatted(name, email);
    }

    static String userListJson(List<String> names) {
        var entries = names.stream()
                .map(n -> "        \"%s\"".formatted(n))
                .toList();
        return """
                {
                    "users": [
                %s
                    ]
                }
                """.formatted(String.join(",\n", entries));
    }

    // --- SQL text blocks ---

    static String findActiveUsersQuery() {
        return """
                SELECT u.id, u.name, u.email
                FROM users u
                WHERE u.active = true
                  AND u.created_at > :since
                ORDER BY u.name
                """;
    }

    static String insertUserQuery() {
        return """
                INSERT INTO users (id, name, email, created_at)
                VALUES (:id, :name, :email, :createdAt)
                ON CONFLICT (email) DO UPDATE
                SET name = EXCLUDED.name,
                    updated_at = NOW()
                """;
    }

    static String complexQuery(boolean includeInactive) {
        return """
                SELECT o.id, o.total, c.name AS customer_name
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE o.created_at >= :startDate
                  AND o.created_at <= :endDate
                  %s
                ORDER BY o.total DESC
                LIMIT :limit
                """.formatted(includeInactive ? "" : "AND c.active = true");
    }

    // --- HTML text blocks ---

    static String htmlPage(String title, String body) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                </head>
                <body>
                    %s
                </body>
                </html>
                """.formatted(title, body);
    }

    static String emailTemplate(String userName, String actionUrl) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px;">
                    <h1>Welcome, %s!</h1>
                    <p>Thank you for signing up. Please confirm your email:</p>
                    <a href="%s"
                       style="background: #007bff; color: white; padding: 10px 20px;
                              text-decoration: none; border-radius: 4px;">
                        Confirm Email
                    </a>
                </div>
                """.formatted(userName, actionUrl);
    }

    // --- YAML / Config text blocks ---

    static String dockerCompose(String appName, int port) {
        return """
                version: "3.8"
                services:
                  %s:
                    build: .
                    ports:
                      - "%d:%d"
                    environment:
                      - SPRING_PROFILES_ACTIVE=docker
                    depends_on:
                      - postgres
                  postgres:
                    image: postgres:16-alpine
                    environment:
                      POSTGRES_DB: %s
                      POSTGRES_USER: app
                      POSTGRES_PASSWORD: secret
                    ports:
                      - "5432:5432"
                """.formatted(appName, port, port, appName);
    }

    // --- Text block with special characters ---

    static void specialCharacters() {
        // Escaping in text blocks
        String withQuotes = """
                He said "hello" and she said "hi".
                No need to escape double quotes in text blocks.
                """;

        // Triple quotes inside text blocks need escaping
        String withTripleQuotes = """
                This is a text block.
                To include \""" you need to escape at least one quote.
                """;

        // Trailing whitespace with \s (space fence)
        String preserveTrailing = """
                Name:    \s
                Email:   \s
                Phone:   \s
                """;

        // Line continuation with backslash
        String singleLine = """
                This is a very long line that we want to \
                keep as a single line in the output \
                without any line breaks.""";

        System.out.println("With quotes:");
        System.out.println(withQuotes);
        System.out.println("With triple quotes:");
        System.out.println(withTripleQuotes);
        System.out.println("Preserved trailing:");
        System.out.println(preserveTrailing);
        System.out.println("Single line:");
        System.out.println(singleLine);
    }

    // --- Indentation control ---

    static void indentationControl() {
        // Closing """ position controls indentation stripping
        String noIndent = """
Left aligned
No indentation
""";

        String indented = """
                Indented by common prefix.
                All lines share the same base indent.
                    This line has extra indent.
                """;

        // String.indent() and String.stripIndent()
        String adjusted = "Hello\n  World\n    !".indent(4);

        System.out.println("No indent:");
        System.out.println(noIndent);
        System.out.println("Indented:");
        System.out.println(indented);
        System.out.println("Adjusted (+4):");
        System.out.println(adjusted);
    }

    public static void main(String[] args) {
        System.out.println("=== JSON ===");
        System.out.println(userJson("Alice", "alice@example.com"));
        System.out.println(userListJson(List.of("Alice", "Bob", "Carlos")));

        System.out.println("=== SQL ===");
        System.out.println(findActiveUsersQuery());
        System.out.println(insertUserQuery());
        System.out.println(complexQuery(false));

        System.out.println("=== HTML ===");
        System.out.println(emailTemplate("Alice", "https://example.com/confirm?token=abc123"));

        System.out.println("=== Docker Compose ===");
        System.out.println(dockerCompose("myapp", 8080));

        System.out.println("=== Special Characters ===");
        specialCharacters();

        System.out.println("=== Indentation ===");
        indentationControl();
    }
}
