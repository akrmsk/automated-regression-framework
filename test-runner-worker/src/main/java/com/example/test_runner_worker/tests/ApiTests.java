package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import com.example.test_runner_worker.service.HtmlReportGenerator;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@Service
public class ApiTests {

    private final List<ReportGenerator> reportGenerators;

    public ApiTests(List<ReportGenerator> reportGenerators) {
        this.reportGenerators = reportGenerators;
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

            given()
                    .when()
                    .get(testUrl)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .and()
                    .body("userId", equalTo(1));

            log.info("API test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            log.error("API test FAILED: {}", t.getMessage());
            String errorMessage = "API test FAILED: " + t.getMessage();
            result.setErrorMessage(errorMessage);
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);

        } finally {
            result.setEndTime(LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);

            String htmlReportPath = null;

            // Generate all reports (HTML, CSV)
            for (ReportGenerator generator : reportGenerators) {
                String reportPath = generator.generateReport(result, runId);

                // Check if this is the HTML generator and save its path
                if (generator instanceof HtmlReportGenerator && reportPath != null) {
                    htmlReportPath = reportPath;
                }
            }

            // After the loop, explicitly set the URL to the HTML path
            result.setReportUrl(htmlReportPath);
        }
        return result;
    }
}