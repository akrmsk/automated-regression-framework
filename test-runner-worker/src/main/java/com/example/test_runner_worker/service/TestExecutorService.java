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

    public TestResult executeTest(TestRun testRun) {
        log.info("Test execution has started for run: {}", testRun.getId());
        List<String> requestedTags = Arrays.stream(testRun.getTags().toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        List<Method> testsToRun = findTestsByTags(requestedTags);

        if (testsToRun.isEmpty()) {
            log.warn("No tests found for tags: {}. Marking as failed.", requestedTags);
            TestResult result = new TestResult();
            result.setStatus(TestRunStatus.FAILED);
            result.setFailedTestCount(1);
            result.setErrorMessage("No tests found for the specified tags: " + requestedTags);
            return result;
        }

        TestResult finalResult = new TestResult();
        finalResult.setStatus(TestRunStatus.COMPLETED);
        finalResult.setFailedTestCount(0);

        for (Method testMethod : testsToRun) {
            TestResult singleTestResult = runSingleTestWithRetries(testMethod);
            if (singleTestResult.getStatus() == TestRunStatus.FAILED) {
                finalResult.setStatus(TestRunStatus.FAILED);
                finalResult.setFailedTestCount(finalResult.getFailedTestCount() + 1);
            }
        }

        return finalResult;
    }

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

    private TestResult runSingleTestWithRetries(Method testMethod) {
        TestResult lastResult = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Attempt {} of {} for test '{}'", attempt, maxRetries, testMethod.getName());
            try {
                Object testInstance = getTestInstance(testMethod.getDeclaringClass());
                lastResult = (TestResult) testMethod.invoke(testInstance);

                if (lastResult.getStatus() == TestRunStatus.COMPLETED) {
                    log.info("Test '{}' passed on attempt {}", testMethod.getName(), attempt);
                    return lastResult;
                }
            } catch (Exception e) {
                log.error("An unexpected error occurred during test execution on attempt {}", attempt, e);
                lastResult = new TestResult();
                lastResult.setStatus(TestRunStatus.FAILED);
                lastResult.setFailedTestCount(1);
                lastResult.setErrorMessage("Exception during test execution: " + e.getMessage());
            }

            if (attempt < maxRetries) {
                log.warn("Test '{}' failed on attempt {}. Retrying...", testMethod.getName(), attempt);
            }
        }

        log.error("Test '{}' failed after {} attempts.", testMethod.getName(), maxRetries);
        return lastResult;
    }

    private Object getTestInstance(Class<?> testClass) {
        if (testClass.equals(UiTests.class)) {
            return uiTests;
        } else if (testClass.equals(ApiTests.class)) {
            return apiTests;
        }
        throw new IllegalArgumentException("Unknown test class: " + testClass.getName());
    }
}
