package com.example.test_runner_worker.model;


import com.example.test_runner_worker.model.enums.TestRunStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {
    private UUID id;
    @Enumerated(EnumType.STRING)
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
