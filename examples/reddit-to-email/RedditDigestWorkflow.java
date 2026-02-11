package com.cafeflow.workflows.reddit;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface RedditDigestWorkflow {
    @WorkflowMethod
    void executeDigest(String subreddit, String targetEmail);
}
