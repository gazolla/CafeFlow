# QUICKSTART Guide

## Prerequisites
- Java 21+
- Maven
- Docker & Docker Compose

## Step 1: Start Infrastructure
```bash
docker-compose up -d
```

## Step 2: Configure Environment
Create a `.env` file or set variables in your IDE:
```env
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

## Step 3: Create Your First Workflow
Create `HelloWorkflow.java`:
```java
@WorkflowInterface
public interface HelloWorkflow {
    @WorkflowMethod
    void sayHello(String name);
}
```

## Step 4: Configure Worker
Add to `application.yml`:
```yaml
spring:
  temporal:
    workers:
      - task-queue: "HELLO_QUEUE"
        workflow-classes:
          - com.cafeflow.workflows.HelloWorkflowImpl
```

## Step 5: Add Schedule (Optional)
Uncomment the `scheduleRunner` bean in `CafeFlowApplication.java`.

## Step 6: Monitor
Access Temporal Web UI at [http://localhost:8080](http://localhost:8080).

## Troubleshooting
- **Temporal not connecting**: Ensure docker-compose is running.
- **Maven errors**: Check if Java 21 is set as default.
