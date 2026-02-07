# Reddit to Email Example

This is a complete example of a CafeFlow automation demonstrating:
- Use of Core Helpers (Reddit, Email, OpenAI)
- Configuration of workflows and activities
- Activities with dependency injection
- Schedule setup in the application main class

## Files
- `RedditAutomationWorkflow.java`: Workflow interface
- `RedditAutomationWorkflowImpl.java`: Workflow logic orchestrating activities
- `RedditActivities.java`: Activities interface
- `RedditActivitiesImpl.java`: Implementation of activities using Helpers

## How to use this example
To reactivate this example, copy the contents of `workflows/` back to `src/main/java/com/cafeflow/workflows/reddit/` and register the worker in `application.yml`.

## Key Patterns Demonstrated
- **Helper Injection**: Using `@Autowired` to bring in specialized helpers.
- **Fail-Fast**: Using the `core` exception handling.
- **DRY**: Reusing `EmailHelper` for notifications.
