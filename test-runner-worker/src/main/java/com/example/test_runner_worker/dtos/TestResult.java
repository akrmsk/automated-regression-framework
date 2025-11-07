package com.example.test_runner_worker.dtos;

import com.example.test_runner_worker.model.enums.TestRunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    private TestRunStatus status;

    private String reportUrl;

    private String errorMessage;

    private Integer failedTestCount;
}
