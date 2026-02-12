---
name: cafeflow-workflow-creator
description: Generates complete Temporal workflows, including interfaces, implementations, activities, DTOs, and configuration for the CafeFlow project. This is the primary skill for creating new workflows from a prompt.
---
# CafeFlow Workflow Creator

> **MANDATORY**: Before generating code, you must be familiar with the project's architectural rules located in the `../../rules/` directory.
> Key rules include `temporal-determinism.md`, `java-best-practices.md`, and `configuration.md`. These override all other instructions.

This skill generates Temporal workflows that compose pre-built helpers.

- **80% of requests**: Copy the templates below, substitute placeholders.
- **20% of requests**: Create a new helper using the helper template, then use it in the workflow.

> **GOLDEN EXAMPLE**: See `examples/reddit-to-email/` for a complete, working workflow.
> When in doubt, copy that example and adapt it to the user's request.

---

## Workflow Template

For EVERY workflow request, generate these files in `workflows/__name__/`:

### File 1: `__Name__Workflow.java`

```java
package com.cafeflow.workflows.__name__;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface __Name__Workflow {
    @WorkflowMethod
    __ReturnType__ __methodName__(__ParamType__ __param__);
}
```

### File 2: `__Name__WorkflowImpl.java`

```java
package com.cafeflow.workflows.__name__;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import java.time.Duration;

@WorkflowImpl(workers = "__NAME___WORKER")
public class __Name__WorkflowImpl implements __Name__Workflow {

    private static final Logger log = Workflow.getLogger(__Name__WorkflowImpl.class);

    private final __Name__Activities activities = Workflow.newActivityStub(
        __Name__Activities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .build())
            .build()
    );

    @Override
    public __ReturnType__ __methodName__(__ParamType__ __param__) {
        // Orchestrate activities — NO I/O, NO direct API calls
        // Example:
        // var data = activities.fetchData();
        // var result = activities.processData(data);
        // activities.sendResult(result);
    }
}
```

### File 3: `__Name__Activities.java`

```java
package com.cafeflow.workflows.__name__;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface __Name__Activities {
    @ActivityMethod
    __Type__ __step1__(__params__);

    @ActivityMethod
    __Type__ __step2__(__params__);
    // One @ActivityMethod per I/O operation
}
```

### File 4: `__Name__ActivitiesImpl.java`

```java
package com.cafeflow.workflows.__name__;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(workers = "__NAME___WORKER")
@RequiredArgsConstructor
public class __Name__ActivitiesImpl implements __Name__Activities {

    private final __Helper1__ __helper1__;
    private final __Helper2__ __helper2__;

    @Override
    public __Type__ __step1__(__params__) {
        return __helper1__.__method__(__args__);
    }

    @Override
    public __Type__ __step2__(__params__) {
        return __helper2__.__method__(__args__);
    }
}
```

### File 5: `__Name__DTO.java` (one record per domain object, FLAT in package)

```java
package com.cafeflow.workflows.__name__;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record __Name__DTO(
        String __field1__,
        @JsonProperty("__json_name__") String __field2__,
        int __field3__) {}
```

### File 6: `application.yml` worker block (APPEND to existing `spring.temporal.workers`)

```yaml
      - name: "__NAME___WORKER"
        task-queue: "__NAME___WORKER"
        workflow-classes:
          - com.cafeflow.workflows.__name__.__Name__WorkflowImpl
        activity-beans:
          - __name__ActivitiesImpl
```

### File 7: `.env` (CREATE/UPDATE at project root — this is a REAL FILE, not just a message)

Generate the `.env` file with placeholder values. The user will fill them in.
Look up the required env vars from the Helper Catalog table below.

```env
# === CafeFlow Environment Variables ===
# Required for __Name__ workflow
# Fill in the values below before running the application.

# --- LLM (required if using any AI helper) ---
# Get your key at: https://aistudio.google.com/apikey
GEMINI_API_KEY=your-google-ai-key-here

# --- Email (required if using EmailHelper) ---
# Gmail: Enable 2FA → Google Account > Security > App Passwords
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password-here

# (include ONLY the sections for helpers this workflow actually uses)
```

**Rules for `.env` generation:**
- This is a FILE you must create/update at the project root, NOT just text in the chat
- Include ONLY variables for helpers this workflow uses
- Each variable must have a comment explaining where to get the value
- If `.env` already exists, APPEND new variables — do NOT overwrite existing ones
- If only `RedditHelper` is used (no env vars), still create `.env` with a comment saying no config needed

### File 8 (if recurring): `__Name__SchedulerConfig.java`

If the prompt says "every day", "weekly", "every X minutes", "recurring", etc., generate the scheduler as a SEPARATE `@Configuration` class in the same workflow package. See the **Scheduler Template** section below.

**NEVER put scheduler logic inside `CafeFlowApplication.java`.**

---

## Helper Catalog

### Implemented Helpers (ready to inject)

| Helper | Package | Key Methods | Env Vars |
|--------|---------|-------------|----------|
| `RedditHelper` | `marketing/` | `fetchTopPosts(subreddit, limit, Class<T>)` → `List<T>` | *none* |
| `EmailHelper` | `communication/` | `sendTextEmail(to, subject, body)` | `SMTP_USERNAME`, `SMTP_PASSWORD` |
| `TelegramHelper` | `communication/` | `sendMessage(chatId, text)`, `sendNotification(chatId, text)`, `sendMessageWithInlineMenu(chatId, text, buttons)`, `editMessage(chatId, msgId, newText)` | `TELEGRAM_BOT_TOKEN` |
| `TwitterHelper` | `marketing/` | `getUserByUsername(username)` → `String`, `postTweet(text)` → `String` | `X_BEARER_TOKEN` |
| `GDriveHelper` | `office/google/` | `listFiles(pageSize)` → `List<File>`, `uploadFile(name, mimeType, content)` → `File`, `downloadFile(fileId)` → `InputStream` | `GD_CREDENTIALS_PATH` |
| `TextSummarizerHelper` | `ai/` | `summarize(text)`, `summarize(text, maxSentences)`, `summarizeBatch(texts)`, `summarizeToLanguage(text, lang)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |
| `SentimentAnalyzerHelper` | `ai/` | `analyze(text)` → `SentimentResult`, `analyzeBatch(texts)`, `classifySimple(text)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |
| `TextTranslatorHelper` | `ai/` | `translate(text, targetLang)`, `translate(text, src, target)`, `translateBatch(texts, lang)`, `detectLanguage(text)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |
| `ContentGeneratorHelper` | `ai/` | `generate(instruction)`, `generateEmail(topic, context, keyPoints)`, `generateSocialPost(platform, topic, tone)`, `generateTweet(content)`, `generateReport(title, dataPoints)`, `rewriteInTone(text, tone)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |
| `DataExtractorHelper` | `ai/` | `extractFields(text, fieldNames)` → `ExtractionResult`, `extractEntities(text)`, `extractKeyValues(text)`, `extractActionItems(text)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |
| `TextClassifierHelper` | `ai/` | `classify(text, categories)` → `ClassificationResult`, `classifyBatch(texts, categories)`, `classifyBoolean(text, question)`, `matchesCriteria(text, criteria)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |
| `TopicExtractorHelper` | `ai/` | `extractTopics(text)`, `generateHashtags(text, max)`, `extractKeywords(text, max)`, `generateTopicLabel(text)` | `GEMINI_API_KEY` or `GROQ_API_KEY` |

**AI Helper Return Types:**
```java
record SentimentResult(String sentiment, double confidence, String explanation)
record ClassificationResult(String category, double confidence, String reasoning)
record ExtractionResult(Map<String, String> fields, List<String> entities, double confidence)
```

### Skeleton Helpers (exist but need implementation before use)

| Helper | Package | Notes |
|--------|---------|-------|
| `SlackHelper` | `office/` | Webhook-based messaging |
| `DiscordHelper` | `communication/` | Webhook-based messaging |
| `WhatsAppHelper` | `communication/` | WhatsApp Business API |
| `GitHubHelper` | `development/` | GitHub REST API |
| `GmailHelper` | `office/google/` | Gmail API (not SMTP) |
| `GDocsHelper` | `office/google/` | Google Docs API |
| `GSheetsHelper` | `office/google/` | Google Sheets API |
| `GCalendarHelper` | `office/google/` | Google Calendar API |
| `PostgreSQLHelper` | `devops/database/` | JDBC operations |
| `NotionHelper` | `office/` | Notion API |
| `AsanaJiraHelper` | `office/` | Project management APIs |
| `FacebookHelper` | `marketing/` | Facebook Graph API |
| `InstagramHelper` | `marketing/` | Instagram Graph API |
| `LinkedInHelper` | `marketing/` | LinkedIn API |
| `MS365Helper` | `office/microsoft/` | Microsoft 365 |
| `MSGraphHelper` | `office/microsoft/` | Microsoft Graph API |
| `MSOutlookHelper` | `office/microsoft/` | Outlook API |

**When a skeleton exists**: implement the existing file. Do NOT create a new class.

**For full method signatures and detailed examples, see:** `references/helpers-reference.md`

---

## Creating a New Helper

### When to create

- Prompt needs a service with NO existing helper AND no skeleton → create new
- Skeleton exists → implement the skeleton file (do NOT duplicate)

### Where to place

- Will be reused by multiple workflows → `helpers/[category]/`
- Only this workflow needs it → `workflows/[name]/`

### Framework Helper Template

```java
package com.cafeflow.helpers.__category__;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class __Name__Helper extends BaseHelper {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${__service__.api.key:}")
    private String apiKey;

    @Override
    protected String getServiceName() {
        return "__service__";
    }

    public __ReturnType__ __method__(__params__) {
        return executeWithProtection("__method__", () -> {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.__service__.com/v1/__endpoint__"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Parse and return
        });
    }
}
```

### AI Helper Template (injects LLMClient)

```java
package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class __Name__Helper extends BaseHelper {

    private final LLMClient llmClient;

    @Override
    protected String getServiceName() {
        return "__service__";
    }

    public String __method__(String input) {
        return executeWithProtection("__method__", () -> {
            String prompt = """
                __Your prompt template here__

                Input: %s
                """.formatted(input);
            return llmClient.send(prompt);
        });
    }
}
```

### New Helper Checklist

- [ ] Extends `BaseHelper`
- [ ] Has `@Component`
- [ ] Implements `getServiceName()`
- [ ] ALL I/O inside `executeWithProtection()`
- [ ] No hardcoded secrets — use `@Value("${...}")` or env vars
- [ ] AI helpers inject `LLMClient` via constructor, never call it from activities directly
- [ ] Add required env vars to `.env` snippet and `application.yml` if needed

---

## Scheduler Template

If the prompt says "every day", "weekly", "recurring", "scheduled", add a scheduler class:

### `__Name__SchedulerConfig.java`

```java
package com.cafeflow.workflows.__name__;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class __Name__SchedulerConfig {

    private final ScheduleClient scheduleClient;

    @Bean
    @Profile("!test")
    public CommandLineRunner setup__Name__Schedule() {
        return args -> {
            String scheduleId = "__name__-schedule";
            Schedule schedule = Schedule.newBuilder()
                .setAction(ScheduleActionStartWorkflow.newBuilder()
                    .setWorkflowType(__Name__Workflow.class)
                    .setOptions(WorkflowOptions.newBuilder()
                        .setWorkflowId("__name__-run")
                        .setTaskQueue("__NAME___WORKER")
                        .build())
                    .build())
                .setSpec(ScheduleSpec.newBuilder()
                    .setIntervals(List.of(
                        new ScheduleIntervalSpec(Duration.of__Unit__(__value__))))
                    .build())
                .setPolicy(SchedulePolicy.newBuilder()
                    .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP)
                    .build())
                .build();
            try {
                scheduleClient.createSchedule(scheduleId, schedule, ScheduleOptions.newBuilder().build());
                log.info("Schedule created: {}", scheduleId);
            } catch (io.temporal.client.schedules.ScheduleAlreadyRunningException e) {
                log.info("Schedule already exists: {}", scheduleId);
            }
        };
    }
}
```

For advanced schedule patterns (calendar specs, cron, overlap policies), see: `.agent/skills/temporal-scheduler/SKILL.md`

---

## Post-Generation Steps (ALWAYS execute after generating code)

### Step 1: Check Docker
Run `docker ps` to verify Temporal is running. If NOT running, tell the user:
```
Docker is not running. Start it with: docker-compose up -d
Wait for Temporal to be ready, then continue.
```

### Step 2: Generate `.env` file
Create/update the `.env` file at the project root with ONLY the variables this workflow needs.
Use placeholder values that the user must fill:
```env
# === Required for [Workflow Name] ===
# Get your key at: https://aistudio.google.com/apikey
GEMINI_API_KEY=your-google-ai-key-here
# Gmail: Enable 2FA → Google Account > Security > App Passwords
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password-here
```

### Step 3: Instruct the user
ALWAYS output this after the code and `.env`:

```markdown
## Next Steps

1. **Fill your `.env`** — I created the file with placeholders. Replace the values:
   - [Service]: [URL/instructions to get credentials]
   - [Service]: [URL/instructions to get credentials]

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```
   The ConfigurationValidator will confirm all helpers are ready on startup.

3. **Monitor**: http://localhost:8081 (Temporal UI)
```

### Rules:
- NEVER include env vars for helpers the workflow does NOT use
- If 3 AI helpers are used, list `GEMINI_API_KEY` only ONCE
- If only `RedditHelper` is used, say "No `.env` changes needed"
- ALWAYS include setup steps with links for services requiring account creation
- NEVER run `mvn spring-boot:run` yourself — only tell the user to run it

---

## Reference Skills (consult only when needed)

- **Advanced Temporal patterns** (signals, queries, testing): `.agent/skills/temporal-orchestrator/SKILL.md`
- **Schedule configuration** (calendar specs, cron, overlap policies): `.agent/skills/temporal-scheduler/SKILL.md`
