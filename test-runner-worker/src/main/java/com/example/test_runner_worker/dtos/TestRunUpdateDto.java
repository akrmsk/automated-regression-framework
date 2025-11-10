package com.example.test_runner_worker.dtos;

import com.example.test_runner_worker.model.enums.TestRunStatus;
import lombok.Data;

// This DTO is used to send the *final* update back to the API.
@Data
public class TestRunUpdateDto {
    private TestRunStatus status;
    private String reportUrl;
    private String errorMessage;
    private String errorDetails;
    private Integer failedTestCount;
    private String screenshotPath;
}