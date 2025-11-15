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
        String searchTerm = "Selenium WebDriver";

        // --- Test Metadata ---
        result.setTestType("UI");
        result.setTestUrl("https://www.google.com");
        result.setTestParameters("Search Term: " + searchTerm);
        result.setTestDescription("UI Smoke Test - Verify Google search for 'Selenium WebDriver'");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            // --- Test Setup ---
            log.info("Connecting to Selenium Hub at: {}", seleniumHubUrl);
            driver = new RemoteWebDriver(new URL(seleniumHubUrl), new ChromeOptions());
            log.info("Driver created. Navigating to Google...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 10-second wait

            // --- Test Execution ---
            driver.get("https://www.google.com");

            // Find the search box (name='q')
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("q")));

            // Type the search term and submit
            searchBox.sendKeys(searchTerm);
            searchBox.sendKeys(Keys.ENTER);

            // Wait for the results page title to contain the search term
            wait.until(ExpectedConditions.titleContains(searchTerm));

            String title = driver.getTitle();
            log.info("Page title is: {}", title);

            // --- Assertion ---
            if (!title.contains(searchTerm)) {
                throw new AssertionError("Page title was '" + title + "', did not contain '" + searchTerm + "'");
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

        } finally {
            // --- Cleanup ---

            // --- SCREENSHOT LOGIC IS IN FINALLY ---
            log.info("Attempting to take screenshot...");
            String screenshotPath = takeScreenshot(driver, runId);
            result.setScreenshotPath(screenshotPath); // Save path for the report

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

        // --- START FIX ---
        // Changed "failure-" to "screenshot-"
        String filename = "screenshot-" + runId + ".png";
        // --- END FIX ---

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