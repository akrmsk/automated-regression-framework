package com.example.test_runner_worker.dtos;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRunUpdateDto {
    private TestRunStatus status;
    private String reportUrl;
    private String errorMessage;
    private String errorDetails;
    private Integer failedTestCount;
    private String screenshotPath;
}
