package com.example.test_runner_worker.model;

import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

// This is a DTO (Data Transfer Object) on the worker side.
// It just represents the job message we receive.
@Data
public class TestRun {
    @JsonProperty("runId")
    private UUID id;
    private TestRunStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reportUrl;
    private String environment;
    private String tags;
    private String errorMessage;
    private String errorDetails;
    private Integer failedTestCount;
    private String screenshotPath;
}