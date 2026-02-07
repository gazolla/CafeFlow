package com.cafeflow.workflows.reddit;

import com.cafeflow.helpers.marketing.RedditHelper;
import com.cafeflow.helpers.communication.EmailHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedditActivitiesImpl implements RedditActivities {

    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;

    @Override
    public List<RedditPost> fetchTopPosts(String subreddit, int limit) {
        return redditHelper.fetchTopPosts(subreddit, limit, RedditPost.class);
    }

    @Override
    public void sendEmail(List<RedditPost> posts) {
        StringBuilder body = new StringBuilder("Here are the top posts:\n\n");
        for (int i = 0; i < posts.size(); i++) {
            RedditPost post = posts.get(i);
            body.append(i + 1).append(". ").append(post.title()).append("\n")
                    .append("   Link: ").append(post.link()).append("\n")
                    .append("   Upvotes: ").append(post.upvotes()).append("\n\n");
        }

        emailHelper.sendTextEmail("gazollajunior@outlook.com", "CafeFlow: Top Reddit Posts", body.toString());
    }
}
