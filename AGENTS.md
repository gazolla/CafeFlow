# CafeFlow — Project Rules for AI Agents

## Read First

This file contains MANDATORY rules for generating code in this project.
For detailed patterns, examples, and references, read `.agent/skills/`.

Skill triggers:
- "Create a workflow..." → `.agent/skills/cafeflow-workflow-creator/SKILL.md`
- Docker/infrastructure → `.agent/skills/docker-spring-temporal/SKILL.md`
- External APIs → `.agent/skills/external-service-integrator/SKILL.md`

## Java Rules

- Use `record` for all DTOs and value objects. NEVER use `@Data class`.
- Add `@JsonIgnoreProperties(ignoreUnknown = true)` on all records.
- Use Java text blocks (`"""`) for multi-line strings.
- Organized imports at the top — never inline fully-qualified class names in code body.
- Constructor injection via `@RequiredArgsConstructor`. Never field injection (`@Autowired`).

```java
// CORRECT — this project uses Java records for ALL DTOs. Zero exceptions.
@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
        String title,
        @JsonProperty("selftext") String selfText,
        @JsonProperty("url") String link,
        int ups,
        String author) {}

// WRONG — Lombok @Data is BANNED in this project. Zero exceptions.
// @Data public class RedditPost { ... }
```

## Temporal Rules

**WorkflowImpl classes:**
- NO `@Component`, NO `@Slf4j`, NO `@RequiredArgsConstructor`
- Use `Workflow.getLogger(this.getClass())` for logging
- NEVER do I/O, HTTP calls, file access, or database queries
- NEVER use `System.currentTimeMillis()`, `Math.random()`, `new Thread()`, `Thread.sleep()`
- Use `Workflow.currentTimeMillis()`, `Workflow.sleep()`, `Workflow.sideEffect()` instead
- Get activities via `Workflow.newActivityStub()` with explicit timeout

**Activity classes:**
- YES `@Component`, YES `@Slf4j`, YES `@RequiredArgsConstructor`
- ALL I/O happens here (API calls, email sending, database queries)
- MUST have interface (`@ActivityInterface`) + implementation (separate classes)
- Register in `application.yml` under `spring.temporal.workers[].activity-beans`

```java
// CORRECT WorkflowImpl
public class MyWorkflowImpl implements MyWorkflow {
    private static final Logger log = Workflow.getLogger(MyWorkflowImpl.class);
    private final MyActivities activities = Workflow.newActivityStub(
        MyActivities.class,
        ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofMinutes(2)).build());
}

// CORRECT ActivityImpl
@Component
@Slf4j
@RequiredArgsConstructor
public class MyActivitiesImpl implements MyActivities {
    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;
}
```

## Helper Rules

- ALL helpers extend `BaseHelper`
- ALL helpers have `@Component` and implement `getServiceName()`
- ALL I/O must be inside `executeWithProtection()`
- AI helpers inject `LLMClient` via constructor
- Helpers are NOT pre-configured — each requires environment variables

## Execution Rules

- NEVER auto-run the application (`mvn spring-boot:run`, `docker-compose up`, etc.)
- ONLY generate code and configuration files
- Let the user start Docker and run the application manually
- Verify with `mvn compile` at most — never `mvn spring-boot:run`

## Configuration Rules

- NEVER hardcode secrets. Use `${ENV_VAR:default}` in `application.yml`.
- After generating a workflow, ALWAYS output a `## Configuration` section with:
  1. Which helpers the workflow uses
  2. The required `.env` variables (ONLY what this workflow needs)
  3. Setup steps with links to obtain each credential
  4. The `cp .env.example .env` instruction
- On startup, `ConfigurationValidator` reports which helpers are ready/missing.

Example output:
```
## Configuration

Your workflow uses: RedditHelper, TextSummarizerHelper, EmailHelper

Add these to your `.env` file:
GEMINI_API_KEY=your-google-ai-key
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

Setup steps:
1. Gemini: https://aistudio.google.com/apikey
2. Gmail: Enable 2FA → Google Account > Security > App Passwords
```

## File Organization

```
src/main/java/com/cafeflow/
    workflows/[name]/           ← Workflow code + DTOs for that workflow
        MyWorkflow.java
        MyWorkflowImpl.java
        MyActivities.java
        MyActivitiesImpl.java
        MyDTO.java              ← FLAT in package. No sub-folders (no dto/, no model/).
    helpers/[category]/         ← Reusable framework helpers only
    core/                       ← Base classes, config, LLM clients
```

- Worker registration → `application.yml` under `spring.temporal.workers`
- Workflow DTOs → `workflows/[name]/` FLAT (NEVER create `dto/`, `model/` sub-folders)
- One workflow per package under `workflows/`
