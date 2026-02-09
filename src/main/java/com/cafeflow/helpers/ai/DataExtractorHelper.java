package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataExtractorHelper extends BaseHelper {

    private final LLMClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String getServiceName() {
        return "data_extractor";
    }

    /**
     * Extracts specific fields from unstructured text.
     * The caller provides the field names to extract (e.g., "name", "date",
     * "amount").
     * Returns an ExtractionResult with the extracted field values.
     */
    public ExtractionResult extractFields(String text, List<String> fieldNames) {
        return executeWithProtection("extractFields", () -> {
            String fields = fieldNames.stream()
                    .map(f -> "\"" + f + "\": \"extracted value or empty string\"")
                    .collect(Collectors.joining(", "));

            String prompt = """
                    Extract the following fields from the text below.
                    Respond ONLY with a valid JSON object in this exact format, nothing else:
                    {"fields": {%s}, "entities": ["list", "of", "named", "entities", "found"], "confidence": 0.95}

                    The "fields" object must contain all requested fields. Use empty string if not found.
                    The "entities" array should list proper nouns, names, organizations, dates found.
                    The "confidence" is a number between 0.0 and 1.0.

                    Text:
                    %s""".formatted(fields, text);

            String response = llmClient.send(prompt);
            return parseExtractionResponse(response);
        });
    }

    /**
     * Extracts named entities (people, organizations, locations, dates) from text.
     * Returns a simple list of entity strings.
     */
    public List<String> extractEntities(String text) {
        return executeWithProtection("extractEntities", () -> {
            String prompt = """
                    Extract all named entities from the following text.
                    Named entities include: people names, organization names, locations, dates, monetary values.
                    Respond ONLY with a JSON array of strings, nothing else.
                    Example: ["John Doe", "Google", "New York", "January 2024", "$5000"]

                    Text:
                    %s""".formatted(text);

            String response = llmClient.send(prompt);
            return parseEntityList(response);
        });
    }

    /**
     * Extracts key-value pairs from unstructured text.
     * Useful for parsing invoices, forms, receipts, etc.
     * Returns a Map of detected field names to their values.
     */
    public Map<String, String> extractKeyValues(String text) {
        return executeWithProtection("extractKeyValues", () -> {
            String prompt = """
                    Extract all key-value pairs from the following text.
                    Identify any structured data like names, dates, amounts, IDs, addresses, etc.
                    Respond ONLY with a valid JSON object mapping field names to values, nothing else.
                    Example: {"name": "John", "date": "2024-01-15", "amount": "$500"}

                    Text:
                    %s""".formatted(text);

            String response = llmClient.send(prompt);
            return parseKeyValues(response);
        });
    }

    /**
     * Extracts action items / tasks from a text (meeting notes, emails, etc.).
     * Returns a list of action item strings.
     */
    public List<String> extractActionItems(String text) {
        return executeWithProtection("extractActionItems", () -> {
            String prompt = """
                    Extract all action items, tasks, and to-dos from the following text.
                    An action item is something someone needs to do.
                    Respond ONLY with a JSON array of strings, each being one action item.
                    Example: ["Review the proposal by Friday", "Send invoice to client", "Schedule follow-up meeting"]

                    Text:
                    %s""".formatted(text);

            String response = llmClient.send(prompt);
            return parseEntityList(response);
        });
    }

    private ExtractionResult parseExtractionResponse(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, ExtractionResult.class);
        } catch (Exception e) {
            log.warn("Failed to parse extraction response: {}. Raw: {}", e.getMessage(), response);
            return new ExtractionResult(Collections.emptyMap(), Collections.emptyList(), 0.0);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseEntityList(String response) {
        try {
            String json = extractJsonArray(response);
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            log.warn("Failed to parse entity list: {}. Raw: {}", e.getMessage(), response);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseKeyValues(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse key-values: {}. Raw: {}", e.getMessage(), response);
            return Collections.emptyMap();
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

    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
