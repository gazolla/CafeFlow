package io.temporal.example;

import io.temporal.client.schedules.Schedule;
import io.temporal.client.schedules.ScheduleActionStartWorkflow;
import io.temporal.client.schedules.ScheduleCalendarSpec;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.client.schedules.ScheduleOptions;
import io.temporal.client.schedules.ScheduleRange;
import io.temporal.client.schedules.ScheduleSpec;
import io.temporal.client.schedules.SchedulePolicy;
import io.temporal.client.schedules.ScheduleOverlapPolicy;
import io.temporal.workflow.WorkflowOptions;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

/**
 * Example of a service that manages recurring cleanup tasks using Temporal Schedules.
 */
@Service
public class CleanupSchedulerService {
    
    private final ScheduleClient scheduleClient;

    public CleanupSchedulerService(ScheduleClient scheduleClient) {
        this.scheduleClient = scheduleClient;
    }

    public void createWeeklyCleanup() {
        String scheduleId = "weekly-system-cleanup";
        
        Schedule schedule = Schedule.newBuilder()
            .setAction(ScheduleActionStartWorkflow.newBuilder()
                .setWorkflowType("SystemCleanupWorkflow")
                .setOptions(WorkflowOptions.newBuilder()
                    .setTaskQueue("CLEANUP_QUEUE")
                    .build())
                .build())
            .setSpec(ScheduleSpec.newBuilder()
                // Run every Sunday at 3:00 AM
                .setCalendars(List.of(
                    ScheduleCalendarSpec.newBuilder()
                        .setDayOfWeek(List.of(new ScheduleRange(0))) // Sunday
                        .setHour(List.of(new ScheduleRange(3)))
                        .setMinute(List.of(new ScheduleRange(0)))
                        .build()))
                .build())
            .setPolicy(SchedulePolicy.newBuilder()
                .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP)
                .build())
            .build();

        scheduleClient.createSchedule(scheduleId, schedule, ScheduleOptions.newBuilder().build());
    }

    public void triggerNow(String scheduleId) {
        scheduleClient.getHandle(scheduleId).trigger();
    }

    public void stopSchedule(String scheduleId) {
        scheduleClient.getHandle(scheduleId).delete();
    }
}
