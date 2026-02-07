package io.temporal.example;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.time.Duration;

/**
 * Example of a standard Order Processing Workflow.
 */
@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String processOrder(OrderDTO order);
}

class OrderWorkflowImpl implements OrderWorkflow {
    
    // Activities must be accessed via stubs in the workflow implementation
    private final OrderActivities activities = Workflow.newActivityStub(
        OrderActivities.class,
        ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setMaximumAttempts(5)
                .build())
            .build()
    );

    @Override
    public String processOrder(OrderDTO order) {
        // Workflow logic must be deterministic
        activities.reserveInventory(order);
        activities.processPayment(order);
        activities.shipOrder(order);
        
        return "Order " + order.id() + " processed successfully";
    }
}

interface OrderActivities {
    void reserveInventory(OrderDTO order);
    void processPayment(OrderDTO order);
    void shipOrder(OrderDTO order);
}

record OrderDTO(String id, String customerId, java.math.BigDecimal amount) {}
