package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import io.restassured.response.Response; // <-- Import RestAssured
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; // <-- Keep this if you use it

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given; // <-- Import RestAssured static methods
import static org.hamcrest.Matchers.equalTo; // <-- Import Hamcrest matchers

@Slf4j
@Service
public class ApiTests {

    private final ReportGenerator reportGenerator;

    // We'll use RestAssured here as it's included in the pom.xml
    // No need for RestTemplate or Selenium for this test
    public ApiTests(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    @Test(name = "API Content Test", tags = {"api", "smoke"}, description = "Verify JSONPlaceholder post content")
    public TestResult runApiContentTest() {
        TestResult result = new TestResult();
        String runId = UUID.randomUUID().toString();

        String testUrl = "https://jsonplaceholder.typicode.com/posts/1";
        result.setTestType("API");
        result.setTestUrl(testUrl);
        result.setTestParameters("None");
        result.setTestDescription("Verify userId for post #1 from JSONPlaceholder");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            log.info("Executing API test for: {}", testUrl);

            // Use RestAssured for a clean API test
            given()
                    .when()
                    .get(testUrl)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .and()
                    .body("userId", equalTo(1)); // Assert that the userId field is 1

            log.info("API test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            log.error("API test FAILED: {}", t.getMessage());
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage(t.getMessage());
        } finally {
            result.setEndTime(LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);
            String reportPath = reportGenerator.generateReport(result, runId);
            result.setReportUrl(reportPath);
        }
        return result;
    }
}