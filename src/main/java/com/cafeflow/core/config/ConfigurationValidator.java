package com.cafeflow.core.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates that all required configuration is present for active helpers.
 * Runs on application startup and prints a clear configuration report.
 *
 * <p>This component scans the Spring ApplicationContext for known helper beans
 * and checks whether their required environment variables are configured.
 * The report helps users quickly identify what needs to be set up.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigurationValidator {

    private final ApplicationContext applicationContext;
    private final Environment environment;

    // --- Configuration Registry ---
    // Maps helper class simple name → list of required config entries.
    // Each entry has: env var name, Spring property path, and whether it's required.

    private static final List<HelperConfigSpec> HELPER_SPECS = List.of(
            // No config needed
            new HelperConfigSpec("RedditHelper",
                    "com.cafeflow.helpers.marketing.RedditHelper",
                    List.of(),
                    "Reddit public API — no credentials needed"),

            // Email
            new HelperConfigSpec("EmailHelper",
                    "com.cafeflow.helpers.communication.EmailHelper",
                    List.of(
                            new RequiredVar("SMTP_USERNAME", "spring.mail.username"),
                            new RequiredVar("SMTP_PASSWORD", "spring.mail.password")
                    ),
                    "SMTP email sending"),

            // Telegram
            new HelperConfigSpec("TelegramHelper",
                    "com.cafeflow.helpers.communication.TelegramHelper",
                    List.of(
                            new RequiredVar("TELEGRAM_BOT_TOKEN", "telegram.bot.token")
                    ),
                    "Telegram Bot API"),

            // Twitter
            new HelperConfigSpec("TwitterHelper",
                    "com.cafeflow.helpers.marketing.TwitterHelper",
                    List.of(
                            new RequiredVar("X_BEARER_TOKEN", "twitter.api.bearer-token")
                    ),
                    "X/Twitter API v2"),

            // Google Drive
            new HelperConfigSpec("GDriveHelper",
                    "com.cafeflow.helpers.office.google.GDriveHelper",
                    List.of(
                            new RequiredVar("GD_CREDENTIALS_PATH", "google.drive.credentials-path")
                    ),
                    "Google Drive API"),

            // AI Helpers — all share LLM config
            aiHelperSpec("TextSummarizerHelper",
                    "com.cafeflow.helpers.ai.TextSummarizerHelper",
                    "LLM-powered text summarization"),
            aiHelperSpec("SentimentAnalyzerHelper",
                    "com.cafeflow.helpers.ai.SentimentAnalyzerHelper",
                    "LLM-powered sentiment analysis"),
            aiHelperSpec("TextTranslatorHelper",
                    "com.cafeflow.helpers.ai.TextTranslatorHelper",
                    "LLM-powered text translation"),
            aiHelperSpec("ContentGeneratorHelper",
                    "com.cafeflow.helpers.ai.ContentGeneratorHelper",
                    "LLM-powered content generation"),
            aiHelperSpec("DataExtractorHelper",
                    "com.cafeflow.helpers.ai.DataExtractorHelper",
                    "LLM-powered data extraction"),
            aiHelperSpec("TextClassifierHelper",
                    "com.cafeflow.helpers.ai.TextClassifierHelper",
                    "LLM-powered text classification"),
            aiHelperSpec("TopicExtractorHelper",
                    "com.cafeflow.helpers.ai.TopicExtractorHelper",
                    "LLM-powered topic extraction")
    );

    private static HelperConfigSpec aiHelperSpec(String name, String className, String description) {
        return new HelperConfigSpec(name, className, List.of(
                new RequiredVar("GEMINI_API_KEY|GROQ_API_KEY", "llm.gemini.api-key|llm.groq.api-key")
        ), description);
    }

    @PostConstruct
    public void validate() {
        Map<String, HelperStatus> report = new LinkedHashMap<>();
        boolean hasWarnings = false;

        for (HelperConfigSpec spec : HELPER_SPECS) {
            boolean beanExists = isBeanPresent(spec.className);

            if (!beanExists) {
                report.put(spec.name, new HelperStatus(Status.INACTIVE, spec.description, List.of()));
                continue;
            }

            if (spec.requiredVars.isEmpty()) {
                report.put(spec.name, new HelperStatus(Status.READY, spec.description, List.of()));
                continue;
            }

            List<String> missing = findMissingVars(spec.requiredVars);

            if (missing.isEmpty()) {
                report.put(spec.name, new HelperStatus(Status.READY, spec.description, List.of()));
            } else {
                report.put(spec.name, new HelperStatus(Status.MISSING_CONFIG, spec.description, missing));
                hasWarnings = true;
            }
        }

        printReport(report, hasWarnings);
    }

    private boolean isBeanPresent(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return !applicationContext.getBeansOfType(clazz).isEmpty();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if required vars are configured. Supports OR-logic with pipe separator.
     * For example, "GEMINI_API_KEY|GROQ_API_KEY" means at least one must be set.
     */
    private List<String> findMissingVars(List<RequiredVar> requiredVars) {
        List<String> missing = new ArrayList<>();

        for (RequiredVar rv : requiredVars) {
            if (rv.envVar.contains("|")) {
                // OR logic: at least one must be configured
                String[] envVars = rv.envVar.split("\\|");
                String[] properties = rv.property.split("\\|");
                boolean anyConfigured = false;

                for (int i = 0; i < envVars.length; i++) {
                    if (isConfigured(envVars[i].trim(), properties[i].trim())) {
                        anyConfigured = true;
                        break;
                    }
                }

                if (!anyConfigured) {
                    missing.add(rv.envVar);
                }
            } else {
                // Single var: must be configured
                if (!isConfigured(rv.envVar, rv.property)) {
                    missing.add(rv.envVar);
                }
            }
        }

        return missing;
    }

    private boolean isConfigured(String envVar, String property) {
        // Check environment variable directly
        String envValue = System.getenv(envVar);
        if (envValue != null && !envValue.isBlank()) {
            return true;
        }

        // Check Spring property (which may resolve from env var or application.yml)
        String propValue = environment.getProperty(property);
        return propValue != null && !propValue.isBlank();
    }

    private void printReport(Map<String, HelperStatus> report, boolean hasWarnings) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              CafeFlow Configuration Report                      ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════╣\n");

        for (Map.Entry<String, HelperStatus> entry : report.entrySet()) {
            String name = entry.getKey();
            HelperStatus status = entry.getValue();

            String icon = switch (status.status) {
                case READY -> "✅";
                case MISSING_CONFIG -> "⚠️";
                case INACTIVE -> "⬚ ";
            };

            String statusText = switch (status.status) {
                case READY -> "ready";
                case MISSING_CONFIG -> "MISSING: " + String.join(" or ", status.missingVars);
                case INACTIVE -> "not active";
            };

            String line = String.format("║ %s %-28s — %-30s ║", icon, name, statusText);
            sb.append(line).append("\n");
        }

        sb.append("╚══════════════════════════════════════════════════════════════════╝");

        if (hasWarnings) {
            sb.append("\n  💡 Tip: Copy .env.example to .env and configure the missing variables.");
            sb.append("\n          Only configure what your workflow needs.");
        }

        log.info(sb.toString());
    }

    // --- Internal Records ---

    private record HelperConfigSpec(
            String name,
            String className,
            List<RequiredVar> requiredVars,
            String description) {}

    private record RequiredVar(
            String envVar,
            String property) {}

    private record HelperStatus(
            Status status,
            String description,
            List<String> missingVars) {}

    private enum Status {
        READY, MISSING_CONFIG, INACTIVE
    }
}
