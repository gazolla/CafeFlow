# Reddit to Email — Complete Example

A CafeFlow workflow that fetches top Reddit posts, summarizes them with AI, and sends a digest via email.

## Files

| File | Role |
|------|------|
| `RedditPost.java` | DTO (Java record) |
| `RedditDigestWorkflow.java` | Workflow interface (`@WorkflowInterface`) |
| `RedditDigestWorkflowImpl.java` | Workflow orchestration (`@WorkflowImpl`) |
| `RedditDigestActivities.java` | Activities interface (`@ActivityInterface`) |
| `RedditDigestActivitiesImpl.java` | Activities implementation (`@Component` + `@ActivityImpl`) |
| `RedditDigestSchedulerConfig.java` | Scheduler (`@Configuration` + `CommandLineRunner`) |

## Helpers Used

- `RedditHelper` — fetches posts (no config needed)
- `TextSummarizerHelper` — AI summarization (needs `GEMINI_API_KEY` or `GROQ_API_KEY`)
- `EmailHelper` — sends email (needs `SMTP_USERNAME`, `SMTP_PASSWORD`)

## How to Use

1. Copy files to `src/main/java/com/cafeflow/workflows/reddit/`
2. Add worker to `application.yml`:

```yaml
spring:
  temporal:
    workers:
      - name: "REDDIT_DIGEST_WORKER"
        task-queue: "REDDIT_DIGEST_WORKER"
        workflow-classes:
          - com.cafeflow.workflows.reddit.RedditDigestWorkflowImpl
        activity-beans:
          - redditDigestActivitiesImpl
```

3. Add to `.env`:

```env
GEMINI_API_KEY=your-google-ai-key
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

4. Run: `mvn spring-boot:run`

## Key Patterns

- **DTO**: `record` with `@JsonIgnoreProperties` (never `@Data`)
- **WorkflowImpl**: No `@Component`, no `@Slf4j`, uses `Workflow.getLogger()` and `Workflow.newActivityStub()`
- **ActivitiesImpl**: `@Component` + `@ActivityImpl` + `@RequiredArgsConstructor`, injects helpers
- **Scheduler**: Separate `@Configuration` class (not in CafeFlowApplication)
- **Helpers**: Reuses existing framework helpers via dependency injection
