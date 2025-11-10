package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import static io.restassured.RestAssured.given;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTests {

    private final ReportGenerator reportGenerator;

    @Test(name = "API Smoke Test", tags = {"api", "smoke"}, description = "Verify Public APIs endpoint returns 200")
    public TestResult runApiSmokeTest() {
        TestResult result = new TestResult();
        String runId = UUID.randomUUID().toString();

        // --- Test Metadata ---
        result.setTestType("API");
        result.setTestUrl("https://api.publicapis.org/entries");
        result.setTestParameters("title=cat");
        result.setTestDescription("API Smoke Test - Verify Public APIs endpoint returns 200 status code");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            // --- Test Execution ---
            given()
                    .param("title", "cat")
                    .when()
                    .get("https://api.publicapis.org/entries")
                    .then()
                    .statusCode(200); // The assertion

            // --- Success Path ---
            log.info("API test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            // --- Failure Path ---
            log.error("API test FAILED: {}", t.getMessage());
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage(t.getMessage());
        } finally {
            // --- Reporting (always runs) ---
            result.setEndTime(LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);

            // Generate the HTML report
            String reportPath = reportGenerator.generateReport(result, runId);
            result.setReportUrl(reportPath);
        }

        return result;
    }
}