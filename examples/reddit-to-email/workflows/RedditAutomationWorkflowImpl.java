package com.cafeflow.workflows.reddit;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

public class RedditAutomationWorkflowImpl implements RedditAutomationWorkflow {

    private final RedditActivities activities = Workflow.newActivityStub(
            RedditActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build());

    @Override
    public void run() {
        List<RedditPost> posts = activities.fetchTopPosts("GoogleAntigravityIDE", 5);
        if (!posts.isEmpty()) {
            activities.sendEmail(posts);
        }
    }
}
