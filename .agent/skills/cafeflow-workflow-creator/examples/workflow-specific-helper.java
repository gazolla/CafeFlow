// Example: Workflow-Specific Helper
// User: "Create workflow that posts top Reddit posts to Twitter"

// ANALYSIS:
// - Reddit: Framework helper exists (RedditHelper) - USE IT
// - Twitter: No helper exists, only this workflow needs it - CREATE WORKFLOW HELPER

// ============================================================================
// File 1: TwitterHelper.java (WORKFLOW-SPECIFIC)
// Location: src/main/java/com/cafeflow/workflows/social/helpers/TwitterHelper.java
// ============================================================================

package com.cafeflow.workflows.social.helpers;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Workflow-specific helper for Twitter API.
 * Only used by social media workflow.
 */
@Slf4j
@Component
public class TwitterHelper extends BaseHelper {

    private final Twitter twitter;

    public TwitterHelper() {
        // Configuration via environment variables
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey(System.getenv("TWITTER_CONSUMER_KEY"))
          .setOAuthConsumerSecret(System.getenv("TWITTER_CONSUMER_SECRET"))
          .setOAuthAccessToken(System.getenv("TWITTER_ACCESS_TOKEN"))
          .setOAuthAccessTokenSecret(System.getenv("TWITTER_ACCESS_SECRET"));
        
        TwitterFactory tf = new TwitterFactory(cb.build());
        this.twitter = tf.getInstance();
    }

    @Override
    protected String getServiceName() {
        return "twitter";
    }

    /**
     * Post a tweet to Twitter.
     * 
     * @param message Tweet text (max 280 characters)
     * @return Tweet ID
     */
    public long postTweet(String message) {
        return executeWithProtection("postTweet", () -> {
            if (message.length() > 280) {
                message = message.substring(0, 277) + "...";
            }
            
            Status status = twitter.updateStatus(message);
            return status.getId();
        });
    }

    /**
     * Post a tweet with media attachment.
     * 
     * @param message Tweet text
     * @param imageUrl URL of image to attach
     * @return Tweet ID
     */
    public long postTweetWithImage(String message, String imageUrl) {
        return executeWithProtection("postTweetWithImage", () -> {
            // Implementation for tweet with image
            Status status = twitter.updateStatus(message);
            return status.getId();
        });
    }
}

// ============================================================================
// File 2: RedditPost.java (Domain Model)
// Location: src/main/java/com/cafeflow/workflows/social/RedditPost.java
// ============================================================================

package com.cafeflow.workflows.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
    String title,
    @JsonProperty("url") String link,
    @JsonProperty("ups") int upvotes,
    String author
) {}

// ============================================================================
// File 3: SocialMediaWorkflow.java (Workflow Interface)
// Location: src/main/java/com/cafeflow/workflows/social/SocialMediaWorkflow.java
// ============================================================================

package com.cafeflow.workflows.social;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SocialMediaWorkflow {
    @WorkflowMethod
    void postRedditToTwitter();
}

// ============================================================================
// File 4: SocialMediaActivities.java (Activities Interface)
// Location: src/main/java/com/cafeflow/workflows/social/SocialMediaActivities.java
// ============================================================================

package com.cafeflow.workflows.social;

import io.temporal.activity.ActivityInterface;
import java.util.List;

@ActivityInterface
public interface SocialMediaActivities {
    List<RedditPost> fetchTopPosts(String subreddit, int limit);
    void postToTwitter(List<RedditPost> posts);
}

// ============================================================================
// File 5: SocialMediaActivitiesImpl.java (Activities Implementation)
// Location: src/main/java/com/cafeflow/workflows/social/SocialMediaActivitiesImpl.java
// ============================================================================

package com.cafeflow.workflows.social;

import com.cafeflow.helpers.marketing.RedditHelper;  // Framework helper
import com.cafeflow.workflows.social.helpers.TwitterHelper;  // Workflow helper
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ActivityImpl(workers = "SOCIAL_WORKER")
@RequiredArgsConstructor
public class SocialMediaActivitiesImpl implements SocialMediaActivities {

    // Framework helper (reusable)
    private final RedditHelper redditHelper;
    
    // Workflow-specific helper
    private final TwitterHelper twitterHelper;

    @Override
    public List<RedditPost> fetchTopPosts(String subreddit, int limit) {
        log.info("Fetching top {} posts from r/{}", limit, subreddit);
        return redditHelper.fetchTopPosts(subreddit, limit, RedditPost.class);
    }

    @Override
    public void postToTwitter(List<RedditPost> posts) {
        log.info("Posting {} posts to Twitter", posts.size());
        String tweet = formatTweet(posts);
        long tweetId = twitterHelper.postTweet(tweet);
        log.info("Posted tweet with ID: {}", tweetId);
    }

    private String formatTweet(List<RedditPost> posts) {
        StringBuilder tweet = new StringBuilder();
        tweet.append("🔥 Top posts from r/programming today:\n\n");
        
        for (int i = 0; i < Math.min(3, posts.size()); i++) {
            RedditPost post = posts.get(i);
            tweet.append(String.format("%d. %s\n", i + 1, post.title()));
        }
        
        tweet.append("\n#programming #dev");
        
        return tweet.toString();
    }
}

// ============================================================================
// File 6: SocialMediaWorkflowImpl.java (Workflow Implementation)
// Location: src/main/java/com/cafeflow/workflows/social/SocialMediaWorkflowImpl.java
// ============================================================================

package com.cafeflow.workflows.social;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(workers = "SOCIAL_WORKER")
public class SocialMediaWorkflowImpl implements SocialMediaWorkflow {

    private final SocialMediaActivities activities = Workflow.newActivityStub(
        SocialMediaActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .build())
            .build()
    );

    @Override
    public void postRedditToTwitter() {
        // Fetch top 5 posts from r/programming
        List<RedditPost> posts = activities.fetchTopPosts("programming", 5);
        
        // Post summary to Twitter
        activities.postToTwitter(posts);
    }
}

// ============================================================================
// Configuration Files
// ============================================================================

// application.yml
/*
spring:
  temporal:
    workers:
      - name: "SOCIAL_WORKER"
        task-queue: "SOCIAL_WORKER"
        workflow-classes:
          - com.cafeflow.workflows.social.SocialMediaWorkflowImpl
        activity-beans:
          - socialMediaActivitiesImpl
*/

// pom.xml (Maven dependency for Twitter4J)
/*
<dependency>
    <groupId>org.twitter4j</groupId>
    <artifactId>twitter4j-core</artifactId>
    <version>4.0.7</version>
</dependency>
*/

// Environment variables
/*
export TWITTER_CONSUMER_KEY=your-consumer-key
export TWITTER_CONSUMER_SECRET=your-consumer-secret
export TWITTER_ACCESS_TOKEN=your-access-token
export TWITTER_ACCESS_SECRET=your-access-secret
*/

// ============================================================================
// KEY POINTS
// ============================================================================
/*
1. TwitterHelper is WORKFLOW-SPECIFIC (in workflows/social/helpers/)
   - Only this workflow needs Twitter
   - Configuration in constructor via System.getenv()
   - Still follows BaseHelper pattern
   - Has @Component for injection

2. RedditHelper is FRAMEWORK helper (reused)
   - Already exists and implemented
   - Public API, no auth needed
   - Used by this and potentially other workflows

3. Both helpers injected into activities via constructor
   - @RequiredArgsConstructor handles injection
   - Activities compose multiple helpers

4. Workflow-specific helper location matters:
   - workflows/social/helpers/TwitterHelper.java (CORRECT)
   - helpers/communication/TwitterHelper.java (WRONG - would be framework)
*/
