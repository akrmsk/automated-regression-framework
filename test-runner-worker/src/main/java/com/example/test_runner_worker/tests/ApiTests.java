package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ApiTests {

    private final ReportGenerator reportGenerator;
    private final String seleniumHubUrl;

    public ApiTests(ReportGenerator reportGenerator, @Value("${selenium.hub.url}") String seleniumHubUrl) {
        this.reportGenerator = reportGenerator;
        this.seleniumHubUrl = seleniumHubUrl;
    }

    @Test(name = "API Smoke Test", tags = {"api", "smoke"}, description = "Verify Public APIs endpoint returns 200")
    public TestResult runApiSmokeTest() {
        TestResult result = new TestResult();
        String runId = UUID.randomUUID().toString();
        WebDriver driver = null;

        String testUrl = "https://api.publicapis.org/entries?title=cat";
        result.setTestType("API");
        result.setTestUrl(testUrl);
        result.setTestParameters("title=cat");
        result.setTestDescription("API Smoke Test - Verify Public APIs endpoint returns 200 status code");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            log.info("Connecting to Selenium Hub for API test: {}", seleniumHubUrl);
            ChromeOptions options = new ChromeOptions();
            driver = new RemoteWebDriver(new URL(seleniumHubUrl), options);
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // --- THIS IS THE FIX ---
            // Navigate to a real page first to establish a valid origin for the fetch command.
            driver.get("https://www.google.com");
            // --- END OF FIX ---

            log.info("Executing fetch command in browser for: {}", testUrl);

            String script = "const callback = arguments[arguments.length - 1];" +
                    "fetch('" + testUrl + "')" +
                    "  .then(response => response.status)" +
                    "  .then(status => callback(status))" +
                    "  .catch(err => callback(err.message));";

            Object response = js.executeAsyncScript(script);

            if (response instanceof Long) {
                Long statusCode = (Long) response;
                if (statusCode != 200) {
                    throw new AssertionError("API test failed. Expected status 200 but got " + statusCode);
                }
            } else {
                throw new Exception("API test failed in JavaScript: " + response.toString());
            }

            log.info("API test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            log.error("API test FAILED: {}", t.getMessage());
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage(t.getMessage());
        } finally {
            if (driver != null) {
                log.info("Quitting driver for API test...");
                driver.quit();
            }
            result.setEndTime(LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);
            String reportPath = reportGenerator.generateReport(result, runId);
            result.setReportUrl(reportPath);
        }
        return result;
    }
}