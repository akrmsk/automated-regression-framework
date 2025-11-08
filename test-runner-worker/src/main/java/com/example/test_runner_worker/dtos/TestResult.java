package com.example.test_runner_worker.dtos;

import com.example.test_runner_worker.model.enums.TestRunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    private TestRunStatus status;

    private String reportUrl;

    private String errorMessage;

    private Integer failedTestCount;

    private String screenshotPath;

    // Additional fields for more detailed reporting
    private String testType; // "UI" or "API"
    private String testUrl; // URL or endpoint being tested
    private String testParameters; // Any parameters used in the test
    private String testDescription; // Description of the test
    private LocalDateTime startTime; // When the test started
    private LocalDateTime endTime; // When the test ended
    private Long durationMs; // How long the test took in milliseconds
}
