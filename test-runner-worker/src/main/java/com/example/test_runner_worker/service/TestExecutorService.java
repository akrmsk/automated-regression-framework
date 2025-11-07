package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.TestRun;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.tests.ApiTests;
import com.example.test_runner_worker.tests.UiTests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestExecutorService {

    private final ApiTests apiTests;
    private final UiTests uiTests;

    public TestExecutorService(ApiTests apiTests, UiTests uiTests) {
        this.apiTests = apiTests;
        this.uiTests = uiTests;
    }

    public TestResult executeTest(TestRun testRun){
        TestResult result = new TestResult();
        String tags = testRun.getTags() != null ? testRun.getTags().toLowerCase() : "";
        try{
            log.info("test execution has started");
            if (tags.contains("ui")) {
                log.info("Running UI test based on 'ui' tag...");
                uiTests.runUiSmokeTest();
                result.setReportUrl("https://www.google.com");

            } else if (tags.contains("api")) {
                log.info("Running API test based on 'api' tag...");
                apiTests.runApiSmokeTest();
                result.setReportUrl("https://api.publicapis.org/entries");

            } else {
                // If no "ui" or "api" tag is found, we just pass the job.
                log.warn("No 'ui' or 'api' tag found for job {}. Marking as passed.", testRun.getId());
            }

            if (result.getStatus() != TestRunStatus.FAILED) {
                log.info("Test execution SUCCEEDED for job: {}", testRun.getId());
                result.setStatus(TestRunStatus.COMPLETED);
            }

        } catch (Exception e){
            log.error("Test execution was interrupted", e);

            result.setStatus(TestRunStatus.FAILED);
            result.setErrorMessage(e.getMessage());
            result.setFailedTestCount(1);
        }
        return result;
    }
}
