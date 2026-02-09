package com.cafeflow.helpers.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SentimentResult(
        String sentiment,
        double confidence,
        String explanation) {
}
