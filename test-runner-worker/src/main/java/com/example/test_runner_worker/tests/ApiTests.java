package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.HtmlReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@Slf4j
@Service
public class ApiTests {

    private final HtmlReportGenerator htmlReportGenerator;

    public ApiTests(HtmlReportGenerator htmlReportGenerator) {
        this.htmlReportGenerator = htmlReportGenerator;
    }

    @Test(name = "API Smoke Test", tags = {"api", "smoke"})
    public TestResult runApiSmokeTest() {
        TestResult result = new TestResult();
        String runId = UUID.randomUUID().toString();

        result.setTestType("API");
        result.setTestUrl("https://api.publicapis.org/entries");
        result.setTestParameters("title=cat");
        result.setTestDescription("API Smoke Test - Verify Public APIs endpoint returns 200 status code");
        result.setStartTime(java.time.LocalDateTime.now());

        long startTimeMs = System.currentTimeMillis();

        try {
            given()
                    .param("title", "cat")
                    .when()
                    .get("https://api.publicapis.org/entries")
                    .then()
                    .statusCode(200);

            log.info("API test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Exception e) {
            log.error("API test FAILED: {}", e.getMessage());
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setEndTime(java.time.LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);

            String reportPath = htmlReportGenerator.generateReport(result, runId);
            result.setReportUrl(reportPath);
        }

        return result;
    }

    @Test(name = "API Regression Test", tags = {"api", "regression"})
    public TestResult runApiRegressionTest() {
        // This is just an example. In a real scenario, this would be a different test.
        return runApiSmokeTest();
    }
}
