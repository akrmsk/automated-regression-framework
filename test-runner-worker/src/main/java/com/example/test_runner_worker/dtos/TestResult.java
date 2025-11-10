package com.example.test_runner_worker.dtos;

import com.example.test_runner_worker.model.enums.TestRunStatus;
import lombok.Data;
import java.time.LocalDateTime;

// This DTO holds the *result* of a single test execution.
@Data
public class TestResult {
    private TestRunStatus status;
    private String reportUrl;
    private String errorMessage;
    private Integer failedTestCount = 0;
    private String screenshotPath;
    private String testType;
    private String testUrl;
    private String testParameters;
    private String testDescription;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
}