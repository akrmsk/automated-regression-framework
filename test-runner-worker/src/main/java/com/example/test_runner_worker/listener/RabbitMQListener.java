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

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleMessage(TestRun testRun) {
        log.info("Received the test run: {}", testRun.getId());

        TestResult result = executorService.executeTest(testRun);

        TestRunUpdateDto testRunUpdateDto = new TestRunUpdateDto();
        testRunUpdateDto.setStatus(result.getStatus());
        testRunUpdateDto.setFailedTestCount(result.getFailedTestCount());
        testRunUpdateDto.setReportUrl(result.getReportUrl());
        testRunUpdateDto.setErrorMessage(result.getErrorMessage());
        testRunUpdateDto.setScreenshotPath(result.getScreenshotPath());

        try {
            retryTemplate.execute(context -> {
                String updateUrl = apiBaseUrl + "/api/runs/" + testRun.getId();
                log.info("Attempt {} to report results for job {}", context.getRetryCount() + 1, testRun.getId());
                restTemplate.put(updateUrl, testRunUpdateDto);
                log.info("Successfully reported results for job: {}", testRun.getId());
                return null;
            });
        } catch (Exception e) {
            log.error("CRITICAL: Failed to report results back to API for job {} after multiple retries. Sending to DLQ.",
                    testRun.getId(), e);
            throw new AmqpRejectAndDontRequeueException("Failed to report results to API", e);
        }
    }
}
