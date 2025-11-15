package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class HtmlReportGenerator implements ReportGenerator {

    private final TemplateEngine templateEngine;
    private final String reportsDirectory = "reports"; // Save to a local 'reports' folder

    public HtmlReportGenerator() {
        // Configure the Thymeleaf template engine
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);

        // Ensure the reports directory exists
        try {
            Files.createDirectories(Paths.get(reportsDirectory));
        } catch (IOException e) {
            log.error("Could not create reports directory: {}", reportsDirectory, e);
        }
    }

    @Override
    public String generateReport(TestResult testResult, String runId) {
        Context context = new Context();

        context.setVariable("status", testResult.getStatus().toString());
        context.setVariable("errorMessage", testResult.getErrorMessage());
        context.setVariable("failedTestCount", testResult.getFailedTestCount());
        context.setVariable("testType", testResult.getTestType());
        context.setVariable("testUrl", testResult.getTestUrl());
        context.setVariable("testParameters", testResult.getTestParameters());
        context.setVariable("testDescription", testResult.getTestDescription());
        context.setVariable("startTime", testResult.getStartTime());
        context.setVariable("endTime", testResult.getEndTime());

        // Calculate and format duration
        if (testResult.getDurationMs() != null) {
            double seconds = testResult.getDurationMs() / 1000.0;
            context.setVariable("formattedDuration", String.format("%.3f seconds", seconds));
        } else {
            context.setVariable("formattedDuration", "N/A");
        }

        if (testResult.getScreenshotPath() != null) {
            try {
                // Read the screenshot file and encode it as Base64
                byte[] fileContent = FileUtils.readFileToByteArray(new File(testResult.getScreenshotPath()));
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                // Embed it directly into the HTML
                context.setVariable("screenshotBase64", "data:image/png;base64," + encodedString);
            } catch (IOException e) {
                log.error("Could not read screenshot file to embed in report", e);
                context.setVariable("screenshotBase64", null);
            }
        } else {
            context.setVariable("screenshotBase64", null);
        }

        // Process the template
        // ... inside the generateReport method ...

// Process the template
        String html = templateEngine.process("report-template", context);

// Save the file with a unique name to prevent parallel conflicts
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String filename = "report-" + runId + "-" + uniqueId + ".html";

        Path destination = Paths.get(reportsDirectory, filename);

        try (FileWriter writer = new FileWriter(destination.toFile())) {
            writer.write(html);
            log.info("HTML report saved successfully: {}", destination.toAbsolutePath());
            // Return the relative path for the dashboard
            return "reports/" + filename;
        } catch (IOException e) {
            log.error("Could not save HTML report: {}", e.getMessage());
            return null;
        }
    }
}