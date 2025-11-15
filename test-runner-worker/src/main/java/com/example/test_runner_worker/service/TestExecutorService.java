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

// --- ADD THIS IMPORT ---
import java.lang.reflect.InvocationTargetException;

@Service
@Slf4j
public class TestExecutorService {

    private final ApiTests apiTests;
    private final UiTests uiTests;
    private final int maxRetries;

    public TestExecutorService(ApiTests apiTests, UiTests uiTests, @Value("${test.max-retries:3}") int maxRetries) {
        this.apiTests = apiTests;
        this.uiTests = uiTests;
        this.maxRetries = maxRetries;
    }

    /**
     * Finds and executes all tests matching the tags from the TestRun job.
     * Note: This is still sequential! We will parallelize this in the next step.
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

        // 2. Execute each test one-by-one (sequentially for now)
        TestResult finalResult = new TestResult();
        finalResult.setStatus(TestRunStatus.COMPLETED);

        int failureCount = 0;
        StringBuilder allErrors = new StringBuilder();
        String finalReportUrl = null;

        for (Method testMethod : testsToRun) {
            TestResult singleTestResult = runSingleTestWithRetries(testMethod);

            // Aggregate results
            if (singleTestResult.getStatus() == TestRunStatus.FAILED) {
                failureCount++;
                allErrors.append("[").append(testMethod.getName()).append("]: ")
                        .append(singleTestResult.getErrorMessage()).append("\n");
            }
            // Save the report URL (we'll just grab the last one for now)
            // This will now correctly get the URL even if the test failed
            finalReportUrl = singleTestResult.getReportUrl();
        }

        // 3. Set the final aggregated result
        finalResult.setFailedTestCount(failureCount);
        if (failureCount > 0) {
            finalResult.setStatus(TestRunStatus.FAILED);
            finalResult.setErrorMessage(allErrors.toString());
        }
        finalResult.setReportUrl(finalReportUrl); // Set the URL of the last run report

        log.info("Test execution finished for run {}. Final Status: {}, Failed: {}",
                testRun.getId(), finalResult.getStatus(), finalResult.getFailedTestCount());

        return finalResult;
    }

    /**
     * Runs a single test method with retry logic.
     */
    private TestResult runSingleTestWithRetries(Method testMethod) {
        TestResult lastResult = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Attempt {} of {} for test '{}'", attempt, maxRetries, testMethod.getName());
            try {
                // Find which class (ApiTests or UiTests) this method belongs to
                Object testInstance = getTestInstance(testMethod.getDeclaringClass());

                // Run the test
                // Both ApiTests and UiTests are guaranteed to return a TestResult
                // and not throw an exception.
                lastResult = (TestResult) testMethod.invoke(testInstance);

                // If it passed, return immediately
                if (lastResult.getStatus() == TestRunStatus.COMPLETED) {
                    log.info("Test '{}' passed on attempt {}", testMethod.getName(), attempt);
                    return lastResult;
                }

                // --- CATCH BLOCK MODIFIED ---
                // This logic is now safer. It handles reflection errors, but
                // assumes the test method itself (runUiSearchTest) will *always*
                // catch its own errors and return a valid TestResult.
            } catch (InvocationTargetException e) {
                log.error("Test method threw an internal exception: {}", e.getTargetException().getMessage());
                // This should not happen if tests catch their own Throwables, but as a fallback:
                lastResult = new TestResult();
                lastResult.setStatus(TestRunStatus.FAILED);
                lastResult.setErrorMessage("Test invocation failed: " + e.getTargetException().getMessage());
            } catch (Exception e) {
                log.error("An unexpected error occurred during test execution on attempt {}", attempt, e);
                lastResult = new TestResult();
                lastResult.setStatus(TestRunStatus.FAILED);
                lastResult.setErrorMessage("Exception during test invocation: " + e.getMessage());
            }
            // --- END MODIFICATION ---

            if (attempt < maxRetries) {
                log.warn("Test '{}' failed on attempt {}. Retrying...", testMethod.getName(), attempt);
            }
        }

        log.error("Test '{}' failed after {} attempts.", testMethod.getName(), maxRetries);
        return lastResult; // Return the last failed result (which will have a reportUrl)
    }

    /**
     * Helper to find all methods with our @Test annotation.
     */
    private List<Method> findTestsByTags(List<String> requestedTags) {
        // Get all methods from UiTests class
        Stream<Method> uiTestMethods = Arrays.stream(UiTests.class.getMethods())
                .filter(method -> method.isAnnotationPresent(Test.class));

        // Get all methods from ApiTests class
        Stream<Method> apiTestMethods = Arrays.stream(ApiTests.class.getMethods())
                .filter(method -> method.isAnnotationPresent(Test.class));

        // Combine them and filter by tag
        return Stream.concat(uiTestMethods, apiTestMethods)
                .filter(method -> {
                    Test testAnnotation = method.getAnnotation(Test.class);
                    List<String> methodTags = Arrays.asList(testAnnotation.tags());
                    // Check if the method's tags contain all the requested tags
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