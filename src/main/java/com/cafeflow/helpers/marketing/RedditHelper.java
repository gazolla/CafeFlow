package com.cafeflow.helpers.marketing;

import com.cafeflow.core.base.BaseHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RedditHelper extends BaseHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    protected String getServiceName() {
        return "reddit";
    }

    public <T> List<T> fetchTopPosts(String subreddit, int limit, Class<T> responseType) {
        String url = String.format("https://www.reddit.com/r/%s/top/.json?limit=%d", subreddit, limit);

        return executeWithProtection("fetchTopPosts", () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "CafeFlow/1.0 (Java 17; com.cafeflow)")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Reddit API returned status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode children = root.path("data").path("children");

            List<T> posts = new ArrayList<>();
            for (JsonNode child : children) {
                JsonNode data = child.path("data");
                T post = objectMapper.convertValue(data, responseType);
                posts.add(post);
            }
            return posts;
        });
    }
}
