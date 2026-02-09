package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicExtractorHelper extends BaseHelper {

    private final LLMClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String getServiceName() {
        return "topic_extractor";
    }

    /**
     * Extracts the main topics/themes from the given text.
     * Returns a list of topic strings (e.g., ["machine learning", "data privacy", "cloud computing"]).
     */
    public List<String> extractTopics(String text) {
        return executeWithProtection("extractTopics", () -> {
            String prompt = """
                    Extract the main topics and themes from the following text.
                    Return ONLY a JSON array of short topic strings (2-4 words each), nothing else.
                    Return at most 5 topics, ordered by relevance.
                    Example: ["machine learning", "data privacy", "cloud computing"]

                    Text:
                    %s""".formatted(text);

            String response = llmClient.send(prompt);
            return parseTopicList(response);
        });
    }

    /**
     * Extracts topics from each text in the list.
     * Returns a list of topic lists in the same order.
     */
    public List<List<String>> extractTopicsBatch(List<String> texts) {
        return executeWithProtection("extractTopicsBatch", () -> {
            List<List<String>> allTopics = new ArrayList<>();
            for (String text : texts) {
                String prompt = """
                        Extract the main topics from the following text.
                        Return ONLY a JSON array of short topic strings (2-4 words each).
                        Return at most 5 topics.

                        Text:
                        %s""".formatted(text);

                String response = llmClient.send(prompt);
                allTopics.add(parseTopicList(response));
            }
            return allTopics;
        });
    }

    /**
     * Generates relevant hashtags from the given text.
     * Returns a list of hashtags (e.g., ["#AI", "#MachineLearning", "#Tech"]).
     */
    public List<String> generateHashtags(String text, int maxHashtags) {
        return executeWithProtection("generateHashtags", () -> {
            String prompt = """
                    Generate up to %d relevant hashtags for the following text.
                    Return ONLY a JSON array of hashtag strings (with # prefix), nothing else.
                    Example: ["#AI", "#MachineLearning", "#Tech"]

                    Text:
                    %s""".formatted(maxHashtags, text);

            String response = llmClient.send(prompt);
            return parseTopicList(response);
        });
    }

    /**
     * Generates a list of keywords for SEO/search from the given text.
     * Returns plain keywords without # prefix.
     */
    public List<String> extractKeywords(String text, int maxKeywords) {
        return executeWithProtection("extractKeywords", () -> {
            String prompt = """
                    Extract up to %d important keywords from the following text.
                    Keywords should be relevant for search and categorization.
                    Return ONLY a JSON array of keyword strings, nothing else.
                    Example: ["artificial intelligence", "neural network", "deep learning"]

                    Text:
                    %s""".formatted(maxKeywords, text);

            String response = llmClient.send(prompt);
            return parseTopicList(response);
        });
    }

    /**
     * Generates a single-line topic label for the given text.
     * Useful for auto-tagging or categorizing content.
     */
    public String generateTopicLabel(String text) {
        return executeWithProtection("generateTopicLabel", () -> {
            String prompt = """
                    Generate a single short topic label (2-5 words) that best describes the following text.
                    Return ONLY the label, nothing else.

                    Text:
                    %s""".formatted(text);

            return llmClient.send(prompt).trim().replaceAll("\"", "");
        });
    }

    @SuppressWarnings("unchecked")
    private List<String> parseTopicList(String response) {
        try {
            String json = extractJsonArray(response);
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            log.warn("Failed to parse topic list: {}. Raw: {}", e.getMessage(), response);
            return Collections.emptyList();
        }
    }

    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
