package com.cafeflow.workflows.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
        String title,
        @JsonProperty("url") String link,
        @JsonProperty("ups") int upvotes) {
}
