package com.cafeflow.workflows.reddit;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(workers = "REDDIT_DIGEST_WORKER")
public class RedditDigestWorkflowImpl implements RedditDigestWorkflow {

    private static final Logger log = Workflow.getLogger(RedditDigestWorkflowImpl.class);

    private final RedditDigestActivities activities = Workflow.newActivityStub(
            RedditDigestActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(5))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .build())
                    .build()
    );

    @Override
    public void executeDigest(String subreddit, String targetEmail) {
        log.info("Starting Reddit digest for r/{}", subreddit);

        // 1. Fetch top posts
        List<RedditPost> posts = activities.fetchTopPosts(subreddit, 5);

        if (posts == null || posts.isEmpty()) {
            log.warn("No posts found for r/{}", subreddit);
            return;
        }

        // 2. Summarize posts using AI
        String digest = activities.summarizePosts(posts);

        // 3. Send digest via email
        activities.sendDigestEmail(targetEmail, digest);

        log.info("Reddit digest sent to {}", targetEmail);
    }
}
