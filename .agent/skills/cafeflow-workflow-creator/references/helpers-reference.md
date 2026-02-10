# Helpers Reference — Method Signatures

> Last synced with source code: 2025-02. To verify: check `src/main/java/com/cafeflow/helpers/`

---

## Implemented Helpers — Full Method Signatures

### RedditHelper (`helpers/marketing/`)
```java
<T> List<T> fetchTopPosts(String subreddit, int limit, Class<T> responseType)
```
No env vars needed. Public Reddit JSON API. Always pass your DTO record as `responseType`.

### EmailHelper (`helpers/communication/`)
```java
void sendTextEmail(String to, String subject, String body)
```
Env: `SMTP_USERNAME`, `SMTP_PASSWORD`. Optional: `SMTP_HOST` (default: smtp.gmail.com), `SMTP_PORT` (default: 587).

### TelegramHelper (`helpers/communication/`)
```java
void sendMessage(String chatId, String text)
void sendMessageWithInlineMenu(String chatId, String text, Map<String, String> buttons)
void editMessage(String chatId, String messageId, String newText)
void sendNotification(String chatId, String text)
```
Env: `TELEGRAM_BOT_TOKEN`.

### TwitterHelper (`helpers/marketing/`)
```java
String getUserByUsername(String username)
String postTweet(String text)
```
Env: `X_BEARER_TOKEN`. Optional: `X_API_KEY`, `X_API_SECRET`.

### GDriveHelper (`helpers/office/google/`)
```java
List<File> listFiles(int pageSize)
File uploadFile(String name, String mimeType, AbstractInputStreamContent content)
InputStream downloadFile(String fileId)
```
Env: `GD_CREDENTIALS_PATH`. Uses OAuth2 with `credentials.json`.

### TextSummarizerHelper (`helpers/ai/`)
```java
String summarize(String text)
String summarize(String text, int maxSentences)
List<String> summarizeBatch(List<String> texts)
String summarizeToLanguage(String text, String targetLanguage)
```

### SentimentAnalyzerHelper (`helpers/ai/`)
```java
SentimentResult analyze(String text)
List<SentimentResult> analyzeBatch(List<String> texts)
String classifySimple(String text)
```

### TextTranslatorHelper (`helpers/ai/`)
```java
String translate(String text, String targetLanguage)
String translate(String text, String sourceLanguage, String targetLanguage)
List<String> translateBatch(List<String> texts, String targetLanguage)
String detectLanguage(String text)
```

### ContentGeneratorHelper (`helpers/ai/`)
```java
String generate(String instruction)
String generateEmail(String topic, String recipientContext, List<String> keyPoints)
String generateSocialPost(String platform, String topic, String tone)
String generateTweet(String content)
String generateReport(String title, List<String> dataPoints)
String rewriteInTone(String text, String tone)
```

### DataExtractorHelper (`helpers/ai/`)
```java
ExtractionResult extractFields(String text, List<String> fieldNames)
List<String> extractEntities(String text)
Map<String, String> extractKeyValues(String text)
List<String> extractActionItems(String text)
```

### TextClassifierHelper (`helpers/ai/`)
```java
ClassificationResult classify(String text, List<String> categories)
List<ClassificationResult> classifyBatch(List<String> texts, List<String> categories)
String classifySimple(String text, List<String> categories)
boolean classifyBoolean(String text, String question)
boolean matchesCriteria(String text, String criteria)
```

### TopicExtractorHelper (`helpers/ai/`)
```java
List<String> extractTopics(String text)
List<List<String>> extractTopicsBatch(List<String> texts)
List<String> generateHashtags(String text, int maxHashtags)
List<String> extractKeywords(String text, int maxKeywords)
String generateTopicLabel(String text)
```

**All AI helpers require**: `GEMINI_API_KEY` or `GROQ_API_KEY`.

---

## AI Helper Return Types

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record SentimentResult(String sentiment, double confidence, String explanation) {}

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClassificationResult(String category, double confidence, String reasoning) {}

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractionResult(Map<String, String> fields, List<String> entities, double confidence) {}
```

---

## Environment Variables Reference

| Helper | Required | Optional |
|--------|----------|----------|
| RedditHelper | *none* | — |
| EmailHelper | `SMTP_USERNAME`, `SMTP_PASSWORD` | `SMTP_HOST`, `SMTP_PORT` |
| TelegramHelper | `TELEGRAM_BOT_TOKEN` | — |
| TwitterHelper | `X_BEARER_TOKEN` | `X_API_KEY`, `X_API_SECRET` |
| GDriveHelper | `GD_CREDENTIALS_PATH` | `GD_APP_NAME`, `GD_TOKENS_DIR` |
| All AI Helpers | `GEMINI_API_KEY` **or** `GROQ_API_KEY` | `LLM_DEFAULT_PROVIDER`, `GEMINI_MODEL`, `GROQ_MODEL` |

---

## Skeleton Helpers (exist but need implementation)

| Helper | Package | Expected Integration |
|--------|---------|---------------------|
| SlackHelper | `office/` | Slack Webhooks/API |
| DiscordHelper | `communication/` | Discord Webhooks |
| WhatsAppHelper | `communication/` | WhatsApp Business API |
| GitHubHelper | `development/` | GitHub REST API v3 |
| GmailHelper | `office/google/` | Gmail API (not SMTP) |
| GDocsHelper | `office/google/` | Google Docs API |
| GSheetsHelper | `office/google/` | Google Sheets API |
| GCalendarHelper | `office/google/` | Google Calendar API |
| PostgreSQLHelper | `devops/database/` | JDBC/Spring Data |
| NotionHelper | `office/` | Notion API |
| AsanaJiraHelper | `office/` | Asana/Jira REST API |
| FacebookHelper | `marketing/` | Facebook Graph API |
| InstagramHelper | `marketing/` | Instagram Graph API |
| LinkedInHelper | `marketing/` | LinkedIn API |
| MS365Helper | `office/microsoft/` | Microsoft 365 API |
| MSGraphHelper | `office/microsoft/` | Microsoft Graph API |
| MSOutlookHelper | `office/microsoft/` | Outlook REST API |
