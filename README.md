# â˜• CafeFlow

CafeFlow is a modern, enterprise-ready template for building robust automations using **Java 21**, **Spring Boot 3**, and **Temporal.io**.

## Project Structure
```text
CafeFlow/
â”œâ”€â”€ src/
â”‚   â”œâ”€â”€ main/java/com/cafeflow/
â”‚   â”‚   â”œâ”€â”€ core/           # Framework base classes and exceptions
â”‚   â”‚   â”œâ”€â”€ helpers/        # Reusable integrations (Reddit, Email, etc.)
â”‚   â”‚   â”œâ”€â”€ workflows/      # Your automation logic (Template)
â”‚   â”‚   â””â”€â”€ CafeFlowApplication.java
â”‚   â””â”€â”€ main/resources/
â”‚       â””â”€â”€ application.yml
â”œâ”€â”€ examples/               # Reference implementations
â””â”€â”€ docker-compose.yml
```

## Quick Start
Check our [QUICKSTART.md](QUICKSTART.md) for a step-by-step guide.

## Available Helpers
- âœ… **EmailHelper**: SMTP integration for notifications.
- âœ… **RedditHelper**: API integration for data fetching.
- âœ… **OpenAIHelper**: LLM integration for content processing.
- âœ… **DatabaseHelper**: PostgreSQL abstraction using KISS principle.
- ðŸ”„ **TODO**: Google Drive, Slack, LinkedIn.

## Creating Your First Automation
1. Define a Workflow Interface (`@WorkflowInterface`).
2. Implement the Workflow.
3. Define and Implement Activities using Helpers.
4. Register the Worker in `application.yml`.
5. (Optional) Setup a Schedule in `CafeFlowApplication`.

## Key Design Patterns
- **BaseHelper Pattern**: Consistent error handling and logging.
- **Helper Composition**: Mix and match helpers (e.g., Reddit + OpenAI + Email).
- **KISS & DRY**: Code that is easy to read and maintain.

## Running the Project
```bash
# 1. Start infra (Temporal + Postgres)
docker-compose up -d

# 2. Run the application
mvn spring-boot:run
```

## Environment Variables
- `SMTP_USERNAME` / `SMTP_PASSWORD`: For email notifications.
- `REDDIT_CLIENT_ID` / `REDDIT_SECRET`: For Reddit integration.

## License
MIT
