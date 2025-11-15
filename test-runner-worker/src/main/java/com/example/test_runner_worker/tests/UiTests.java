package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*; // <-- Import By, Keys, WebElement
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions; // <-- Import wait
import org.openqa.selenium.support.ui.WebDriverWait; // <-- Import wait
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption; // <-- Import this
import java.time.Duration; // <-- Import Duration
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

    @Test(name = "UI Google Search Test", tags = {"ui", "smoke"}, description = "Verify Google search results page title")
    public TestResult runUiSearchTest() {
        TestResult result = new TestResult();
        WebDriver driver = null;
        String runId = UUID.randomUUID().toString();

        // Test site that allows automation
        String testUrl = "https://the-internet.herokuapp.com/";
        String linkText = "Checkboxes";

        // --- Test Metadata ---
        result.setTestType("UI");
        result.setTestUrl(testUrl);
        result.setTestParameters("Link Text: " + linkText);
        result.setTestDescription("UI Smoke Test - Verify navigation on the-internet.herokuapp.com");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            // --- Test Setup ---
            log.info("Connecting to Selenium Hub at: {}", seleniumHubUrl);
            driver = new RemoteWebDriver(new URL(seleniumHubUrl), new ChromeOptions());
            log.info("Driver created. Navigating to " + testUrl);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // --- Test Execution ---
            driver.get(testUrl);

            WebElement checkboxesLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText(linkText)));
            checkboxesLink.click();

            WebElement pageHeading = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h3")));
            String headingText = pageHeading.getText();

            // --- Assertion ---
            if (!headingText.equals(linkText)) {
                throw new AssertionError("Page heading was '" + headingText + "', did not equal '" + linkText + "'");
            }

            // --- Success Path ---
            log.info("UI test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            log.error("UI test FAILED: {}", t.getMessage());
            String errorMessage = "UI test FAILED: " + t.getMessage();
            result.setErrorMessage(errorMessage);
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);

            // --- NO SCREENSHOT HERE --- We moved it to 'finally'

        } finally {
            // --- START OF CHANGE: Screenshot logic moved here ---
            // This code now runs for BOTH success and failure
            log.info("Attempting to take screenshot...");
            String screenshotPath = takeScreenshot(driver, runId);
            result.setScreenshotPath(screenshotPath); // Save path for the report
            // --- END OF CHANGE ---

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
            log.warn("Driver was null, cannot take screenshot.");
            return null;
        }

        String filename = "screenshot-" + runId + ".png";
        Path destination = Paths.get(reportsDirectory, "screenshots", filename);

        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File screenshotFile = ts.getScreenshotAs(OutputType.FILE);
            // Use REPLACE_EXISTING to prevent errors if file somehow exists
            Files.copy(screenshotFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Screenshot saved successfully: {}", destination.toAbsolutePath());
            return destination.toAbsolutePath().toString();

        } catch (IOException | ClassCastException | WebDriverException e) {
            log.error("Could not save screenshot: {}", e.getMessage());
            return null;
        }
    }
}