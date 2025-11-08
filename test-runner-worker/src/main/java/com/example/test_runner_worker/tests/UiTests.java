package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.HtmlReportGenerator;
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
import java.util.UUID;

@Slf4j
@Service
public class UiTests {

    private final String seleniumHubUrl;
    private final HtmlReportGenerator htmlReportGenerator;

    public UiTests(@Value("${selenium.hub.url}") String seleniumHubUrl, HtmlReportGenerator htmlReportGenerator) {
        this.seleniumHubUrl = seleniumHubUrl;
        this.htmlReportGenerator = htmlReportGenerator;
    }

    @Test(name = "UI Smoke Test", tags = {"ui", "smoke"})
    public TestResult runUiSmokeTest() {
        TestResult result = new TestResult();
        WebDriver driver = null;
        String runId = UUID.randomUUID().toString();

        result.setTestType("UI");
        result.setTestUrl("https://www.google.com");
        result.setTestParameters("None");
        result.setTestDescription("UI Smoke Test - Verify Google homepage loads and has correct title");
        result.setStartTime(java.time.LocalDateTime.now());

        long startTimeMs = System.currentTimeMillis();

        try {
            log.info("Connecting to Selenium Hub at: {}", seleniumHubUrl);
            driver = new RemoteWebDriver(new URL(seleniumHubUrl), new ChromeOptions());
            log.info("Driver created. Navigating to Google...");

            driver.get("https://www.google.com");
            String title = driver.getTitle();
            log.info("Page title is: {}", title);

            if (!title.contains("Google")) {
                throw new AssertionError("Page title was '" + title + "', did not contain 'Google'");
            }

            log.info("UI test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            log.error("UI test FAILED: {}", t.getMessage());
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage(t.getMessage());

            log.info("Attempting to take failure screenshot...");
            String screenshotPath = takeScreenshot(driver, runId);
            result.setScreenshotPath(screenshotPath);

        } finally {
            if (driver != null) {
                log.info("Quitting driver...");
                driver.quit();
            }

            result.setEndTime(java.time.LocalDateTime.now());
            result.setDurationMs(System.currentTimeMillis() - startTimeMs);

            String reportPath = htmlReportGenerator.generateReport(result, runId);
            result.setReportUrl(reportPath);
        }

        return result;
    }

    @Test(name = "UI Regression Test", tags = {"ui", "regression"})
    public TestResult runUiRegressionTest() {
        // This is just an example. In a real scenario, this would be a different test.
        return runUiSmokeTest();
    }

    private String takeScreenshot(WebDriver driver, String runId) {
        if (driver == null) {
            return null;
        }

        String reportsDirectory = "/app/reports";
        String filename = "failure-" + runId + ".png";
        Path destination = Paths.get(reportsDirectory, filename);

        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
            Files.copy(screenshotFile.toPath(), destination);
            log.info("Screenshot saved successfully: {}", destination);
            return destination.toString();

        } catch (IOException | ClassCastException e) {
            log.error("Could not save screenshot: {}", e.getMessage());
            return null;
        }
    }
}
