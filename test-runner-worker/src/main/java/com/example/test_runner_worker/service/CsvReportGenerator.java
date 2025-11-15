package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CsvReportGenerator implements ReportGenerator {

    private final String reportsDirectory = "reports";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CsvReportGenerator() {
        // Ensure the reports directory exists
        try {
            Files.createDirectories(Paths.get(reportsDirectory));
        } catch (IOException e) {
            log.error("Could not create reports directory: {}", reportsDirectory, e);
        }
    }

    @Override
    public String generateReport(TestResult testResult, String runId) {
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String filename = "report-" + runId + "-" + uniqueId + ".csv";
        Path destination = Paths.get(reportsDirectory, filename);

        // Define CSV header and data row
        String header = "RunID,Status,TestType,Description,StartTime,EndTime,Duration(ms),ErrorMessage\n";
        String data = String.format(
                "%s,%s,%s,\"%s\",%s,%s,%d,\"%s\"\n",
                runId,
                testResult.getStatus(),
                testResult.getTestType(),
                testResult.getTestDescription(),
                testResult.getStartTime() != null ? testResult.getStartTime().format(dtf) : "N/A",
                testResult.getEndTime() != null ? testResult.getEndTime().format(dtf) : "N/A",
                testResult.getDurationMs(),
                testResult.getErrorMessage() != null ? testResult.getErrorMessage().replace("\"", "'") : ""
        );

        try (FileWriter writer = new FileWriter(destination.toFile())) {
            // Write header only if the file is new (or just overwrite)
            writer.write(header);
            writer.write(data);
            log.info("CSV report saved successfully: {}", destination.toAbsolutePath());
            // Return relative path
            return "reports/" + filename;

        } catch (IOException e) {
            log.error("Could not save CSV report: {}", e.getMessage());
            return null;
        }
    }
}