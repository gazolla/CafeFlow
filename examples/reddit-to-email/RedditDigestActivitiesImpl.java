package com.cafeflow.workflows.reddit;

import com.cafeflow.helpers.ai.TextSummarizerHelper;
import com.cafeflow.helpers.communication.EmailHelper;
import com.cafeflow.helpers.marketing.RedditHelper;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ActivityImpl(workers = "REDDIT_DIGEST_WORKER")
@RequiredArgsConstructor
public class RedditDigestActivitiesImpl implements RedditDigestActivities {

    private final RedditHelper redditHelper;
    private final TextSummarizerHelper textSummarizerHelper;
    private final EmailHelper emailHelper;

    @Override
    public List<RedditPost> fetchTopPosts(String subreddit, int limit) {
        log.info("Fetching top {} posts from r/{}", limit, subreddit);
        return redditHelper.fetchTopPosts(subreddit, limit, RedditPost.class);
    }

    @Override
    public String summarizePosts(List<RedditPost> posts) {
        log.info("Summarizing {} posts", posts.size());

        List<String> texts = posts.stream()
                .map(post -> "Title: " + post.title() + "\nContent: " +
                        (post.selfText() != null ? post.selfText() : "No text content"))
                .toList();

        List<String> summaries = textSummarizerHelper.summarizeBatch(texts);

        StringBuilder digest = new StringBuilder("# Reddit Digest\n\n");
        for (int i = 0; i < posts.size(); i++) {
            digest.append("### ").append(posts.get(i).title()).append("\n");
            digest.append(summaries.get(i)).append("\n");
            digest.append("[Link](").append(posts.get(i).link()).append(")\n\n---\n\n");
        }

        return digest.toString();
    }

    @Override
    public void sendDigestEmail(String email, String content) {
        log.info("Sending digest email to {}", email);
        emailHelper.sendTextEmail(email, "Reddit Digest", content);
    }
}
