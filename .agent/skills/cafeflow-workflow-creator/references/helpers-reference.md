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

**Need custom API?** → Workflow helper if single-use, framework if reusable
