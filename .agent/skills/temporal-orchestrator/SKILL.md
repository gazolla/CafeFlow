---
name: temporal-orchestrator
description: Advanced Temporal patterns — signals, queries, side effects, testing.
---

# Temporal Orchestrator — Advanced Patterns

> This skill covers ADVANCED Temporal patterns only.
> For standard workflow creation (interface, impl, activities), use `cafeflow-workflow-creator`.

---

## Signals and Queries

Use `@SignalMethod` for external input during workflow execution.
Use `@QueryMethod` to read workflow state without affecting it.

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

        while (true) {
            Workflow.await(Duration.ofDays(30), () -> false);

            try {
                processBilling(userEmail, paymentMethodId);
            } catch (Exception e) {
                status = "PAYMENT_FAILED";
                boolean signaled = Workflow.await(Duration.ofDays(3), () -> !status.equals("PAYMENT_FAILED"));
                if (!signaled) break;
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

---

## Side Effects

Use `Workflow.sideEffect` for non-deterministic code (e.g., UUID generation) that doesn't need the weight of an activity.

```java
String traceId = Workflow.sideEffect(String.class, () -> UUID.randomUUID().toString());
```

---

## UUID-Based Workflow IDs

Always use unique workflow IDs to avoid collisions with previous (terminated/failed) workflows.

> **WARNING**: Reusing a workflow ID that was terminated/failed will cause errors.

```java
@Bean
@Profile("!test")
public CommandLineRunner runner(WorkflowClient workflowClient) {
    return args -> {
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

---

## Integration Testing

Use `TestWorkflowEnvironment` with `@SpringBootTest` to verify workflows without a running Temporal server.

**Test config** (`src/test/resources/application-test.yml`):
```yaml
spring:
  temporal:
    test-server:
      enabled: true
```

**Test class:**
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
    private OrderActivities orderActivities;

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

> **TIP**: Exclude `CommandLineRunner` beans in tests with `@Profile("!test")`.

---

## Activity Isolation Testing

Test activities without a full workflow using `TestActivityEnvironment`.

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

---

## References
- See `examples/` for complete Java implementations.
- See `references/temporal-determinism-rules.md` for determinism constraints.
