// Example: Implementing Skeleton Helper
// User: "Create workflow that sends build status to Slack #engineering"

// ANALYSIS:
// - Slack: Skeleton exists in helpers/communication/SlackHelper.java
// - Multiple workflows might need Slack notifications
// - Decision: IMPLEMENT the skeleton as framework helper

// ============================================================================
// File 1: SlackHelper.java (IMPLEMENTING SKELETON)
// Location: src/main/java/com/cafeflow/helpers/communication/SlackHelper.java
// ============================================================================

package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Framework helper for Slack webhook integrations.
 * Supports sending messages and formatted attachments.
 */
@Slf4j
@Component
public class SlackHelper extends BaseHelper {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @Override
    protected String getServiceName() {
        return "slack";
    }

    /**
     * Send simple text message to channel.
     * 
     * @param channel Channel name (e.g., "#engineering")
     * @param message Message text
     */
    public void sendMessage(String channel, String message) {
        executeWithProtection("sendMessage", () -> {
            Map<String, Object> payload = Map.of(
                "channel", channel,
                "text", message,
                "username", "CafeFlow Bot",
                "icon_emoji", ":robot_face:"
            );
            
            sendToSlack(payload);
        });
    }

    /**
     * Send formatted message with color and title.
     * 
     * @param channel Channel name
     * @param title Message title
     * @param text Message body
     * @param color Color (good, warning, danger, or hex like #36a64f)
     */
    public void sendFormattedMessage(String channel, String title, String text, String color) {
        executeWithProtection("sendFormattedMessage", () -> {
            Map<String, Object> attachment = Map.of(
                "title", title,
                "text", text,
                "color", color,
                "footer", "CafeFlow Automation",
                "ts", Instant.now().getEpochSecond()
            );
            
            Map<String, Object> payload = Map.of(
                "channel", channel,
                "attachments", List.of(attachment)
            );
            
            sendToSlack(payload);
        });
    }

    /**
     * Send message with fields (key-value pairs).
     * 
     * @param channel Channel name
     * @param title Message title
     * @param fields Map of field names to values
     * @param color Message color
     */
    public void sendMessageWithFields(
        String channel, 
        String title, 
        Map<String, String> fields,
        String color
    ) {
        executeWithProtection("sendMessageWithFields", () -> {
            List<Map<String, Object>> fieldList = fields.entrySet().stream()
                .map(entry -> Map.of(
                    "title", (Object) entry.getKey(),
                    "value", entry.getValue(),
                    "short", false
                ))
                .toList();
            
            Map<String, Object> attachment = Map.of(
                "title", title,
                "fields", fieldList,
                "color", color,
                "footer", "CafeFlow",
                "ts", Instant.now().getEpochSecond()
            );
            
            Map<String, Object> payload = Map.of(
                "channel", channel,
                "attachments", List.of(attachment)
            );
            
            sendToSlack(payload);
        });
    }

    private void sendToSlack(Map<String, Object> payload) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(payload);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();
        
        HttpResponse<String> response = httpClient.send(
            request, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Slack API error: " + response.statusCode() + " - " + response.body()
            );
        }
    }
}

// ============================================================================
// File 2: BuildStatus.java (Domain Model)
// Location: src/main/java/com/cafeflow/workflows/build/BuildStatus.java
// ============================================================================

package com.cafeflow.workflows.build;

public record BuildStatus(
    String projectName,
    boolean success,
    String commitHash,
    int testsRun,
    int testsPassed,
    long durationMs
) {
    public String getStatusEmoji() {
        return success ? "✅" : "❌";
    }
    
    public String getColor() {
        return success ? "good" : "danger";
    }
}

// ============================================================================
// File 3: BuildNotificationWorkflow.java (Workflow Interface)
// Location: src/main/java/com/cafeflow/workflows/build/BuildNotificationWorkflow.java
// ============================================================================

package com.cafeflow.workflows.build;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface BuildNotificationWorkflow {
    @WorkflowMethod
    void notifyBuildStatus(BuildStatus status);
}

// ============================================================================
// File 4: BuildNotificationActivities.java (Activities Interface)
// Location: src/main/java/com/cafeflow/workflows/build/BuildNotificationActivities.java
// ============================================================================

package com.cafeflow.workflows.build;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface BuildNotificationActivities {
    void sendSlackNotification(BuildStatus status);
}

// ============================================================================
// File 5: BuildNotificationActivitiesImpl.java (Activities Implementation)
// Location: src/main/java/com/cafeflow/workflows/build/BuildNotificationActivitiesImpl.java
// ============================================================================

package com.cafeflow.workflows.build;

import com.cafeflow.helpers.communication.SlackHelper;  // Framework helper (implemented)
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@ActivityImpl(workers = "BUILD_WORKER")
@RequiredArgsConstructor
public class BuildNotificationActivitiesImpl implements BuildNotificationActivities {

    private final SlackHelper slackHelper;  // Using implemented skeleton

    @Override
    public void sendSlackNotification(BuildStatus status) {
        log.info("Sending build notification to Slack for {}", status.projectName());
        
        String title = String.format(
            "%s Build %s: %s",
            status.getStatusEmoji(),
            status.success() ? "Succeeded" : "Failed",
            status.projectName()
        );
        
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Commit", status.commitHash().substring(0, 7));
        fields.put("Tests", String.format("%d/%d passed", status.testsPassed(), status.testsRun()));
        fields.put("Duration", formatDuration(status.durationMs()));
        
        slackHelper.sendMessageWithFields(
            "#engineering",
            title,
            fields,
            status.getColor()
        );
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%dm %ds", minutes, seconds);
    }
}

// ============================================================================
// File 6: BuildNotificationWorkflowImpl.java (Workflow Implementation)
// Location: src/main/java/com/cafeflow/workflows/build/BuildNotificationWorkflowImpl.java
// ============================================================================

package com.cafeflow.workflows.build;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl(workers = "BUILD_WORKER")
public class BuildNotificationWorkflowImpl implements BuildNotificationWorkflow {

    private final BuildNotificationActivities activities = Workflow.newActivityStub(
        BuildNotificationActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .build()
    );

    @Override
    public void notifyBuildStatus(BuildStatus status) {
        activities.sendSlackNotification(status);
    }
}

// ============================================================================
// Configuration Files
// ============================================================================

// application.yml
/*
slack:
  webhook:
    url: ${SLACK_WEBHOOK_URL}

spring:
  temporal:
    workers:
      - name: "BUILD_WORKER"
        task-queue: "BUILD_WORKER"
        workflow-classes:
          - com.cafeflow.workflows.build.BuildNotificationWorkflowImpl
        activity-beans:
          - buildNotificationActivitiesImpl
*/

// Environment variable
/*
export SLACK_WEBHOOK_URL=https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX
*/

// ============================================================================
// KEY POINTS
// ============================================================================
/*
1. SlackHelper was SKELETON - we implemented it
   - Located in helpers/communication/ (framework)
   - Multiple workflows can use Slack notifications
   - Configuration via application.yml + environment variable

2. Implementation follows BaseHelper pattern
   - Extends BaseHelper
   - Implements getServiceName()
   - Uses executeWithProtection for all I/O
   - Has @Component annotation

3. Three methods for flexibility
   - sendMessage: Simple text
   - sendFormattedMessage: With title and color
   - sendMessageWithFields: Structured data (key-value pairs)

4. Framework helper means ANY workflow can use it
   - This build workflow uses it
   - Future deployment workflows can use it
   - Monitoring workflows can use it
   - All share same webhook configuration

5. When to implement skeleton vs create new helper:
   - Skeleton exists → Implement it (this example)
   - No skeleton, reusable → Create framework helper
   - No skeleton, single-use → Create workflow helper
*/
