package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.io.File;
import org.apache.commons.io.FileUtils;

@Service
@Slf4j
public class HtmlReportGenerator implements ReportGenerator {

    private final TemplateEngine templateEngine;

    public HtmlReportGenerator() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    public String generateReport(TestResult testResult, String runId) {
        Context context = new Context();

        // Basic test result information
        context.setVariable("status", testResult.getStatus().toString());
        context.setVariable("errorMessage", testResult.getErrorMessage());
        context.setVariable("failedTestCount", testResult.getFailedTestCount());

        // Additional detailed information
        context.setVariable("testType", testResult.getTestType());
        context.setVariable("testUrl", testResult.getTestUrl());
        context.setVariable("testParameters", testResult.getTestParameters());
        context.setVariable("testDescription", testResult.getTestDescription());
        context.setVariable("startTime", testResult.getStartTime());
        context.setVariable("endTime", testResult.getEndTime());
        context.setVariable("durationMs", testResult.getDurationMs());

        // Format duration for display (convert milliseconds to a more readable format)
        if (testResult.getDurationMs() != null) {
            long seconds = testResult.getDurationMs() / 1000;
            long millis = testResult.getDurationMs() % 1000;
            context.setVariable("formattedDuration", String.format("%d.%03d seconds", seconds, millis));
        } else {
            context.setVariable("formattedDuration", "N/A");
        }

        if (testResult.getScreenshotPath() != null) {
            try {
                byte[] fileContent = FileUtils.readFileToByteArray(new File(testResult.getScreenshotPath()));
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                context.setVariable("screenshotPath", "data:image/png;base64," + encodedString);
            } catch (IOException e) {
                log.error("Could not read screenshot file", e);
                context.setVariable("screenshotPath", null);
            }
        } else {
            context.setVariable("screenshotPath", null);
        }

        String html = templateEngine.process("report-template", context);
        String reportsDirectory = "/app/reports";
        String filename = "report-" + runId + ".html";
        Path destination = Paths.get(reportsDirectory, filename);

        try (FileWriter writer = new FileWriter(destination.toFile())) {
            writer.write(html);
            log.info("HTML report saved successfully: {}", destination);
            return destination.toString();
        } catch (IOException e) {
            log.error("Could not save HTML report: {}", e.getMessage());
            return null;
        }
    }
}
