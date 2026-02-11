package com.cafeflow.workflows.reddit;

import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedditDigestSchedulerConfig {

    private final ScheduleClient scheduleClient;

    @Bean
    @Profile("!test")
    public CommandLineRunner setupRedditDigestSchedule() {
        return args -> {
            String scheduleId = "reddit-digest-schedule";

            Schedule schedule = Schedule.newBuilder()
                    .setAction(ScheduleActionStartWorkflow.newBuilder()
                            .setWorkflowType(RedditDigestWorkflow.class)
                            .setArguments("java", "team@company.com")
                            .setOptions(WorkflowOptions.newBuilder()
                                    .setWorkflowId("reddit-digest-run")
                                    .setTaskQueue("REDDIT_DIGEST_WORKER")
                                    .build())
                            .build())
                    .setSpec(ScheduleSpec.newBuilder()
                            .setIntervals(List.of(
                                    new ScheduleIntervalSpec(Duration.ofHours(24))))
                            .build())
                    .setPolicy(SchedulePolicy.newBuilder()
                            .setOverlap(ScheduleOverlapPolicy.SCHEDULE_OVERLAP_POLICY_SKIP)
                            .build())
                    .build();

            try {
                scheduleClient.createSchedule(scheduleId, schedule, ScheduleOptions.newBuilder().build());
                log.info("Schedule created: {}", scheduleId);
            } catch (ScheduleAlreadyRunningException e) {
                log.info("Schedule already exists: {}", scheduleId);
            }
        };
    }
}
