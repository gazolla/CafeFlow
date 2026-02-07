---
name: temporal-orchestrator
description: Implementation of Temporal Workflows and Activities in Java with Spring Boot.
---

# Temporal Orchestrator Skill

This skill provides patterns and instructions for implementing durable workflow orchestration using the Temporal Java SDK, integrated with Spring Boot using the `temporal-spring-boot-starter`.

## Core Concepts

- **Workflows**: Durable, stateful functions that orchestrate activities.
- **Activities**: Stateless units of work that interact with external systems.
- **Workers**: Processes that host workflow and activity implementations.
- **Task Queues**: Named queues where workflows and activities are dispatched.

## Instructions

### 1. Dependency Configuration
Ensure the `temporal-spring-boot-starter` is present in your `pom.xml`:

```xml
<dependency>
    <groupId>io.temporal</groupId>
    <artifactId>temporal-spring-boot-starter-alpha</artifactId>
    <version>1.24.0</version>
</dependency>
```

### 2. Spring Properties
Configure Temporal in `application.yml`:

```yaml
spring:
  temporal:
    connection:
      target: localhost:7233
    workers:
      - name: "ORDER_PROCESSING_QUEUE"
        task-queue: "ORDER_PROCESSING_QUEUE"
        capacity:
          max-concurrent-workflow-task-pollers: 5
```

> [!TIP]
> **Auto-Discovery**: The starter automatically registers classes annotated with `@WorkflowImpl` and `@ActivityImpl` (if they are Spring beans). Explicit registration in `application.yml` (`workflow-classes`, `activity-classes`) is usually NOT required and can cause conflicts. Ensure your worker `name` matches the `workers` attribute in the annotations.

> [!CAUTION]
> **DO NOT use `@Component` on workflow implementations.** Workflow classes are managed by Temporal, not Spring. Using `@Component` causes: `java.lang.Error: Called from non workflow or workflow callback thread`.

> [!IMPORTANT]
> **Always explicitly register workflow classes** in `application.yml` using `workflow-classes`. The `@WorkflowImpl` annotation alone may not be sufficient for auto-discovery.

### 3. Implementing a Workflow
Workflows must have an interface and an implementation.

**Interface:**
```java
@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String processOrder(OrderDTO order);
}
```

**Implementation:**
```java
@WorkflowImpl(workers = "ORDER_PROCESSING_QUEUE")
public class OrderWorkflowImpl implements OrderWorkflow {
    
    private final OrderActivities activities = Workflow.newActivityStub(
        OrderActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(5)
                .build())
            .build()
    );

    @Override
    public String processOrder(OrderDTO order) {
        activities.reserveInventory(order);
        activities.processPayment(order);
        activities.shipOrder(order);
        return "COMPLETED";
    }
}
```

### 4. Implementing Activities
Activities **must** have an interface and an implementation to allow for proper mocking and testing.

**Interface:**
```java
@ActivityInterface
public interface OrderActivities {
    @ActivityMethod
    void reserveInventory(OrderDTO order);
    
    @ActivityMethod
    void processPayment(OrderDTO order);
    
    @ActivityMethod
    void shipOrder(OrderDTO order);
}
```

**Implementation:**
Activities should be Spring beans (`@Component` or `@Service`) to leverage dependency injection.

> [!TIP]
> **Use `@Component` on Activity implementations.** Unlike Workflows, Activities are standard Java objects managed by Spring and can use dependency injection.

```java
@Component
@ActivityImpl(workers = "ORDER_PROCESSING_QUEUE")
public class OrderActivitiesImpl implements OrderActivities {
    
    private final InventoryClient inventoryClient;

    public OrderActivitiesImpl(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @Override
    public void reserveInventory(OrderDTO order) {
        inventoryClient.reserve(order.getId());
    }
    
    @Override
    public void processPayment(OrderDTO order) {
        // Implementation
    }
    
    @Override
    public void shipOrder(OrderDTO order) {
        // Implementation
    }
}
```

> [!WARNING]
> **Activity visibility:** Ensure your Activity implementation classes are `public` so Temporal can discover and register them.

## Advanced Patterns & Examples

### Workflow with Search Attributes and Signals
Useful for long-running processes that need external input.

```java
@WorkflowInterface
public interface SubscriptionWorkflow {
    @WorkflowMethod
    void startSubscription(String userEmail);

    @SignalMethod
    void updatePaymentMethod(String newMethodId);

    @QueryMethod
    String getStatus();
}

@WorkflowImpl(workers = "SUBSCRIPTION_QUEUE")
public class SubscriptionWorkflowImpl implements SubscriptionWorkflow {
    
    private String status = "INITIALIZING";
    private String paymentMethodId = "default";

    @Override
    public void startSubscription(String userEmail) {
        status = "ACTIVE";
        
        // Loop for monthly billing
        while (true) {
            Workflow.await(Duration.ofDays(30), () -> false); // Wait 30 days
            
            try {
                processBilling(userEmail, paymentMethodId);
            } catch (Exception e) {
                status = "PAYMENT_FAILED";
                // Wait for signal or timeout
                boolean signaled = Workflow.await(Duration.ofDays(3), () -> !status.equals("PAYMENT_FAILED"));
                if (!signaled) break; // Cancel sub if not fixed in 3 days
            }
        }
        status = "CANCELLED";
    }

    @Override
    public void updatePaymentMethod(String newMethodId) {
        this.paymentMethodId = newMethodId;
        if (this.status.equals("PAYMENT_FAILED")) {
            this.status = "ACTIVE";
        }
    }

    @Override
    public String getStatus() { return status; }
}
```

### Side Effects
Use `Workflow.sideEffect` for non-deterministic code that doesn't need the full weight of an activity (e.g., generating a UUID).

```java
String traceId = Workflow.sideEffect(String.class, () -> UUID.randomUUID().toString());
```

### UUID-Based Workflow IDs
Always use unique workflow IDs to avoid collisions with previous (terminated/failed) workflows.

> [!WARNING]
> **Reusing a workflow ID that was terminated/failed will cause errors** when starting a new workflow with the same ID.

```java
@Bean
public CommandLineRunner runner(WorkflowClient workflowClient) {
    return args -> {
        // Use UUID to ensure unique workflow ID per execution
        String workflowId = "order-workflow-" + UUID.randomUUID().toString().substring(0, 8);
        
        OrderWorkflow workflow = workflowClient.newWorkflowStub(
            OrderWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue("ORDER_PROCESSING_QUEUE")
                .build());
        
        workflow.processOrder(order);
    };
}
```

### 6. Integration Testing
Use `TestWorkflowEnvironment` with `@SpringBootTest` to verify workflows without a running server.

**Dependencies needed (`pom.xml`):**
```xml
<dependency>
    <groupId>io.temporal</groupId>
    <artifactId>temporal-testing</artifactId>
    <scope>test</scope>
</dependency>
```

**Test Configuration (`src/test/resources/application-test.yml`):**
```yaml
spring:
  temporal:
    test-server:
      enabled: true
```

**Test Class Pattern:**
```java
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderWorkflowIntegrationTest {

    @Autowired
    private TestWorkflowEnvironment testEnv;

    @Autowired
    private WorkflowClient workflowClient;

    @MockBean
    private OrderActivities orderActivities; // Mock activities to isolate workflow logic

    @BeforeEach
    void setUp() {
        testEnv.start();
        when(orderActivities.checkInventory(any())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void testOrderWorkflow() {
        OrderWorkflow workflow = workflowClient.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue("ORDER_QUEUE").build());

        String result = workflow.processOrder(new OrderDTO("123"));
        assertEquals("COMPLETED", result);
    }
}
```

> [!TIP]
> **Exclude Runners in Tests**: If you have a `CommandLineRunner` that starts a workflow, exclude it from tests using `@Profile("!test")` on the bean definition to avoid conflicts.

### 7. Testing Activities Isolated
You can also test activities in isolation using `TestActivityEnvironment`.

```java
@ExtendWith(MockitoExtension.class)
class OrderActivitiesTest {
    
    @Mock
    private InventoryClient inventoryClient;
    
    private TestActivityEnvironment testEnv;
    private OrderActivities activities;

    @BeforeEach
    void setUp() {
        testEnv = TestActivityEnvironment.newInstance();
        // Register the implementation with injected mocks
        testEnv.registerActivitiesImplementations(new OrderActivitiesImpl(inventoryClient));
        activities = testEnv.newActivityStub(OrderActivities.class);
    }

    @Test
    void testReserveInventory() {
        OrderDTO order = new OrderDTO("123");
        activities.reserveInventory(order);
        verify(inventoryClient).reserve("123");
    }
}
```

## References
- See `examples/` for complete Java implementations (Order, Subscriptions).
- See `references/temporal-spring-boot-dependencies.xml` for `pom.xml` configuration.
- See `references/temporal-determinism-rules.md` for critical constraints.
