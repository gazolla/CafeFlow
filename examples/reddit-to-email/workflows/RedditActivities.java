package com.cafeflow.workflows.reddit;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.List;

@ActivityInterface
public interface RedditActivities {

    @ActivityMethod
    List<RedditPost> fetchTopPosts(String subreddit, int limit);

    @ActivityMethod
    void sendEmail(List<RedditPost> posts);
}
