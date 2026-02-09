package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentGeneratorHelper extends BaseHelper {

    private final LLMClient llmClient;

    @Override
    protected String getServiceName() {
        return "content_generator";
    }

    /**
     * Generates content based on a free-form instruction.
     * This is the most flexible method — the caller controls the prompt entirely.
     */
    public String generate(String instruction) {
        return executeWithProtection("generate", () -> llmClient.send(instruction));
    }

    /**
     * Generates an email body from a topic and key points.
     */
    public String generateEmail(String topic, String recipientContext, List<String> keyPoints) {
        return executeWithProtection("generateEmail", () -> {
            String points = keyPoints.stream()
                    .map(p -> "- " + p)
                    .collect(Collectors.joining("\n"));
            String prompt = """
                    Write a professional email body about the following topic.
                    Do NOT include subject line, greeting, or signature — only the body paragraphs.

                    Topic: %s
                    Recipient context: %s
                    Key points to cover:
                    %s""".formatted(topic, recipientContext, points);
            return llmClient.send(prompt);
        });
    }

    /**
     * Generates a social media post for the specified platform.
     * Respects platform character limits and conventions.
     */
    public String generateSocialPost(String platform, String topic, String tone) {
        return executeWithProtection("generateSocialPost(" + platform + ")", () -> {
            String prompt = """
                    Write a %s post about the following topic.
                    Tone: %s
                    Follow %s conventions and best practices (hashtags, length, formatting).
                    Return ONLY the post text, ready to publish.

                    Topic: %s""".formatted(platform, tone, platform, topic);
            return llmClient.send(prompt);
        });
    }

    /**
     * Generates a tweet (max 280 characters) from the given content.
     */
    public String generateTweet(String content) {
        return executeWithProtection("generateTweet", () -> {
            String prompt = """
                    Create a tweet (maximum 280 characters) based on the following content.
                    Include relevant hashtags. Return ONLY the tweet text.

                    Content: %s""".formatted(content);
            String tweet = llmClient.send(prompt);
            // Enforce 280 char limit as safety net
            return tweet.length() > 280 ? tweet.substring(0, 277) + "..." : tweet;
        });
    }

    /**
     * Generates a report from structured data points.
     * Useful for turning raw data into human-readable narratives.
     */
    public String generateReport(String title, List<String> dataPoints) {
        return executeWithProtection("generateReport", () -> {
            String data = dataPoints.stream()
                    .map(d -> "- " + d)
                    .collect(Collectors.joining("\n"));
            String prompt = """
                    Write a concise executive report based on the following data.
                    Use clear sections with headers. Be factual and objective.

                    Report title: %s
                    Data points:
                    %s""".formatted(title, data);
            return llmClient.send(prompt);
        });
    }

    /**
     * Rewrites the given text in a different tone.
     * Tones: "formal", "casual", "professional", "friendly", "technical", etc.
     */
    public String rewriteInTone(String text, String tone) {
        return executeWithProtection("rewriteInTone(" + tone + ")", () -> {
            String prompt = """
                    Rewrite the following text in a %s tone.
                    Keep the same meaning but change the style. Return ONLY the rewritten text.

                    Text:
                    %s""".formatted(tone, text);
            return llmClient.send(prompt);
        });
    }
}
