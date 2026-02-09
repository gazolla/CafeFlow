package com.cafeflow.helpers.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractionResult(
        Map<String, String> fields,
        List<String> entities,
        double confidence) {
}
