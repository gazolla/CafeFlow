package com.cafeflow.workflows.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
        String title,
        @JsonProperty("selftext") String selfText,
        @JsonProperty("url") String link,
        int ups,
        String author) {}
