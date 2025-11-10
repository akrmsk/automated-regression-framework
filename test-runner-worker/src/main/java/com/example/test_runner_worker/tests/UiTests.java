package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class UiTests {

    private final String seleniumHubUrl;
    private final ReportGenerator reportGenerator;
    private final String reportsDirectory = "reports"; // Local directory

    public UiTests(@Value("${selenium.hub.url}") String seleniumHubUrl, ReportGenerator reportGenerator) {
        this.seleniumHubUrl = seleniumHubUrl;
        this.reportGenerator = reportGenerator;
        // Ensure the reports directory exists
        try {
            Files.createDirectories(Paths.get(reportsDirectory, "screenshots"));
        } catch (IOException e) {
            log.error("Could not create screenshots directory", e);
        }
    }

    @Test(name = "UI Smoke Test", tags = {"ui", "smoke"}, description = "Verify Google homepage title")
    public TestResult runUiSmokeTest() {
        TestResult result = new TestResult();
        WebDriver driver = null;
        String runId = UUID.randomUUID().toString();

        // --- Test Metadata ---
        result.setTestType("UI");
        result.setTestUrl("https://www.google.com");
        result.setTestParameters("None");
        result.setTestDescription("UI Smoke Test - Verify Google homepage loads and has correct title");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            // --- Test Setup ---
            log.info("Connecting to Selenium Hub at: {}", seleniumHubUrl);
            driver = new RemoteWebDriver(new URL(seleniumHubUrl), new ChromeOptions());
            log.info("Driver created. Navigating to Google...");

            // --- Test Execution ---
            driver.get("https://www.google.com");
            String title = driver.getTitle();
            log.info("Page title is: {}", title);

            // --- Assertion ---
            if (!title.contains("Google")) {
                throw new AssertionError("Page title was '" + title + "', did not contain 'Google'");
            }

            // --- Success Path ---
            log.info("UI test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            // --- Failure Path ---
            log.error("UI test FAILED: {}", t.getMessage());
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage(t.getMessage());

            // Take screenshot on failure
            log.info("Attempting to take failure screenshot...");
            String screenshotPath = takeScreenshot(driver, runId);
            result.setScreenshotPath(screenshotPath); // Save path for the report

        } finally {
            // --- Cleanup ---
            if (driver != null) {
                log.info("Quitting driver...");
                driver.quit();
            }

            // --- Reporting (always runs) ---
            result.setEndTime(LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);

            // Generate the HTML report
            String reportPath = reportGenerator.generateReport(result, runId);
            result.setReportUrl(reportPath);
        }

        return result;
    }

    private String takeScreenshot(WebDriver driver, String runId) {
        if (driver == null) {
            return null;
        }

        String filename = "failure-" + runId + ".png";
        // Save screenshots in a sub-directory
        Path destination = Paths.get(reportsDirectory, "screenshots", filename);

        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
            Files.copy(screenshotFile.toPath(), destination);
            log.info("Screenshot saved successfully: {}", destination.toAbsolutePath());
            return destination.toAbsolutePath().toString();

        } catch (IOException | ClassCastException e) {
            log.error("Could not save screenshot: {}", e.getMessage());
            return null;
        }
    }
}