package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextClassifierHelper extends BaseHelper {

    private final LLMClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String getServiceName() {
        return "text_classifier";
    }

    /**
     * Classifies the given text into one of the provided categories.
     * Returns a ClassificationResult with the chosen category, confidence and reasoning.
     */
    public ClassificationResult classify(String text, List<String> categories) {
        return executeWithProtection("classify", () -> {
            String cats = categories.stream()
                    .map(c -> "\"" + c + "\"")
                    .collect(Collectors.joining(", "));

            String prompt = """
                    Classify the following text into exactly ONE of these categories: [%s]
                    Respond ONLY with a valid JSON object in this exact format, nothing else:
                    {"category": "chosen_category", "confidence": 0.95, "reasoning": "Brief explanation"}

                    The "category" MUST be one of the provided categories exactly as written.
                    The "confidence" must be a number between 0.0 and 1.0.
                    The "reasoning" must be a brief one-sentence explanation.

                    Text:
                    %s""".formatted(cats, text);

            String response = llmClient.send(prompt);
            return parseClassificationResponse(response);
        });
    }

    /**
     * Classifies each text in the list into the provided categories.
     * Returns a list of ClassificationResult in the same order.
     */
    public List<ClassificationResult> classifyBatch(List<String> texts, List<String> categories) {
        return executeWithProtection("classifyBatch", () -> {
            String cats = categories.stream()
                    .map(c -> "\"" + c + "\"")
                    .collect(Collectors.joining(", "));

            List<ClassificationResult> results = new ArrayList<>();
            for (String text : texts) {
                String prompt = """
                        Classify the following text into exactly ONE of these categories: [%s]
                        Respond ONLY with a valid JSON object:
                        {"category": "chosen_category", "confidence": 0.95, "reasoning": "Brief explanation"}

                        Text:
                        %s""".formatted(cats, text);

                String response = llmClient.send(prompt);
                results.add(parseClassificationResponse(response));
            }
            return results;
        });
    }

    /**
     * Simple classification that returns only the category label.
     */
    public String classifySimple(String text, List<String> categories) {
        return executeWithProtection("classifySimple", () -> {
            ClassificationResult result = classify(text, categories);
            return result.category();
        });
    }

    /**
     * Binary classification: answers a yes/no question about the text.
     * Returns true if the LLM answers yes, false otherwise.
     */
    public boolean classifyBoolean(String text, String question) {
        return executeWithProtection("classifyBoolean", () -> {
            String prompt = """
                    Based on the following text, answer this question with ONLY "yes" or "no":
                    %s

                    Text:
                    %s""".formatted(question, text);

            String response = llmClient.send(prompt).trim().toLowerCase();
            return response.contains("yes");
        });
    }

    /**
     * Checks if the text matches a given criteria / filter condition.
     * Useful for filtering content in workflows.
     */
    public boolean matchesCriteria(String text, String criteria) {
        return executeWithProtection("matchesCriteria", () -> {
            String prompt = """
                    Does the following text match this criteria? Answer ONLY "yes" or "no".
                    Criteria: %s

                    Text:
                    %s""".formatted(criteria, text);

            String response = llmClient.send(prompt).trim().toLowerCase();
            return response.contains("yes");
        });
    }

    private ClassificationResult parseClassificationResponse(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, ClassificationResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse classification response: {}. Raw: {}", e.getMessage(), response);
            return new ClassificationResult("unknown", 0.0, "Failed to parse LLM response");
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
