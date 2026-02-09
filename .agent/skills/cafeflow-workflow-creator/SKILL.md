# CafeFlow Workflow Creator Skill

## Purpose
Guide AI to create complete, production-ready Temporal workflows in CafeFlow. This includes implementing new helpers when needed, following the BaseHelper pattern, and properly organizing workflow-specific vs. reusable helpers.

## When to Use This Skill
Trigger when user requests:
- "Create a workflow that..."
- "Build an automation for..."
- "Automate [process]..."
- Any request involving Temporal workflow creation

## Critical Architecture Principles

### 1. Helpers Are NOT Pre-Configured
**IMPORTANT**: The CafeFlow framework does NOT provide ready-to-use service integrations. Each project must configure its own services.

**Example**: EmailHelper exists as a class, but:
- User MUST provide SMTP server details
- User MUST configure credentials
- There is NO default email service

**This means**: When user says "send email", you MUST ask which email service to use OR implement a helper for their specific service.

### 2. Helper Types

**Framework Helpers** (`helpers/[category]/`):
- Reusable across MULTIPLE workflows
- Generic service integrations
- Examples: RedditHelper (public API), SlackHelper (if used by many workflows)

**Workflow Helpers** (`workflows/[name]/helpers/`):
- Used by SINGLE workflow only
- Workflow-specific configuration
- Examples: TwitterHelper for social-media workflow, StripeHelper for payment workflow

### 3. When to Create Which Helper

```
Does multiple workflows need this service?
  ├─ YES → Create Framework Helper (helpers/[category]/)
  │        Example: Multiple workflows need Slack notifications
  │
  └─ NO → Create Workflow Helper (workflows/[name]/helpers/)
           Example: Only payment workflow needs Stripe
```

### 4. Always Follow BaseHelper Pattern
ALL helpers (framework and workflow) MUST:
- Extend `BaseHelper`
- Implement `getServiceName()`
- Use `executeWithProtection()` for all I/O operations
- Have `@Component` annotation (for dependency injection)

### 5. LLM Integration Layer
**IMPORTANT**: The `core.llm` package provides AI capabilities as infrastructure.
- `LLMClient` is registered as a Spring bean via `LLMConfig` (`@Configuration`)
- Helpers in `helpers/ai/` wrap `LLMClient` with BaseHelper protection
- **NEVER** call `LLMClient` directly in workflows or activities — always use AI helpers
- AI helpers can be composed with ANY other helper (Reddit + Summarizer + Email)
- At least one LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`) must be set for AI helpers to work
- The default provider is configured via `llm.default-provider` in `application.yml` (default: `gemini`)
- Available providers: Google Gemini (`gemini-1.5-flash`) and Groq (`llama-3.3-70b-versatile`)

---

## Project Structure

```
src/main/java/com/cafeflow/
├── core/
│   ├── base/
│   │   ├── BaseHelper.java                    # Base class all helpers extend
│   │   ├── ThrowingRunnable.java
│   │   └── ThrowingSupplier.java
│   └── exception/
│       └── HelperException.java
│
├── helpers/                                     # FRAMEWORK helpers (reusable)
│   ├── ai/
│   │   ├── TextSummarizerHelper.java           # LLM-powered text summarization
│   │   ├── SentimentAnalyzerHelper.java        # LLM-powered sentiment analysis
│   │   └── SentimentResult.java                # Sentiment result record
│   ├── communication/
│   │   ├── EmailHelper.java                    # Generic SMTP (requires config)
│   │   └── SlackHelper.java                    # Skeleton (implement if needed)
│   ├── marketing/
│   │   └── RedditHelper.java                   # Implemented (public API)
│   ├── development/
│   │   └── GitHubHelper.java                   # Skeleton
│   └── office/google/
│       ├── GmailHelper.java                    # Skeleton
│       └── GDriveHelper.java                   # Skeleton
│
└── workflows/                                   # Workflow packages
    └── [workflow-name]/
        ├── [Name]Workflow.java                 # Workflow interface
        ├── [Name]WorkflowImpl.java             # Workflow implementation
        ├── [Name]Activities.java               # Activities interface
        ├── [Name]ActivitiesImpl.java           # Activities implementation
        ├── [DomainModel].java                  # DTOs/Records
        └── helpers/                             # WORKFLOW-SPECIFIC helpers
            └── [Service]Helper.java
```

---

## Available Framework Helpers

### Fully Implemented
**RedditHelper** (`helpers/marketing/`):
- Public Reddit API integration
- No authentication required
- Generic JSON mapping with `fetchTopPosts(subreddit, limit, Class)`

### Require Configuration
**EmailHelper** (`helpers/communication/`):
- Generic SMTP helper
- **User MUST configure**: SMTP host, port, username, password
- Used when multiple workflows need email with SAME SMTP server

### AI Helpers (LLM-powered)

**TextSummarizerHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `summarize(text)`, `summarize(text, maxSentences)`, `summarizeBatch(texts)`, `summarizeToLanguage(text, language)`
- Use when: Workflow needs to summarize posts, articles, emails, documents
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**SentimentAnalyzerHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `analyze(text)`, `analyzeBatch(texts)`, `classifySimple(text)`
- Returns: `SentimentResult` record with `sentiment` (positive/negative/neutral), `confidence` (0.0-1.0), `explanation`
- Use when: Workflow needs to classify sentiment, filter by mood, detect negativity
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**TextTranslatorHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `translate(text, targetLanguage)`, `translate(text, source, target)`, `translateBatch(texts, targetLanguage)`, `detectLanguage(text)`
- Use when: Workflow needs to translate content between languages or detect source language
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**ContentGeneratorHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `generate(instruction)`, `generateEmail(topic, recipientContext, keyPoints)`, `generateSocialPost(platform, topic, tone)`, `generateTweet(content)`, `generateReport(title, dataPoints)`, `rewriteInTone(text, tone)`
- Use when: Workflow needs to create emails, social posts, tweets, reports, or rewrite content
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**DataExtractorHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `extractFields(text, fieldNames)`, `extractEntities(text)`, `extractKeyValues(text)`, `extractActionItems(text)`
- Returns: `ExtractionResult` record with `fields` (Map), `entities` (List), `confidence`
- Use when: Workflow needs to parse unstructured text into structured data (invoices, emails, meeting notes)
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**TextClassifierHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `classify(text, categories)`, `classifyBatch(texts, categories)`, `classifySimple(text, categories)`, `classifyBoolean(text, question)`, `matchesCriteria(text, criteria)`
- Returns: `ClassificationResult` record with `category`, `confidence`, `reasoning`
- Use when: Workflow needs to categorize content, filter by criteria, or answer yes/no questions about text
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**TopicExtractorHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `extractTopics(text)`, `extractTopicsBatch(texts)`, `generateHashtags(text, max)`, `extractKeywords(text, max)`, `generateTopicLabel(text)`
- Use when: Workflow needs to tag content, generate hashtags, extract keywords, or auto-label topics
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

### Skeletons (Need Implementation)
- SlackHelper
- GitHubHelper
- GmailHelper
- GDriveHelper
- PostgreSQLHelper

**When you see skeleton**: Implement it if workflow needs it AND it will be reused!

---

## Workflow Creation Process

### Step 1: Analyze User Request

Extract:
- **Services needed**: What external services? (Email, Slack, Twitter, Stripe, etc.)
- **Business logic**: What should workflow do?
- **Configuration**: Are services provided or do you need to ask?

**Example**:
```
User: "Create workflow that posts Reddit top posts to Twitter"

Analysis:
- Services: Reddit (read), Twitter (post)
- Logic: Fetch → Format → Post
- Configuration: Twitter API credentials needed
```

### Step 2: Decide Helper Strategy

For EACH service, ask:

```
Q1: Does a framework helper exist?
├─ RedditHelper exists → ✅ USE IT (already implemented)
├─ EmailHelper exists BUT needs config → ASK user for SMTP details
└─ TwitterHelper doesn't exist → CREATE IT

Q2: Will other workflows use this service?
├─ YES → Create Framework Helper (helpers/[category]/)
└─ NO → Create Workflow Helper (workflows/[name]/helpers/)

Q3: Is there a skeleton?
├─ YES → Implement the skeleton
└─ NO → Create new helper from scratch
```

**Decision Examples**:

| Service | Exists? | Reusable? | Decision |
|---------|---------|-----------|----------|
| Reddit | Yes (implemented) | - | ✅ Use RedditHelper |
| Email (Gmail SMTP) | Yes (needs config) | Yes | ✅ Configure EmailHelper |
| Email (SendGrid) | No | Only this workflow | Create WorkflowHelper |
| Twitter | No | Only this workflow | Create WorkflowHelper |
| Slack | Yes (skeleton) | Multiple workflows | Implement SlackHelper |
| Stripe | No | Only payment workflow | Create WorkflowHelper |
| Notion | No | Multiple workflows will use | Create FrameworkHelper |
| Text summarization | Yes (AI helper) | - | ✅ Use TextSummarizerHelper |
| Sentiment analysis | Yes (AI helper) | - | ✅ Use SentimentAnalyzerHelper |
| Custom LLM behavior | No | Only this workflow | Create workflow helper injecting LLMClient |

### Step 3: Create/Implement Helpers

#### Option A: Use Existing Helper (RedditHelper)

No code needed - just inject and use:

```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;  // Already implemented
    
    public List<RedditPost> fetch() {
        return redditHelper.fetchTopPosts("java", 10, RedditPost.class);
    }
}
```

#### Option B: Configure Existing Helper (EmailHelper)

Helper exists but needs SMTP configuration:

**Ask user**:
```
To use EmailHelper, I need your SMTP configuration:
- SMTP Host: (e.g., smtp.gmail.com)
- SMTP Port: (e.g., 587)
- Username: Your email
- Password: Your email password or app password

Should I use EmailHelper with your SMTP, or create a workflow-specific helper?
```

**Then configure** in `application.yml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
```

**And inject**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final EmailHelper emailHelper;  // Uses configured SMTP
}
```

#### Option C: Implement Skeleton Helper

Example: Workflow needs Slack, skeleton exists

**File**: `helpers/communication/SlackHelper.java`

```java
package com.cafeflow.helpers.communication;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

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

    public void sendMessage(String channel, String message) {
        executeWithProtection("sendMessage", () -> {
            Map<String, Object> payload = Map.of(
                "channel", channel,
                "text", message
            );
            
            String json = objectMapper.writeValueAsString(payload);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
            );
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Slack API error: " + response.statusCode());
            }
        });
    }
}
```

**Configuration** (`application.yml`):
```yaml
slack:
  webhook:
    url: ${SLACK_WEBHOOK_URL}
```

#### Option D: Create Workflow-Specific Helper

Example: Workflow needs Twitter (only this workflow)

**File**: `workflows/social/helpers/TwitterHelper.java`

```java
package com.cafeflow.workflows.social.helpers;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Slf4j
@Component
public class TwitterHelper extends BaseHelper {

    private final Twitter twitter;

    public TwitterHelper() {
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

    public void postTweet(String message) {
        executeWithProtection("postTweet", () -> {
            twitter.updateStatus(message);
        });
    }
}
```

**Note**: Helper is in `workflows/social/helpers/` because only this workflow needs it.

**Maven Dependency**:
```xml
<dependency>
    <groupId>org.twitter4j</groupId>
    <artifactId>twitter4j-core</artifactId>
    <version>4.0.7</version>
</dependency>
```

#### Option E: Create New Framework Helper

Example: Multiple workflows will need Notion

**File**: `helpers/productivity/NotionHelper.java`

```java
package com.cafeflow.helpers.productivity;

import com.cafeflow.core.base.BaseHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@Component
public class NotionHelper extends BaseHelper {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${notion.api.key}")
    private String apiKey;
    
    @Value("${notion.version:2022-06-28}")
    private String notionVersion;

    @Override
    protected String getServiceName() {
        return "notion";
    }

    public void createPage(String databaseId, Map<String, Object> properties) {
        executeWithProtection("createPage", () -> {
            Map<String, Object> payload = Map.of(
                "parent", Map.of("database_id", databaseId),
                "properties", properties
            );
            
            String json = objectMapper.writeValueAsString(payload);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.notion.com/v1/pages"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Notion-Version", notionVersion)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Notion API error: " + response.statusCode());
            }
        });
    }
}
```

**Configuration** (`application.yml`):
```yaml
notion:
  api:
    key: ${NOTION_API_KEY}
  version: 2022-06-28
```

**Note**: Helper is in `helpers/productivity/` because multiple workflows might use Notion.

### Step 4: Create Domain Models

Use Records for immutability:

```java
package com.cafeflow.workflows.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
    String title,
    @JsonProperty("url") String link,
    @JsonProperty("ups") int upvotes
) {}
```

### Step 5: Create Workflow Files

**Workflow Interface**:
```java
@WorkflowInterface
public interface SocialMediaWorkflow {
    @WorkflowMethod
    void postRedditToTwitter();
}
```

**Activities Interface**:
```java
@ActivityInterface
public interface SocialMediaActivities {
    List<RedditPost> fetchTopPosts();
    void postToTwitter(List<RedditPost> posts);
}
```

**Activities Implementation**:
```java
@Slf4j
@Component
@ActivityImpl(workers = "SOCIAL_WORKER")
@RequiredArgsConstructor
public class SocialMediaActivitiesImpl implements SocialMediaActivities {

    private final RedditHelper redditHelper;  // Framework helper
    private final TwitterHelper twitterHelper;  // Workflow helper

    @Override
    public List<RedditPost> fetchTopPosts() {
        return redditHelper.fetchTopPosts("programming", 5, RedditPost.class);
    }

    @Override
    public void postToTwitter(List<RedditPost> posts) {
        String tweet = formatTweet(posts);
        twitterHelper.postTweet(tweet);
    }

    private String formatTweet(List<RedditPost> posts) {
        return "Top programming posts: " + 
               posts.stream()
                    .map(RedditPost::title)
                    .collect(Collectors.joining(", "));
    }
}
```

**Workflow Implementation**:
```java
@WorkflowImpl(workers = "SOCIAL_WORKER")
public class SocialMediaWorkflowImpl implements SocialMediaWorkflow {

    private final SocialMediaActivities activities = Workflow.newActivityStub(
        SocialMediaActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .build()
    );

    @Override
    public void postRedditToTwitter() {
        List<RedditPost> posts = activities.fetchTopPosts();
        activities.postToTwitter(posts);
    }
}
```

### Step 6: Configure Worker

**application.yml**:
```yaml
spring:
  temporal:
    workers:
      - name: "SOCIAL_WORKER"
        task-queue: "SOCIAL_WORKER"
        workflow-classes:
          - com.cafeflow.workflows.social.SocialMediaWorkflowImpl
        activity-beans:
          - socialMediaActivitiesImpl
```

---

## Helper Creation Checklist

When creating ANY helper (framework or workflow), ensure:

- [ ] Extends `BaseHelper`
- [ ] Implements `getServiceName()`
- [ ] Has `@Component` annotation
- [ ] Has `@Slf4j` annotation (recommended)
- [ ] Uses `executeWithProtection()` for ALL I/O
- [ ] Configuration via `@Value` (framework) or `System.getenv()` (workflow)
- [ ] No hardcoded credentials
- [ ] Proper package location (helpers/ vs workflows/[name]/helpers/)

### AI Helper Checklist (additional)
- [ ] Injects `LLMClient` (not creates manually)
- [ ] Prompt engineering is clear and specific
- [ ] Handles LLM response parsing errors gracefully
- [ ] Falls back to sensible default on failure
- [ ] Does NOT expose raw LLM responses to workflows

---

## Configuration Rules

### Framework Helpers
**Configuration**: `application.yml` + Environment Variables

```yaml
service:
  api:
    key: ${SERVICE_API_KEY}
```

```java
@Component
public class ServiceHelper extends BaseHelper {
    @Value("${service.api.key}")
    private String apiKey;
}
```

### Workflow Helpers
**Configuration**: `System.getenv()` directly in constructor

```java
@Component
public class WorkflowServiceHelper extends BaseHelper {
    private final String apiKey;
    
    public WorkflowServiceHelper() {
        this.apiKey = System.getenv("WORKFLOW_SERVICE_API_KEY");
    }
}
```

### Helper → Required Environment Variables

When generating a workflow, identify ALL helpers used and their required variables:

| Helper | Required Environment Variables | Notes |
|--------|-------------------------------|-------|
| RedditHelper | *(none)* | Public API, no credentials |
| EmailHelper | `SMTP_USERNAME`, `SMTP_PASSWORD` | Optional: `SMTP_HOST`, `SMTP_PORT` |
| TelegramHelper | `TELEGRAM_BOT_TOKEN` | From @BotFather |
| TwitterHelper | `X_BEARER_TOKEN` | Optional: `X_API_KEY`, `X_API_SECRET` |
| GDriveHelper | `GD_CREDENTIALS_PATH` | OAuth2 credentials JSON |
| SlackHelper | `SLACK_WEBHOOK_URL` | Incoming Webhook |
| GitHubHelper | `GITHUB_TOKEN` | Personal Access Token |
| *All AI Helpers* | `GEMINI_API_KEY` **or** `GROQ_API_KEY` | At least one LLM provider |

**AI Helpers that require LLM config**: TextSummarizerHelper, SentimentAnalyzerHelper, TextTranslatorHelper, ContentGeneratorHelper, DataExtractorHelper, TextClassifierHelper, TopicExtractorHelper.

### NEVER Ask Users For
- ❌ SMTP server details (ask which email service, then you configure)
- ❌ API endpoints (you look these up)
- ❌ Port numbers (you know these)
- ❌ Technical implementation details

### ALWAYS Ask Users For
- ✅ Which service to use ("Gmail or SendGrid?")
- ✅ Business data (email recipients, subreddit names)
- ✅ Workflow logic (what to fetch, what to send)
- ✅ API credentials via environment variables

### Step 7: Generate Configuration Instructions

After generating all code files, you MUST provide configuration instructions:

1. **Identify all helpers used** in the workflow (from activities, imports, injections)
2. **Look up required env vars** from the Helper → Required Environment Variables table above
3. **Generate a `.env` snippet** with ONLY the variables this workflow needs
4. **Include setup instructions** for each service (e.g., "Create Gmail App Password")

**Output format** (include in your response after the code):

```
## Configuration

Your workflow uses: [list helpers]

Add these to your `.env` file (create from `.env.example` if it doesn't exist):

\`\`\`env
# === Required for this workflow ===
GEMINI_API_KEY=your-google-ai-key
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
\`\`\`

### Setup steps:
1. **LLM (Gemini)**: Get API key from https://aistudio.google.com/apikey
2. **Email (Gmail)**: Enable 2FA → Google Account > Security > App Passwords > Mail

Then run:
\`\`\`bash
cp .env.example .env   # if first time
# Edit .env with your values
mvn spring-boot:run
\`\`\`

The ConfigurationValidator will confirm all helpers are ready on startup.
```

**Rules**:
- NEVER generate env vars for helpers the workflow does NOT use
- ALWAYS deduplicate: if 3 AI helpers are used, list LLM vars only ONCE
- ALWAYS include the setup steps for services that need account creation (Gmail App Password, Telegram BotFather, etc.)
- If the workflow only uses RedditHelper, explicitly say "No configuration needed"

---

## Common Patterns

### Pattern 1: Single Existing Helper
```
User: "Fetch top Reddit posts"
Helpers: RedditHelper (exists, use it)
Files: Workflow + Activities (no new helpers)
```

### Pattern 2: Configure Existing Helper
```
User: "Email me daily report"
Helpers: EmailHelper (exists, needs SMTP config)
Action: Configure application.yml with user's SMTP
Files: Workflow + Activities (no new helpers)
```

### Pattern 3: Implement Skeleton
```
User: "Post to Slack"
Helpers: SlackHelper (skeleton exists)
Action: Implement SlackHelper.sendMessage()
Files: SlackHelper + Workflow + Activities
```

### Pattern 4: Create Workflow Helper
```
User: "Post to Twitter"
Helpers: None (Twitter specific to this workflow)
Action: Create TwitterHelper in workflows/social/helpers/
Files: TwitterHelper + Workflow + Activities
```

### Pattern 5: Create Framework Helper
```
User: "Create Notion page" (+ future workflows will use Notion)
Helpers: None (but will be reused)
Action: Create NotionHelper in helpers/productivity/
Files: NotionHelper + Workflow + Activities
```

### Pattern 6: LLM-Enhanced Workflow
```
User: "Summarize Reddit posts before sending email"
Helpers: RedditHelper (exists) + TextSummarizerHelper (exists) + EmailHelper (needs config)
Action: Compose all three in activities — fetch → summarize → send
Files: Workflow + Activities (no new helpers needed)
```

### Pattern 7: Sentiment-Filtered Workflow
```
User: "Monitor Reddit and alert only on negative posts"
Helpers: RedditHelper + SentimentAnalyzerHelper + TelegramHelper
Action: Fetch → Analyze sentiment → Filter negative → Send alert
Files: Workflow + Activities (no new helpers needed)
```

### Pattern 8: Multi-Language Workflow
```
User: "Summarize posts in Portuguese and send to Telegram"
Helpers: RedditHelper + TextSummarizerHelper (summarizeToLanguage) + TelegramHelper
Action: Fetch → Summarize to pt-BR → Send
Files: Workflow + Activities (no new helpers needed)
```

---

## Response Template

When user requests workflow:

```
I'll create a [purpose] workflow. Here's my analysis:

**Services Required:**
- [Service 1]: [Helper decision and reasoning]
- [Service 2]: [Helper decision and reasoning]

**Helper Strategy:**
- Using existing: [list]
- Implementing skeleton: [list]
- Creating workflow helper: [list with justification]
- Creating framework helper: [list with justification]

**Files to Create:**
1. [List all files including helpers]

[Then show code for each file]

## Configuration

Your workflow uses: [Helper1, Helper2, Helper3]

Add these to your `.env` file:

\`\`\`env
# === Required for this workflow ===
[ONLY the env vars needed by the helpers above]
\`\`\`

### Setup steps:
[Numbered list with links to get each credential]

Then run:
\`\`\`bash
cp .env.example .env   # if first time
# Edit .env with your values
mvn spring-boot:run
\`\`\`
```

---

## Critical Rules

### DO ✅
- Create helpers when needed (don't assume they exist)
- Follow BaseHelper pattern religiously
- Put workflow-specific helpers in workflow package
- Put reusable helpers in helpers/[category]/
- Use executeWithProtection for ALL I/O
- Ask users which service to use
- Configure via environment variables

### DON'T ❌
- Assume services are pre-configured
- Create framework helper for single-use service
- Create workflow helper for widely-used service
- Hardcode credentials
- Put I/O in workflows
- Skip @Component on helpers
- Forget executeWithProtection

---

## Final Checklist

Before completing workflow:

- [ ] All needed helpers exist (created or implemented)
- [ ] All helpers extend BaseHelper
- [ ] All helpers have @Component
- [ ] Helper location correct (framework vs workflow)
- [ ] Maven dependencies added (if new libraries)
- [ ] Worker configured in application.yml
- [ ] No hardcoded secrets
- [ ] Code compiles
- [ ] `.env` snippet provided with ONLY the required variables for this workflow
- [ ] Setup steps provided for each service requiring credentials
- [ ] Configuration instructions reference `.env.example` as starting point
