package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CsvReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String REPORTS_DIRECTORY = "/app/reports";
    private static final String CSV_HEADER = "Run ID,Status,Test Type,Description,URL,Parameters,Start Time,End Time,Duration (ms),Failed Tests,Error Message\n";

    @Override
    public String generateReport(TestResult testResult, String runId) {
        String filename = "report-" + runId + ".csv";
        Path destination = Paths.get(REPORTS_DIRECTORY, filename);

        try (FileWriter writer = new FileWriter(destination.toFile())) {
            // Write CSV header
            writer.write(CSV_HEADER);
            
            // Write test result data
            StringBuilder csvLine = new StringBuilder();
            csvLine.append(runId).append(",");
            csvLine.append(testResult.getStatus()).append(",");
            csvLine.append(escapeField(testResult.getTestType())).append(",");
            csvLine.append(escapeField(testResult.getTestDescription())).append(",");
            csvLine.append(escapeField(testResult.getTestUrl())).append(",");
            csvLine.append(escapeField(testResult.getTestParameters())).append(",");
            csvLine.append(formatDateTime(testResult.getStartTime())).append(",");
            csvLine.append(formatDateTime(testResult.getEndTime())).append(",");
            csvLine.append(testResult.getDurationMs()).append(",");
            csvLine.append(testResult.getFailedTestCount()).append(",");
            csvLine.append(escapeField(testResult.getErrorMessage()));
            csvLine.append("\n");
            
            writer.write(csvLine.toString());
            
            log.info("CSV report saved successfully: {}", destination);
            return destination.toString();
        } catch (IOException e) {
            log.error("Could not save CSV report: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Escapes special characters in CSV fields.
     * If the field contains commas, quotes, or newlines, it is enclosed in quotes.
     * Any quotes within the field are doubled.
     */
    private String escapeField(String field) {
        if (field == null) {
            return "";
        }
        
        // If the field contains commas, quotes, or newlines, enclose it in quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Double any quotes in the field
            String escapedField = field.replace("\"", "\"\"");
            return "\"" + escapedField + "\"";
        }
        
        return field;
    }
    
    /**
     * Formats a date-time value for CSV output.
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }
}