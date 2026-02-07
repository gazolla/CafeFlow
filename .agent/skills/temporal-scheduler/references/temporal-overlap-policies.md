# Temporal Overlap Policies

When a schedule is triggered but a previous run of the same schedule is still executing, the **Overlap Policy** determines the behavior.

## Available Policies

### 1. SCHEDULE_OVERLAP_POLICY_SKIP (Default)
Do not start a new run. This is the safest and most common policy for cleanup or synchronization tasks.

### 2. SCHEDULE_OVERLAP_POLICY_BUFFER_ONE
Buffer exactly one run to start immediately after the current one finishes. If another trigger occurs while one is already buffered, it is skipped.

### 3. SCHEDULE_OVERLAP_POLICY_BUFFER_ALL
Buffer all missed runs and start them sequentially as the previous ones finish. **Warning**: This can lead to a massive backlog if the runs take longer than the interval.

### 4. SCHEDULE_OVERLAP_POLICY_CANCEL_OTHER
Cancel the currently running execution and start the new one immediately.

### 5. SCHEDULE_OVERLAP_POLICY_TERMINATE_OTHER
Force-terminate the currently running execution and start the new one. Use with caution.

### 6. SCHEDULE_OVERLAP_POLICY_ALLOW_ALL
Allow multiple executions to run concurrently.

## Implementation in Java

```java
SchedulePolicy.newBuilder()
    .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP)
    .build();
```
