package com.example.test_runner_worker.listener;

import com.example.test_runner_worker.dtos.TestResult;
import com.example.test_runner_worker.dtos.TestRunUpdateDto;
import com.example.test_runner_worker.model.TestRun;
import com.example.test_runner_worker.service.TestExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RabbitMQListener {

    private final RestTemplate restTemplate;
    private final TestExecutorService executorService;
    private final String apiBaseUrl;
    private final RetryTemplate retryTemplate;

    public RabbitMQListener(RestTemplate restTemplate,
                            TestExecutorService executorService,
                            @Value("${api.base.url}") String apiBaseUrl,
                            RetryTemplate retryTemplate) {
        this.restTemplate = restTemplate;
        this.executorService = executorService;
        this.apiBaseUrl = apiBaseUrl;
        this.retryTemplate = retryTemplate;
    }

    // This method listens to the queue specified in application.properties
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(TestRun testRun) {
        log.info("Received job from queue: Run ID {}", testRun.getId());

        // 1. Execute the test(s)
        TestResult result = executorService.executeTest(testRun);

        // 2. Prepare the update DTO to send back to the API
        TestRunUpdateDto updateDto = new TestRunUpdateDto();
        updateDto.setStatus(result.getStatus());
        updateDto.setFailedTestCount(result.getFailedTestCount());
        updateDto.setReportUrl(result.getReportUrl());
        updateDto.setErrorMessage(result.getErrorMessage());
        updateDto.setScreenshotPath(result.getScreenshotPath());

        // 3. Send the result back to the API (with retry)
        try {
            retryTemplate.execute(context -> {
                String updateUrl = apiBaseUrl + "/api/runs/" + testRun.getId();
                log.info("Attempt {} to report results for job {}", context.getRetryCount() + 1, testRun.getId());
                // We will need to create this PUT endpoint on the API side next
                restTemplate.put(updateUrl, updateDto);
                log.info("Successfully reported results for job: {}", testRun.getId());
                return null; // Return null to indicate success
            });
        } catch (Exception e) {
            log.error("CRITICAL: Failed to report results to API for job {} after 3 attempts.", testRun.getId(), e);
            // This will reject the message and (if configured) send it to the Dead-Letter Queue
            throw new AmqpRejectAndDontRequeueException("Failed to report results to API", e);
        }
    }
}