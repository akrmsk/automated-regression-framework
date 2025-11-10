package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;

public interface ReportGenerator {
    /**
     * Generates a report file from a TestResult.
     * @param testResult The result of the test.
     * @param runId A unique ID for this specific test execution.
     * @return The file path to the saved report, or null if failed.
     */
    String generateReport(TestResult testResult, String runId);
}