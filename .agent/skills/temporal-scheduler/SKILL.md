---
name: temporal-scheduler
description: Patterns for managing Temporal Schedules in Java.
---

# Temporal Scheduler Skill

This skill provides patterns for creating, managing, and automating recurring workflows using Temporal Schedules in Java.

## Core Concepts

- **Schedules**: A way to start workflow executions at specific times or intervals.
- **Action**: The workflow type and arguments to be executed by the schedule.
- **Spec**: The timing specification (intervals, calendar-based, cron).
- **Policy**: Conflict resolution and overlap policies for the schedule.

## Instructions

### 1. Injecting ScheduleClient
When using `temporal-spring-boot-starter`, you can autowire the `ScheduleClient`.

```java
@Service
public class ReportSchedulerService {
    
    private final ScheduleClient scheduleClient;

    public ReportSchedulerService(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }
}
```

### 2. Creating a Schedule
Schedules are created using a `ScheduleHandle`. This example shows how to schedule a daily cleanup task.

```java
public void scheduleDailyCleanup() {
    String scheduleId = "cleanup-schedule";
    
    Schedule schedule = Schedule.newBuilder()
        .setAction(ScheduleActionStartWorkflow.newBuilder()
            .setWorkflowType("CleanupWorkflow")
            .setOptions(WorkflowOptions.newBuilder()
                .setTaskQueue("CLEANUP_QUEUE")
                .setWorkflowId("cleanup-execution")
                .build())
            .build())
        .setSpec(ScheduleSpec.newBuilder()
            .setCalendars(List.of(
                ScheduleCalendarSpec.newBuilder()
                    .setHour(List.of(new ScheduleRange(2))) // 2 AM
                    .setMinute(List.of(new ScheduleRange(0)))
                    .setDayOfWeek(List.of(new ScheduleRange(1, 5))) // Mon-Fri
                    .build()))
            .setIntervals(List.of(
                // IMPORTANT: Wrap Duration in Objects.requireNonNull to avoid null safety warnings
                new ScheduleIntervalSpec(Objects.requireNonNull(Duration.ofHours(12))))) 
            .build())
        .setPolicy(SchedulePolicy.newBuilder()
            .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_BUFFER_ONE)
            .setCatchupWindow(Duration.ofMinutes(10))
            .build())
        .build();

    scheduleClient.createSchedule(scheduleId, schedule, ScheduleOptions.newBuilder().build());
}
```

### 3. Triggering a Schedule Immediately
To manually trigger a scheduled workflow immediately, use the `trigger` method with an overlap policy.

> [!NOTE]
> In newer SDK versions, `trigger` takes `ScheduleOverlapPolicy` directly, not `ScheduleTriggerOptions`.

```java
public void triggerNow(String scheduleId) {
    scheduleClient.getHandle(scheduleId)
        .trigger(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_ALLOW_ALL);
}
```

### 4. Listing and Monitoring Schedules
```java
public void listSchedules() {
    scheduleClient.listSchedules().forEachRemaining(description -> {
        System.out.println("ID: " + description.getScheduleId());
        ScheduleHandle handle = scheduleClient.getHandle(description.getScheduleId());
        System.out.println("Details: " + handle.describe());
    });
}
```

## References
- See `examples/` for the `CleanupSchedulerExample.java`.
- See `references/temporal-overlap-policies.md` for policy details.
