package com.cafeflow.workflows.reddit;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

@ActivityInterface
public interface RedditDigestActivities {
    @ActivityMethod
    List<RedditPost> fetchTopPosts(String subreddit, int limit);

    @ActivityMethod
    String summarizePosts(List<RedditPost> posts);

    @ActivityMethod
    void sendDigestEmail(String email, String content);
}
