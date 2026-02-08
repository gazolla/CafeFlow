package com.cafeflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class CafeFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeFlowApplication.class, args);
        log.info("â˜• CafeFlow started successfully!");
        log.info("Available Helpers ready for use: Email, Reddit, OpenAI, Database, etc.");
    }

    /*
     * TODO: Example of how to schedule a workflow
     * 
     * @Bean
     * public CommandLineRunner
     * scheduleRunner(io.temporal.client.schedules.ScheduleClient scheduleClient) {
     * return args -> {
     * String scheduleId = "my-daily-automation";
     * try {
     * // Define the action (start a workflow)
     * io.temporal.client.schedules.ScheduleActionStartWorkflow action =
     * io.temporal.client.schedules.ScheduleActionStartWorkflow.newBuilder()
     * .setWorkflowType("MyWorkflow")
     * .setWorkflowId("my-workflow-id")
     * .setTaskQueue("AUTOMATION_TASK_QUEUE")
     * .build();
     * 
     * // Define the spec (e.g., every day at 08h00)
     * io.temporal.client.schedules.ScheduleSpec spec =
     * io.temporal.client.schedules.ScheduleSpec.newBuilder()
     * .setCalendars(java.util.Collections.singletonList(
     * io.temporal.client.schedules.ScheduleCalendarSpec.newBuilder()
     * .setHour(java.util.Collections.singletonList(new
     * io.temporal.client.schedules.ScheduleRange(8)))
     * .setMinute(java.util.Collections.singletonList(new
     * io.temporal.client.schedules.ScheduleRange(0)))
     * .build()
     * ))
     * .build();
     * 
     * // Create the schedule
     * io.temporal.client.schedules.Schedule schedule =
     * io.temporal.client.schedules.Schedule.newBuilder()
     * .setAction(action)
     * .setSpec(spec)
     * .build();
     * 
     * scheduleClient.createSchedule(scheduleId, schedule,
     * io.temporal.client.schedules.ScheduleOptions.newBuilder().build());
     * log.info("Schedule created: {}", scheduleId);
     * } catch (io.temporal.failure.ScheduleAlreadyRunningException e) {
     * log.info("Schedule {} is already running", scheduleId);
     * }
     * };
     * }
     */
}