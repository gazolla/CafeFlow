package com.cafeflow.helpers.marketing;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class TwitterHelper extends BaseHelper {

    private final HttpClient httpClient;

    @Value("${twitter.api.bearer-token:}")
    private String bearerToken;

    public TwitterHelper() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Override
    protected String getServiceName() {
        return "twitter";
    }

    /**
     * Looks up a user by their username.
     * GET /2/users/by/username/:username
     */
    public String getUserByUsername(String username) {
        return executeWithProtection("getUserByUsername", () -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.x.com/2/users/by/username/" + username))
                    .header("Authorization", "Bearer " + bearerToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("X API error: " + response.statusCode() + " - " + response.body());
            }
            return response.body();
        });
    }

    /**
     * Posts a new tweet.
     * POST /2/tweets
     */
    public String postTweet(String text) {
        return executeWithProtection("postTweet", () -> {
            String jsonBody = "{\"text\": \"" + text + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.x.com/2/tweets"))
                    .header("Authorization", "Bearer " + bearerToken) // Note: Posting usually requires OAuth 1.0a or
                                                                      // 2.0 User Context
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                throw new RuntimeException("X API error: " + response.statusCode() + " - " + response.body());
            }
            return response.body();
        });
    }
}
