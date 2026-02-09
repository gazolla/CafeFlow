package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SentimentAnalyzerHelper extends BaseHelper {

    private final LLMClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SENTIMENT_PROMPT = """
            Analyze the sentiment of the following text.
            Respond ONLY with a valid JSON object in this exact format, nothing else:
            {"sentiment": "positive", "confidence": 0.95, "explanation": "Brief reason"}

            The "sentiment" field must be one of: "positive", "negative", or "neutral".
            The "confidence" field must be a number between 0.0 and 1.0.
            The "explanation" field must be a brief one-sentence explanation.

            Text to analyze:
            %s""";

    @Override
    protected String getServiceName() {
        return "sentiment_analyzer";
    }

    /**
     * Analyzes the sentiment of the given text.
     * Returns a SentimentResult with sentiment, confidence and explanation.
     */
    public SentimentResult analyze(String text) {
        return executeWithProtection("analyze", () -> {
            String prompt = SENTIMENT_PROMPT.formatted(text);
            String response = llmClient.send(prompt);
            return parseSentimentResponse(response);
        });
    }

    /**
     * Analyzes sentiment for each text in the list.
     * Returns a list of SentimentResult in the same order.
     */
    public List<SentimentResult> analyzeBatch(List<String> texts) {
        return executeWithProtection("analyzeBatch", () -> {
            List<SentimentResult> results = new ArrayList<>();
            for (String text : texts) {
                String prompt = SENTIMENT_PROMPT.formatted(text);
                String response = llmClient.send(prompt);
                results.add(parseSentimentResponse(response));
            }
            return results;
        });
    }

    /**
     * Simplified sentiment classification.
     * Returns only the sentiment label: "positive", "negative", or "neutral".
     */
    public String classifySimple(String text) {
        return executeWithProtection("classifySimple", () -> {
            String prompt = SENTIMENT_PROMPT.formatted(text);
            String response = llmClient.send(prompt);
            return parseSentimentResponse(response).sentiment();
        });
    }

    private SentimentResult parseSentimentResponse(String response) {
        try {
            // Try to extract JSON from the response (LLM might add extra text)
            String json = extractJson(response);
            return objectMapper.readValue(json, SentimentResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse LLM sentiment response: {}. Raw response: {}", e.getMessage(), response);
            return new SentimentResult("unknown", 0.0, "Failed to parse LLM response");
        }
    }

    private String extractJson(String response) {
        // Find the first '{' and last '}' to extract JSON even if LLM adds extra text
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
