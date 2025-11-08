package com.example.test_runner_worker.service;

import com.example.test_runner_worker.dtos.TestResult;

/**
 * Interface for all report generators.
 */
public interface ReportGenerator {
    
    /**
     * Generates a report for the given test result.
     *
     * @param testResult the test result to generate a report for
     * @param runId the unique identifier for the test run
     * @return the path to the generated report
     */
    String generateReport(TestResult testResult, String runId);
}