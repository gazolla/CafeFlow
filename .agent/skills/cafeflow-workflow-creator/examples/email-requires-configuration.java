// Example: Email Helper Requires Configuration
// User: "Create workflow that emails me daily Reddit digest"

// IMPORTANT: EmailHelper EXISTS but is NOT pre-configured!
// User MUST provide SMTP server details

// ============================================================================
// CONVERSATION FLOW
// ============================================================================

/*
LLM: "I'll create a daily digest workflow. For email functionality, I need to 
know which email service you're using. EmailHelper supports any SMTP server.

Which email provider?
1. Gmail (smtp.gmail.com)
2. Outlook/Office365 (smtp.office365.com)
3. SendGrid (smtp.sendgrid.net)
4. Other (please specify)

Also, I'll need you to set these environment variables:
- SMTP_USERNAME: your email
- SMTP_PASSWORD: your password or app-specific password"

User: "Gmail"

LLM: "Perfect! I'll configure EmailHelper for Gmail SMTP."
*/

// ============================================================================
// File 1: application.yml (EMAIL CONFIGURATION REQUIRED)
// Location: src/main/resources/application.yml
// ============================================================================

/*
spring:
  application:
    name: cafeflow

  # EMAIL CONFIGURATION - USER MUST PROVIDE
  mail:
    host: smtp.gmail.com           # Gmail SMTP server
    port: 587                       # TLS port
    username: ${SMTP_USERNAME}      # User provides via env var
    password: ${SMTP_PASSWORD}      # User provides via env var
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

  temporal:
    connection:
      target: localhost:7233
*/

// Environment variables user must set:
/*
export SMTP_USERNAME=user@gmail.com
export SMTP_PASSWORD=your-app-specific-password

# Note: For Gmail, use App Password, not regular password
# Generate at: https://myaccount.google.com/apppasswords
*/

// ============================================================================
// File 2: RedditPost.java (Domain Model)
// Location: src/main/java/com/cafeflow/workflows/digest/RedditPost.java
// ============================================================================

package com.cafeflow.workflows.digest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
    String title,
    @JsonProperty("url") String link,
    @JsonProperty("ups") int upvotes,
    String author,
    @JsonProperty("created_utc") long createdUtc
) {}

// ============================================================================
// File 3: DailyDigestWorkflow.java (Workflow Interface)
// Location: src/main/java/com/cafeflow/workflows/digest/DailyDigestWorkflow.java
// ============================================================================

package com.cafeflow.workflows.digest;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DailyDigestWorkflow {
    @WorkflowMethod
    void sendDailyDigest(String recipientEmail);
}

// ============================================================================
// File 4: DailyDigestActivities.java (Activities Interface)
// Location: src/main/java/com/cafeflow/workflows/digest/DailyDigestActivities.java
// ============================================================================

package com.cafeflow.workflows.digest;

import io.temporal.activity.ActivityInterface;
import java.util.List;

@ActivityInterface
public interface DailyDigestActivities {
    List<RedditPost> fetchTodayTopPosts();
    void sendDigestEmail(String recipient, List<RedditPost> posts);
}

// ============================================================================
// File 5: DailyDigestActivitiesImpl.java (Activities Implementation)
// Location: src/main/java/com/cafeflow/workflows/digest/DailyDigestActivitiesImpl.java
// ============================================================================

package com.cafeflow.workflows.digest;

import com.cafeflow.helpers.communication.EmailHelper;  // Framework helper (needs config)
import com.cafeflow.helpers.marketing.RedditHelper;     // Framework helper (works as-is)
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@ActivityImpl(workers = "DIGEST_WORKER")
@RequiredArgsConstructor
public class DailyDigestActivitiesImpl implements DailyDigestActivities {

    // Both are framework helpers, but different configuration needs:
    // - RedditHelper: No config needed (public API)
    // - EmailHelper: Requires SMTP configuration in application.yml
    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;

    @Override
    public List<RedditPost> fetchTodayTopPosts() {
        log.info("Fetching today's top posts from r/java");
        
        // Fetch top 20 posts and filter for today
        List<RedditPost> allPosts = redditHelper.fetchTopPosts("java", 20, RedditPost.class);
        
        LocalDate today = LocalDate.now();
        
        return allPosts.stream()
            .filter(post -> isFromToday(post.createdUtc(), today))
            .limit(10)
            .toList();
    }

    @Override
    public void sendDigestEmail(String recipient, List<RedditPost> posts) {
        log.info("Sending digest email to {} with {} posts", recipient, posts.size());
        
        String subject = "Daily Java Digest - " + LocalDate.now();
        String body = formatDigestEmail(posts);
        
        // EmailHelper is configured via application.yml
        // It uses the SMTP settings provided by user
        emailHelper.sendTextEmail(recipient, subject, body);
        
        log.info("Digest email sent successfully");
    }

    private boolean isFromToday(long createdUtc, LocalDate today) {
        Instant instant = Instant.ofEpochSecond(createdUtc);
        LocalDate postDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return postDate.equals(today);
    }

    private String formatDigestEmail(List<RedditPost> posts) {
        StringBuilder email = new StringBuilder();
        email.append("=".repeat(60)).append("\n");
        email.append("DAILY JAVA DIGEST - ").append(LocalDate.now()).append("\n");
        email.append("=".repeat(60)).append("\n\n");
        
        if (posts.isEmpty()) {
            email.append("No top posts from r/java today.\n");
        } else {
            email.append("Top ").append(posts.size()).append(" posts from r/java:\n\n");
            
            for (int i = 0; i < posts.size(); i++) {
                RedditPost post = posts.get(i);
                email.append(String.format("%d. %s\n", i + 1, post.title()));
                email.append(String.format("   Author: %s | Upvotes: %d\n", 
                    post.author(), post.upvotes()));
                email.append(String.format("   Link: %s\n\n", post.link()));
            }
        }
        
        email.append("-".repeat(60)).append("\n");
        email.append("Powered by CafeFlow | Automated Daily Digest\n");
        
        return email.toString();
    }
}

// ============================================================================
// File 6: DailyDigestWorkflowImpl.java (Workflow Implementation)
// Location: src/main/java/com/cafeflow/workflows/digest/DailyDigestWorkflowImpl.java
// ============================================================================

package com.cafeflow.workflows.digest;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;

@WorkflowImpl(workers = "DIGEST_WORKER")
public class DailyDigestWorkflowImpl implements DailyDigestWorkflow {

    private final DailyDigestActivities activities = Workflow.newActivityStub(
        DailyDigestActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .setInitialInterval(Duration.ofSeconds(1))
                .build())
            .build()
    );

    @Override
    public void sendDailyDigest(String recipientEmail) {
        // Fetch today's top posts
        List<RedditPost> posts = activities.fetchTodayTopPosts();
        
        // Send digest email
        activities.sendDigestEmail(recipientEmail, posts);
    }
}

// ============================================================================
// File 7: CafeFlowApplication.java (Schedule Setup)
// Location: src/main/java/com/cafeflow/CafeFlowApplication.java
// ============================================================================

package com.cafeflow;

import com.cafeflow.workflows.digest.DailyDigestWorkflow;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.List;

@SpringBootApplication
@Slf4j
public class CafeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeFlowApplication.class, args);
        log.info("☕ CafeFlow started successfully!");
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner setupDailyDigestSchedule(ScheduleClient scheduleClient) {
        return args -> {
            String scheduleId = "daily-java-digest";
            
            ScheduleActionStartWorkflow action = ScheduleActionStartWorkflow.newBuilder()
                .setWorkflowType(DailyDigestWorkflow.class)
                .setArguments("user@example.com")  // Replace with actual recipient
                .setOptions(WorkflowOptions.newBuilder()
                    .setWorkflowId("daily-digest-run")
                    .setTaskQueue("DIGEST_WORKER")
                    .build())
                .build();
            
            // Every day at 8:00 AM
            ScheduleSpec spec = ScheduleSpec.newBuilder()
                .setCalendars(List.of(
                    ScheduleCalendarSpec.newBuilder()
                        .setHour(List.of(new ScheduleRange(8)))
                        .setMinute(List.of(new ScheduleRange(0)))
                        .build()
                ))
                .build();
            
            Schedule schedule = Schedule.newBuilder()
                .setAction(action)
                .setSpec(spec)
                .setPolicy(SchedulePolicy.newBuilder()
                    .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP)
                    .build())
                .build();
            
            try {
                scheduleClient.createSchedule(scheduleId, schedule, 
                    ScheduleOptions.newBuilder().build());
                log.info("Daily digest schedule created: runs daily at 8:00 AM");
            } catch (ScheduleAlreadyRunningException e) {
                log.info("Daily digest schedule already exists");
            }
        };
    }
}

// ============================================================================
// Configuration: application.yml worker section
// ============================================================================

/*
spring:
  temporal:
    workers:
      - name: "DIGEST_WORKER"
        task-queue: "DIGEST_WORKER"
        workflow-classes:
          - com.cafeflow.workflows.digest.DailyDigestWorkflowImpl
        activity-beans:
          - dailyDigestActivitiesImpl
*/

// ============================================================================
// CRITICAL POINTS ABOUT EMAIL CONFIGURATION
// ============================================================================

/*
1. EmailHelper is a FRAMEWORK helper but NOT pre-configured
   - The class exists in helpers/communication/
   - Spring Boot JavaMailSender is NOT auto-configured by default
   - User MUST add spring.mail.* configuration

2. Different email providers need different settings:
   
   Gmail:
   - Host: smtp.gmail.com
   - Port: 587 (TLS) or 465 (SSL)
   - Requires App Password (not regular password)
   
   Outlook/Office365:
   - Host: smtp.office365.com
   - Port: 587
   - Regular password works
   
   SendGrid:
   - Host: smtp.sendgrid.net
   - Port: 587
   - Username: "apikey"
   - Password: Your SendGrid API key

3. When to use EmailHelper vs create custom helper:
   
   Use EmailHelper when:
   - All workflows use SAME email provider
   - Configure once in application.yml
   - Shared SMTP credentials
   
   Create custom helper when:
   - Different email provider per workflow
   - Example: Marketing emails via SendGrid, transactional via Gmail
   - Put custom helper in workflows/[name]/helpers/

4. Security best practices:
   - NEVER hardcode credentials
   - Always use environment variables
   - Add .env to .gitignore
   - Use app-specific passwords when possible
*/

// ============================================================================
// Example .env file (add to .gitignore!)
// ============================================================================

/*
# Email configuration
SMTP_USERNAME=your.email@gmail.com
SMTP_PASSWORD=your-app-specific-password

# For Gmail: Generate App Password at
# https://myaccount.google.com/apppasswords
*/
