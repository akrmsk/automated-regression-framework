package com.example.test_runner_worker.tests;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.service.ReportGenerator;
import com.example.test_runner_worker.service.HtmlReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UiTests {

    private final String seleniumHubUrl;
    private final List<ReportGenerator> reportGenerators;
    private final String reportsDirectory = "reports";

    public UiTests(@Value("${selenium.hub.url}") String seleniumHubUrl,
                   List<ReportGenerator> reportGenerators) {
        this.seleniumHubUrl = seleniumHubUrl;
        this.reportGenerators = reportGenerators;
        try {
            Files.createDirectories(Paths.get(reportsDirectory, "screenshots"));
        } catch (IOException e) {
            log.error("Could not create screenshots directory", e);
        }
    }

    @Test(name = "UI Google Search Test", tags = {"ui", "smoke"}, description = "Verify Google search results page title")
    public TestResult runUiSearchTest() {
        // ... (all your test setup code remains the same) ...
        TestResult result = new TestResult();
        WebDriver driver = null;
        String runId = UUID.randomUUID().toString();

        result.setTestType("UI");
        result.setTestUrl("https://the-internet.herokuapp.com/");
        result.setTestParameters("Link Text: Checkboxes");
        result.setTestDescription("UI Smoke Test - Verify navigation on the-internet.herokuapp.com");
        result.setStartTime(LocalDateTime.now());
        long startTimeMs = System.currentTimeMillis();

        try {
            // ... (all your try logic remains the same) ...
            log.info("Connecting to Selenium Hub at: {}", seleniumHubUrl);
            driver = new RemoteWebDriver(new URL(seleniumHubUrl), new ChromeOptions());
            log.info("Driver created. Navigating to https://the-internet.herokuapp.com/");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get("https://the-internet.herokuapp.com/");
            WebElement checkboxesLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Checkboxes")));
            checkboxesLink.click();
            WebElement pageHeading = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h3")));
            String headingText = pageHeading.getText();
            if (!headingText.equals("Checkboxes")) {
                throw new AssertionError("Page heading was '" + headingText + "', did not equal 'Checkboxes'");
            }
            log.info("UI test passed.");
            result.setStatus(TestRunStatus.COMPLETED);
            result.setFailedTestCount(0);

        } catch (Throwable t) {
            // ... (all your catch logic remains the same) ...
            log.error("UI test FAILED: {}", t.getMessage());
            String errorMessage = "UI test FAILED: " + t.getMessage();
            result.setErrorMessage(errorMessage);
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);

        } finally {
            log.info("Attempting to take screenshot...");
            String screenshotPath = takeScreenshot(driver, runId);
            result.setScreenshotPath(screenshotPath);

            if (driver != null) {
                log.info("Quitting driver...");
                driver.quit();
            }

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

    // ... (takeScreenshot method remains the same) ...
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
            Files.copy(screenshotFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Screenshot saved successfully: {}", destination.toAbsolutePath());
            return destination.toAbsolutePath().toString();
        } catch (IOException | ClassCastException | WebDriverException e) {
            log.error("Could not save screenshot: {}", e.getMessage());
            return null;
        }
    }
}