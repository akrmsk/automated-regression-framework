package com.example.test_runner_worker.service;

import com.example.test_runner_worker.annotations.Test;
import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.model.TestRun;
import com.example.test_runner_worker.model.enums.TestRunStatus;
import com.example.test_runner_worker.tests.ApiTests;
import com.example.test_runner_worker.tests.UiTests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.lang.reflect.InvocationTargetException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

@Service
@Slf4j
public class TestExecutorService {

    private final ApiTests apiTests;
    private final UiTests uiTests;
    private final int maxRetries;

    private final ExecutorService executorService;

    public TestExecutorService(ApiTests apiTests,
                               UiTests uiTests,
                               @Value("${test.max-retries:3}") int maxRetries,
                               @Value("${test.parallel-threads:5}") int parallelThreads) {
        this.apiTests = apiTests;
        this.uiTests = uiTests;
        this.maxRetries = maxRetries;

        this.executorService = Executors.newFixedThreadPool(parallelThreads);
        log.info("Initialized TestExecutorService with a thread pool of {}", parallelThreads);
    }

    /**
     * Finds and executes all tests matching the tags from the TestRun job.
     * This version runs tests in parallel using a thread pool.
     */
    public TestResult executeTest(TestRun testRun) {
        log.info("Test execution has started for run: {}", testRun.getId());
        List<String> requestedTags = Arrays.stream(testRun.getTags().toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        // 1. Find all @Test methods that match the tags
        List<Method> testsToRun = findTestsByTags(requestedTags);

        if (testsToRun.isEmpty()) {
            log.warn("No tests found for tags: {}. Marking as failed.", requestedTags);
            TestResult result = new TestResult();
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage("No tests found for the specified tags: " + requestedTags);
            return result;
        }


        // 2. Submit each test to the executor service
        List<Future<TestResult>> futures = new ArrayList<>();
        for (Method testMethod : testsToRun) {
            // Submit a task (as a lambda) to the thread pool
            // The task will call runSingleTestWithRetries for its assigned testMethod
            Future<TestResult> future = executorService.submit(() -> runSingleTestWithRetries(testMethod));
            futures.add(future);
        }

        log.info("Submitted {} tests to the thread pool for run {}", futures.size(), testRun.getId());

        // 3. Aggregate results from all futures
        TestResult finalResult = new TestResult();
        finalResult.setStatus(TestRunStatus.COMPLETED);

        int failureCount = 0;
        StringBuilder allErrors = new StringBuilder();
        String finalReportUrl = null;

        // Iterate over the futures and call .get()
        // This blocks until each test is complete
        for (Future<TestResult> future : futures) {
            try {
                TestResult singleTestResult = future.get(); // Get the result from the completed test

                // Aggregate results (same logic as before)
                if (singleTestResult.getStatus() == TestRunStatus.FAILED) {
                    failureCount++;
                    allErrors.append("[").append(singleTestResult.getTestType()).append("]: ") // Added test type for clarity
                            .append(singleTestResult.getErrorMessage()).append("\n");
                }
                // Save the report URL.
                // NOTE: This still only saves one URL.
                // We'll address this when we create a summary report.
                finalReportUrl = singleTestResult.getReportUrl();

            } catch (InterruptedException | ExecutionException e) {
                log.error("Critical error retrieving test result from future", e);
                failureCount++;
                allErrors.append("[Test Execution Error]: Failed to retrieve result from thread: ").append(e.getMessage()).append("\n");
            }
        }

        // 4. Set the final aggregated result
        finalResult.setFailedTestCount(failureCount);
        if (failureCount > 0) {
            finalResult.setStatus(TestRunStatus.FAILED);
            finalResult.setErrorMessage(allErrors.toString());
        }
        finalResult.setReportUrl(finalReportUrl);

        log.info("Test execution finished for run {}. Final Status: {}, Failed: {}",
                testRun.getId(), finalResult.getStatus(), finalResult.getFailedTestCount());

        return finalResult;
    }

    /**
     * Runs a single test method with retry logic.
     * This method is thread-safe as-is because it operates on its own testMethod
     * and doesn't share state with other running tests.
     */
    private TestResult runSingleTestWithRetries(Method testMethod) {
        TestResult lastResult = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Attempt {} of {} for test '{}'", attempt, maxRetries, testMethod.getName());
            try {
                Object testInstance = getTestInstance(testMethod.getDeclaringClass());

                lastResult = (TestResult) testMethod.invoke(testInstance);

                // Add test name to the result for better error messages
                lastResult.setTestType(testMethod.getName());

                if (lastResult.getStatus() == TestRunStatus.COMPLETED) {
                    log.info("Test '{}' passed on attempt {}", testMethod.getName(), attempt);
                    return lastResult;
                }

            } catch (InvocationTargetException e) {
                log.error("Test method threw an internal exception: {}", e.getTargetException().getMessage());
                lastResult = new TestResult();
                lastResult.setStatus(TestRunStatus.FAILED);
                lastResult.setErrorMessage("Test invocation failed: " + e.getTargetException().getMessage());
                lastResult.setTestType(testMethod.getName());
            } catch (Exception e) {
                log.error("An unexpected error occurred during test execution on attempt {}", attempt, e);
                lastResult = new TestResult();
                lastResult.setStatus(TestRunStatus.FAILED);
                lastResult.setErrorMessage("Exception during test invocation: " + e.getMessage());
                lastResult.setTestType(testMethod.getName());
            }

            if (attempt < maxRetries) {
                log.warn("Test '{}' failed on attempt {}. Retrying...", testMethod.getName(), attempt);
            }
        }

        log.error("Test '{}' failed after {} attempts.", testMethod.getName(), maxRetries);
        return lastResult; // Return the last failed result
    }

    /**
     * Helper to find all methods with our @Test annotation.
     */
    private List<Method> findTestsByTags(List<String> requestedTags) {
        Stream<Method> uiTestMethods = Arrays.stream(UiTests.class.getMethods())
                .filter(method -> method.isAnnotationPresent(Test.class));

        Stream<Method> apiTestMethods = Arrays.stream(ApiTests.class.getMethods())
                .filter(method -> method.isAnnotationPresent(Test.class));

        return Stream.concat(uiTestMethods, apiTestMethods)
                .filter(method -> {
                    Test testAnnotation = method.getAnnotation(Test.class);
                    List<String> methodTags = Arrays.asList(testAnnotation.tags());
                    return methodTags.containsAll(requestedTags);
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper to get the correct service instance (ApiTests or UiTests).
     */
    private Object getTestInstance(Class<?> testClass) {
        if (testClass.equals(UiTests.class)) {
            return uiTests;
        } else if (testClass.equals(ApiTests.class)) {
            return apiTests;
        }
        throw new IllegalArgumentException("Unknown test class: " + testClass.getName());
    }
}