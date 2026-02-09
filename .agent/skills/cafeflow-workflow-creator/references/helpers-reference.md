# Helpers Reference

## Understanding CafeFlow Helpers

### Critical Concept: Helpers Are NOT Pre-Configured Services

**IMPORTANT**: CafeFlow provides helper CLASSES, not pre-configured SERVICES.

Think of helpers as **templates** or **patterns**, not ready-to-use integrations.

---

## Framework Helpers Status

### ✅ Fully Implemented (No Configuration Needed)

#### RedditHelper
**Location**: `helpers/marketing/RedditHelper.java`

**Status**: Fully functional - uses public Reddit JSON API

**Methods**:
```java
<T> List<T> fetchTopPosts(String subreddit, int limit, Class<T> responseType)
```

**Example**:
```java
List<RedditPost> posts = redditHelper.fetchTopPosts("java", 10, RedditPost.class);
```

**Configuration**: NONE - uses public API

---

### ⚙️ Implemented But Requires Configuration

#### EmailHelper
**Location**: `helpers/communication/EmailHelper.java`

**Status**: Class exists, user MUST configure SMTP

**Methods**:
```java
void sendTextEmail(String to, String subject, String body)
```

**Required Configuration** (`application.yml`):
```yaml
spring:
  mail:
    host: smtp.gmail.com           # User provides
    port: 587                       # User provides
    username: ${SMTP_USERNAME}      # User provides via env var
    password: ${SMTP_PASSWORD}      # User provides via env var
```

**When to Use**:
- All workflows use SAME email provider
- Shared SMTP server across project

**When NOT to Use**:
- Different email providers per workflow → Create workflow-specific helper

**Example Email Providers**:

Gmail:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
```

Outlook:
```yaml
spring:
  mail:
    host: smtp.office365.com
    port: 587
```

SendGrid:
```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: "apikey"
    password: ${SENDGRID_API_KEY}
```

---

### 📝 Skeleton Helpers (Need Implementation)

These helpers exist as EMPTY classes with TODO comments. You must implement them if needed.

#### SlackHelper
**Location**: `helpers/communication/SlackHelper.java`

**Status**: Skeleton only

**What to Implement**:
```java
@Component
public class SlackHelper extends BaseHelper {
    @Value("${slack.webhook.url}")
    private String webhookUrl;
    
    @Override
    protected String getServiceName() {
        return "slack";
    }
    
    public void sendMessage(String channel, String message) {
        executeWithProtection("sendMessage", () -> {
            // TODO: Implement Slack webhook POST
        });
    }
}
```

**Configuration**:
```yaml
slack:
  webhook:
    url: ${SLACK_WEBHOOK_URL}
```

---

#### GitHubHelper
**Location**: `helpers/development/GitHubHelper.java`

**Status**: Skeleton only

**What to Implement**:
```java
public void createIssue(String owner, String repo, String title, String body)
public List<Issue> getIssues(String owner, String repo)
public List<PullRequest> getPullRequests(String owner, String repo)
```

**Configuration**:
```yaml
github:
  token: ${GITHUB_TOKEN}
```

---

#### GmailHelper
**Location**: `helpers/office/google/GmailHelper.java`

**Status**: Skeleton only

**Note**: Different from EmailHelper - uses Gmail API, not SMTP

---

#### GDriveHelper
**Location**: `helpers/office/google/GDriveHelper.java`

**Status**: Skeleton only

---

#### PostgreSQLHelper
**Location**: `helpers/devops/database/PostgreSQLHelper.java`

**Status**: Skeleton only

---

## When to Use Which Strategy

### Use Existing Framework Helper

**Scenario**: Helper exists and is implemented OR you'll configure it globally

**Example**:
```
User: "Fetch top Reddit posts"
Helper: RedditHelper (exists, works as-is)
Action: Just inject and use
```

```java
@RequiredArgsConstructor
public class MyActivities {
    private final RedditHelper redditHelper;  // Use existing
}
```

---

### Configure Existing Framework Helper

**Scenario**: EmailHelper exists, but you need to configure SMTP

**When**:
- All workflows will use same email provider
- Shared SMTP configuration

**Example**:
```
User: "Email me daily report via Gmail"
Helper: EmailHelper (exists, needs Gmail SMTP config)
Action: Add spring.mail.* to application.yml
```

**Steps**:
1. Ask user which email provider
2. Add configuration to `application.yml`
3. Set environment variables
4. Inject EmailHelper normally

---

### Implement Skeleton Helper

**Scenario**: Skeleton exists, multiple workflows will use it

**When**:
- Helper skeleton file exists
- Service will be reused across workflows
- Generic integration (not workflow-specific)

**Example**:
```
User: "Post to Slack when build completes"
Helper: SlackHelper (skeleton exists)
Action: Implement SlackHelper methods
Decision: Multiple workflows might need Slack
```

**Steps**:
1. Open `helpers/communication/SlackHelper.java`
2. Implement methods following BaseHelper pattern
3. Add configuration to `application.yml`
4. Use in any workflow

---

### Create New Framework Helper

**Scenario**: No helper exists, but multiple workflows will use this service

**When**:
- Service doesn't have a helper (not even skeleton)
- Multiple workflows will need it
- Generic service integration

**Example**:
```
User: "Store data in Notion database"
Helper: None exists
Action: Create NotionHelper in helpers/productivity/
Decision: Other workflows might use Notion too
```

**Steps**:
1. Create `helpers/[category]/ServiceHelper.java`
2. Extend BaseHelper
3. Implement service methods
4. Add configuration to `application.yml`
5. Add Maven dependencies if needed

---

### Create Workflow-Specific Helper

**Scenario**: Service only used by ONE workflow OR needs different config than framework helper

**When**:
- Service is workflow-specific (Twitter, Stripe, custom API)
- Different configuration than framework helper
- Example: EmailHelper uses Gmail, but this workflow needs SendGrid

**Example**:
```
User: "Post to Twitter"
Helper: None exists
Action: Create TwitterHelper in workflows/social/helpers/
Decision: Only social workflow needs Twitter
```

```
User: "Send newsletter via SendGrid"
Helper: EmailHelper exists but uses Gmail
Action: Create SendGridEmailHelper in workflows/newsletter/helpers/
Decision: Different email provider than default
```

**Steps**:
1. Create `workflows/[name]/helpers/ServiceHelper.java`
2. Extend BaseHelper
3. Configure in constructor via `System.getenv()`
4. Add `@Component` annotation
5. Inject into activities

---

## Decision Matrix

| Situation | Strategy | Location | Configuration |
|-----------|----------|----------|---------------|
| RedditHelper | Use existing | helpers/marketing/ | None needed |
| Email (all same SMTP) | Configure EmailHelper | helpers/communication/ | application.yml |
| Email (different per workflow) | Create workflow helper | workflows/[name]/helpers/ | Constructor |
| Slack (multiple workflows) | Implement skeleton | helpers/communication/ | application.yml |
| Twitter (single workflow) | Create workflow helper | workflows/[name]/helpers/ | Constructor |
| Notion (future reuse) | Create framework helper | helpers/productivity/ | application.yml |
| Custom API (one workflow) | Create workflow helper | workflows/[name]/helpers/ | Constructor |

---

## Helper Implementation Template

### Framework Helper Template

```java
package com.cafeflow.helpers.[category];

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceHelper extends BaseHelper {
    
    @Value("${service.api.key}")
    private String apiKey;
    
    @Value("${service.api.url}")
    private String apiUrl;

    @Override
    protected String getServiceName() {
        return "service-name";
    }

    public SomeResult doSomething(SomeParams params) {
        return executeWithProtection("doSomething", () -> {
            // Implementation here
            // All I/O must be inside executeWithProtection
        });
    }
}
```

**Configuration** (`application.yml`):
```yaml
service:
  api:
    key: ${SERVICE_API_KEY}
    url: https://api.service.com
```

---

### Workflow Helper Template

```java
package com.cafeflow.workflows.[workflow].helpers;

import com.cafeflow.core.base.BaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component  // CRITICAL: Must have @Component for injection!
public class ServiceHelper extends BaseHelper {
    
    private final String apiKey;
    private final String apiUrl;

    public ServiceHelper() {
        // Configuration via environment variables
        this.apiKey = System.getenv("WORKFLOW_SERVICE_API_KEY");
        this.apiUrl = System.getenv("WORKFLOW_SERVICE_URL");
        
        if (apiKey == null || apiUrl == null) {
            throw new IllegalStateException(
                "Missing required environment variables: " +
                "WORKFLOW_SERVICE_API_KEY, WORKFLOW_SERVICE_URL"
            );
        }
    }

    @Override
    protected String getServiceName() {
        return "workflow-service";
    }

    public SomeResult doSomething(SomeParams params) {
        return executeWithProtection("doSomething", () -> {
            // Implementation here
        });
    }
}
```

**No application.yml needed** - uses environment variables directly

---

## Common Mistakes

### ❌ Assuming EmailHelper Is Ready to Use

```java
// WRONG: Thinking EmailHelper just works
@RequiredArgsConstructor
public class MyActivities {
    private final EmailHelper emailHelper;
    
    public void send() {
        emailHelper.sendTextEmail("user@example.com", "Hi", "Body");
        // ERROR: No SMTP configured!
    }
}
```

```yaml
# CORRECT: Configure SMTP first
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
```

---

### ❌ Creating Duplicate Helpers

```java
// WRONG: Creating new helper when EmailHelper exists
public class GmailSenderHelper extends BaseHelper {
    // Duplicating EmailHelper functionality
}
```

```java
// CORRECT: Configure existing EmailHelper for Gmail
// In application.yml:
spring:
  mail:
    host: smtp.gmail.com
```

---

### ❌ Putting Workflow Helper in Wrong Location

```java
// WRONG: Workflow-specific helper in framework location
// helpers/communication/TwitterHelper.java  ❌
```

```java
// CORRECT: Workflow-specific helper in workflow package
// workflows/social/helpers/TwitterHelper.java  ✅
```

---

### ❌ Not Implementing Skeleton When It Exists

```java
// WRONG: Creating new SlackNotifier when SlackHelper skeleton exists
public class SlackNotifier extends BaseHelper {
    // Duplicating what should be in SlackHelper
}
```

```java
// CORRECT: Implement existing SlackHelper skeleton
// helpers/communication/SlackHelper.java
```

---

## Best Practices

1. **Always check if helper exists first**
   - Look in `helpers/` directory
   - Check for skeletons
   - Ask if unclear

2. **Ask users about configuration**
   - Don't assume Gmail for email
   - Ask which service they want
   - Let them provide credentials

3. **Reuse when possible**
   - Framework helpers over workflow helpers
   - Implement skeletons over creating new helpers
   - Configure existing over creating duplicates

4. **Follow the pattern**
   - Extend BaseHelper
   - Implement getServiceName()
   - Use executeWithProtection
   - Add @Component

5. **Secure configuration**
   - Never hardcode credentials
   - Always use environment variables
   - Document required env vars

---

## Quick Reference

**Need Reddit?** → Use RedditHelper (works out of the box)

**Need Email?** → Configure EmailHelper SMTP OR create workflow helper if different provider

**Need Slack?** → Implement SlackHelper skeleton (framework) OR create workflow helper

**Need Twitter?** → Create workflow helper (workflows/[name]/helpers/)

**Need Notion?** → Create framework helper (helpers/productivity/) if reusable

**Need text summarization?** → Use TextSummarizerHelper (requires LLM API key)

**Need sentiment analysis?** → Use SentimentAnalyzerHelper (requires LLM API key)

**Need translation?** → Use TextTranslatorHelper (requires LLM API key)

**Need content generation (emails, posts, reports)?** → Use ContentGeneratorHelper (requires LLM API key)

**Need data extraction (fields, entities, key-values)?** → Use DataExtractorHelper (requires LLM API key)

**Need text classification / filtering?** → Use TextClassifierHelper (requires LLM API key)

**Need topics, hashtags, keywords?** → Use TopicExtractorHelper (requires LLM API key)

**Need custom AI behavior?** → Create new AI helper in helpers/ai/ injecting LLMClient

---

### AI Helpers (LLM-Powered)

#### TextSummarizerHelper
**Location**: `helpers/ai/TextSummarizerHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
String summarize(String text)
String summarize(String text, int maxSentences)
List<String> summarizeBatch(List<String> texts)
String summarizeToLanguage(String text, String targetLanguage)
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final TextSummarizerHelper summarizerHelper;
    private final EmailHelper emailHelper;

    public void fetchAndSummarize() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("java", 5, RedditPost.class);
        List<String> titles = posts.stream().map(RedditPost::title).toList();
        List<String> summaries = summarizerHelper.summarizeBatch(titles);
        emailHelper.sendTextEmail("user@email.com", "Daily Digest", String.join("\n\n", summaries));
    }
}
```

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

#### SentimentAnalyzerHelper
**Location**: `helpers/ai/SentimentAnalyzerHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
SentimentResult analyze(String text)
List<SentimentResult> analyzeBatch(List<String> texts)
String classifySimple(String text)
```

**SentimentResult record**:
```java
record SentimentResult(String sentiment, double confidence, String explanation)
// sentiment: "positive", "negative", "neutral", or "unknown" on parse failure
// confidence: 0.0 to 1.0
// explanation: brief one-sentence reason
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final SentimentAnalyzerHelper sentimentHelper;
    private final TelegramHelper telegramHelper;

    public void alertOnNegative() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("product", 10, RedditPost.class);
        for (RedditPost post : posts) {
            SentimentResult result = sentimentHelper.analyze(post.title());
            if ("negative".equals(result.sentiment()) && result.confidence() > 0.7) {
                telegramHelper.sendNotification("CHAT_ID",
                    "Warning: Negative post detected: " + post.title() + "\n" + result.explanation());
            }
        }
    }
}
```

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

#### TextTranslatorHelper
**Location**: `helpers/ai/TextTranslatorHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
String translate(String text, String targetLanguage)
String translate(String text, String sourceLanguage, String targetLanguage)
List<String> translateBatch(List<String> texts, String targetLanguage)
String detectLanguage(String text)
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final TextTranslatorHelper translatorHelper;
    private final EmailHelper emailHelper;

    public void fetchAndTranslate() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("technology", 5, RedditPost.class);
        List<String> titles = posts.stream().map(RedditPost::title).toList();
        List<String> translated = translatorHelper.translateBatch(titles, "Portuguese");
        emailHelper.sendTextEmail("user@email.com", "Tech News PT-BR", String.join("\n", translated));
    }
}
```

**Use Cases**:
- Multi-language content workflows (translate before send)
- Language detection for routing (detect → translate → deliver)
- Bilingual newsletter generation

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

#### ContentGeneratorHelper
**Location**: `helpers/ai/ContentGeneratorHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
String generate(String instruction)
String generateEmail(String topic, String recipientContext, List<String> keyPoints)
String generateSocialPost(String platform, String topic, String tone)
String generateTweet(String content)          // enforces 280 char limit
String generateReport(String title, List<String> dataPoints)
String rewriteInTone(String text, String tone)
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final ContentGeneratorHelper contentGenerator;
    private final EmailHelper emailHelper;

    public void weeklyDigest() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("java", 10, RedditPost.class);
        List<String> dataPoints = posts.stream()
            .map(p -> p.title() + " (" + p.score() + " upvotes)")
            .toList();
        String report = contentGenerator.generateReport("Weekly Java Trends", dataPoints);
        emailHelper.sendTextEmail("team@company.com", "Weekly Java Report", report);
    }

    public void socialMediaAutomation() {
        String tweet = contentGenerator.generateTweet("New blog post about Spring Boot 3.2 features");
        String linkedIn = contentGenerator.generateSocialPost("LinkedIn", "Spring Boot 3.2", "professional");
        // Post to respective platforms...
    }
}
```

**Tone Options for `rewriteInTone()`**: `"formal"`, `"casual"`, `"professional"`, `"friendly"`, `"technical"`, etc.

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

#### DataExtractorHelper
**Location**: `helpers/ai/DataExtractorHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
ExtractionResult extractFields(String text, List<String> fieldNames)
List<String> extractEntities(String text)
Map<String, String> extractKeyValues(String text)
List<String> extractActionItems(String text)
```

**ExtractionResult record**:
```java
record ExtractionResult(
    Map<String, String> fields,    // requested field name → extracted value (empty string if not found)
    List<String> entities,         // named entities found: people, orgs, locations, dates
    double confidence)             // 0.0 to 1.0
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final DataExtractorHelper dataExtractor;
    private final EmailHelper emailHelper;

    public void processInvoice(String invoiceText) {
        // Extract specific fields
        ExtractionResult result = dataExtractor.extractFields(
            invoiceText,
            List.of("vendor_name", "invoice_number", "total_amount", "due_date")
        );
        String vendor = result.fields().get("vendor_name");
        String amount = result.fields().get("total_amount");

        // Extract key-value pairs (auto-detect)
        Map<String, String> allData = dataExtractor.extractKeyValues(invoiceText);

        // Extract action items from meeting notes
        List<String> todos = dataExtractor.extractActionItems(meetingNotes);
    }
}
```

**Use Cases**:
- Invoice/receipt parsing workflows
- Meeting notes → action items extraction
- Form data extraction from unstructured text
- Named entity recognition (people, orgs, locations)

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

#### TextClassifierHelper
**Location**: `helpers/ai/TextClassifierHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
ClassificationResult classify(String text, List<String> categories)
List<ClassificationResult> classifyBatch(List<String> texts, List<String> categories)
String classifySimple(String text, List<String> categories)
boolean classifyBoolean(String text, String question)
boolean matchesCriteria(String text, String criteria)
```

**ClassificationResult record**:
```java
record ClassificationResult(
    String category,     // chosen category (or "unknown" on parse failure)
    double confidence,   // 0.0 to 1.0
    String reasoning)    // brief one-sentence explanation
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final TextClassifierHelper classifierHelper;
    private final SlackHelper slackHelper;

    public void routeByCategory() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("technology", 20, RedditPost.class);
        List<String> categories = List.of("AI/ML", "Security", "DevOps", "Frontend", "Backend");

        for (RedditPost post : posts) {
            ClassificationResult result = classifierHelper.classify(post.title(), categories);
            if (result.confidence() > 0.7) {
                slackHelper.sendMessage("#" + result.category().toLowerCase(), post.title());
            }
        }
    }

    public void filterContent() {
        // Binary classification
        boolean isUrgent = classifierHelper.classifyBoolean(emailBody, "Is this email urgent?");
        boolean matchesTech = classifierHelper.matchesCriteria(article, "related to cloud computing");
    }
}
```

**Use Cases**:
- Content routing to different channels based on category
- Spam/relevance filtering in pipelines
- Binary yes/no decisions (urgent, relevant, actionable)
- Criteria-based content filtering

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

#### TopicExtractorHelper
**Location**: `helpers/ai/TopicExtractorHelper.java`

**Status**: Fully implemented - requires LLM API key (`GEMINI_API_KEY` or `GROQ_API_KEY`)

**Methods**:
```java
List<String> extractTopics(String text)
List<List<String>> extractTopicsBatch(List<String> texts)
List<String> generateHashtags(String text, int maxHashtags)
List<String> extractKeywords(String text, int maxKeywords)
String generateTopicLabel(String text)
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final TopicExtractorHelper topicExtractor;
    private final ContentGeneratorHelper contentGenerator;

    public void tagAndPublish() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("machinelearning", 5, RedditPost.class);
        for (RedditPost post : posts) {
            // Extract topics for categorization
            List<String> topics = topicExtractor.extractTopics(post.title());

            // Generate hashtags for social media
            List<String> hashtags = topicExtractor.generateHashtags(post.title(), 5);

            // Generate a single topic label
            String label = topicExtractor.generateTopicLabel(post.title());

            // Extract SEO keywords
            List<String> keywords = topicExtractor.extractKeywords(post.title(), 10);

            // Use in content generation
            String socialPost = contentGenerator.generateSocialPost(
                "Twitter", post.title(), "informative") + "\n" + String.join(" ", hashtags);
        }
    }
}
```

**Use Cases**:
- Auto-tagging content for categorization
- SEO keyword extraction for blog/article workflows
- Hashtag generation for social media automation
- Topic-based content routing and filtering

**Configuration**: Set `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

---

### Decision Matrix — AI Helpers

| Situation | Strategy | Helper |
|-----------|----------|--------|
| Need to summarize text | Use TextSummarizerHelper | `helpers/ai/TextSummarizerHelper` |
| Need sentiment analysis | Use SentimentAnalyzerHelper | `helpers/ai/SentimentAnalyzerHelper` |
| Need translation / language detection | Use TextTranslatorHelper | `helpers/ai/TextTranslatorHelper` |
| Need email/social/report generation | Use ContentGeneratorHelper | `helpers/ai/ContentGeneratorHelper` |
| Need to extract fields/entities from text | Use DataExtractorHelper | `helpers/ai/DataExtractorHelper` |
| Need to classify text into categories | Use TextClassifierHelper | `helpers/ai/TextClassifierHelper` |
| Need topics/hashtags/keywords | Use TopicExtractorHelper | `helpers/ai/TopicExtractorHelper` |
| Need custom LLM behavior | Create new AI helper in `helpers/ai/` injecting LLMClient | Custom |
| Need LLM in one workflow only | Create workflow helper extending BaseHelper + inject LLMClient | `workflows/[name]/helpers/` |

**Need custom API?** → Workflow helper if single-use, framework if reusable

---

## Configuration & Environment Variables

### How Configuration Works in CafeFlow

CafeFlow uses a 3-layer configuration strategy:

1. **`.env.example`** — Template with ALL possible variables (committed to git)
2. **`.env`** — User's actual values (NOT committed, in `.gitignore`)
3. **`ConfigurationValidator`** — Spring component that validates config on startup

### Setup Flow

```bash
# 1. Copy template (first time only)
cp .env.example .env

# 2. Edit .env — uncomment and fill ONLY the variables your workflow needs
#    (The .env.example has comments explaining each variable)

# 3. Run — spring-dotenv loads .env automatically
mvn spring-boot:run

# 4. Check startup log for ConfigurationValidator report:
#    ✅ RedditHelper         — ready
#    ✅ EmailHelper          — ready
#    ⚠️ TextSummarizerHelper — MISSING: GEMINI_API_KEY or GROQ_API_KEY
```

### Complete Variable Reference

| Variable | Helper(s) | Required | Default | Notes |
|----------|-----------|----------|---------|-------|
| `GEMINI_API_KEY` | All AI Helpers | Yes* | *(none)* | Google AI Studio key |
| `GEMINI_MODEL` | All AI Helpers | No | `gemini-1.5-flash` | Model override |
| `GROQ_API_KEY` | All AI Helpers | Yes* | *(none)* | Groq console key |
| `GROQ_MODEL` | All AI Helpers | No | `llama-3.3-70b-versatile` | Model override |
| `LLM_DEFAULT_PROVIDER` | All AI Helpers | No | `gemini` | `gemini` or `groq` |
| `SMTP_HOST` | EmailHelper | No | `smtp.gmail.com` | SMTP server host |
| `SMTP_PORT` | EmailHelper | No | `587` | SMTP server port |
| `SMTP_USERNAME` | EmailHelper | Yes | *(none)* | Email address |
| `SMTP_PASSWORD` | EmailHelper | Yes | *(none)* | App password |
| `TELEGRAM_BOT_TOKEN` | TelegramHelper | Yes | *(none)* | From @BotFather |
| `X_BEARER_TOKEN` | TwitterHelper | Yes | *(none)* | X/Twitter API |
| `X_API_KEY` | TwitterHelper | Yes | *(none)* | X/Twitter API |
| `X_API_SECRET` | TwitterHelper | Yes | *(none)* | X/Twitter API |
| `GD_APP_NAME` | GDriveHelper | No | `CafeFlow` | App name |
| `GD_CREDENTIALS_PATH` | GDriveHelper | Yes | `/credentials.json` | OAuth2 JSON |
| `GD_TOKENS_DIR` | GDriveHelper | No | `tokens` | Token storage |
| `SLACK_WEBHOOK_URL` | SlackHelper | Yes | *(none)* | Incoming Webhook |
| `GITHUB_TOKEN` | GitHubHelper | Yes | *(none)* | PAT token |

*\*At least one LLM provider (Gemini or Groq) must be configured for AI helpers to work.*

### spring-dotenv

CafeFlow uses `spring-dotenv` to automatically load `.env` files. No manual `export` needed:

```xml
<!-- Already in pom.xml -->
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

Just place a `.env` file in the project root and Spring Boot reads it automatically on startup.

### ConfigurationValidator

On startup, `ConfigurationValidator` scans all active helper beans and prints a report:

```
╔══════════════════════════════════════════════════════════════════╗
║              CafeFlow Configuration Report                      ║
╠══════════════════════════════════════════════════════════════════╣
║ ✅ RedditHelper         — ready (no config needed)              ║
║ ✅ EmailHelper          — ready                                 ║
║ ⚠️ TextSummarizerHelper — MISSING: GEMINI_API_KEY|GROQ_API_KEY  ║
║ ⬚  SlackHelper          — not active                           ║
╚══════════════════════════════════════════════════════════════════╝
  💡 Tip: Copy .env.example to .env and configure the missing variables.
```

- **✅ ready** — Helper bean exists and all required vars are configured
- **⚠️ MISSING** — Helper bean exists but required vars are missing (will fail at runtime)
- **⬚ not active** — Helper bean not loaded (no workflow uses it)
