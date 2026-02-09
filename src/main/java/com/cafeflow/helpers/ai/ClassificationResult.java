package com.cafeflow.helpers.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClassificationResult(
        String category,
        double confidence,
        String reasoning) {
}
