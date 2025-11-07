package com.example.test_runner_worker.listener;

import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.dtos.TestRunUpdateDto;
import com.example.test_runner_worker.model.TestRun;
import com.example.test_runner_worker.service.TestExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RabbitMQListener {

    private final RestTemplate restTemplate;
    private final TestExecutorService executorService;
    private final String apiBaseUrl;

    // Correct Constructor Injection
    public RabbitMQListener(RestTemplate restTemplate,
                            TestExecutorService executorService,
                            @Value("${api.base.url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.executorService = executorService;
        this.apiBaseUrl = apiBaseUrl;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(TestRun testRun) {
        log.info("Received the test run: {}", testRun.getId());

        // 1. Delegate the hard work to the executor
        // The executor will ALWAYS return a result, never an exception.
        TestResult result = executorService.executeTest(testRun);

        // 2. Create the DTO to send back to the API
        TestRunUpdateDto testRunUpdateDto = new TestRunUpdateDto();

        // --- THIS IS THE FIX ---
        // Copy ALL fields from the result to the DTO
        testRunUpdateDto.setStatus(result.getStatus());
        testRunUpdateDto.setFailedTestCount(result.getFailedTestCount());
        testRunUpdateDto.setReportUrl(result.getReportUrl());
        testRunUpdateDto.setErrorMessage(result.getErrorMessage());
        // --- END OF FIX ---

        // 3. Build the URL and send the update
        try {
            String updateUrl = apiBaseUrl + "/api/runs/" + testRun.getId();
            restTemplate.put(updateUrl, testRunUpdateDto);
            log.info("Successfully processed and reported results for job: {}", testRun.getId());

        } catch (Exception e) {
            // If the API is down, we can't report the result.
            // This is a "final" failure. We log it and let the message be consumed.
            log.error("CRITICAL: Failed to report results back to API for job {}. Error: {}",
                    testRun.getId(), e.getMessage());
        }
    }
}