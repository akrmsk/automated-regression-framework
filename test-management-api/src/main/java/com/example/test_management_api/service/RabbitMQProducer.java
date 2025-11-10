package com.example.test_management_api.service;

import com.example.test_management_api.model.TestRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j // Creates the 'log' variable
@RequiredArgsConstructor // Creates a constructor for all 'final' fields
public class RabbitMQProducer {

    // This is now final, so RequiredArgsConstructor will inject it
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // The @Autowired constructor is no longer needed

    public void sendTestRunJob(TestRun testRun) {
        // The 'log' variable will now be found
        log.info("Sending job to RabbitMQ: Run ID {}", testRun.getId());

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                testRun // Send the entire TestRun object as JSON
        );

        log.info("Job sent successfully for Run ID {}", testRun.getId());
    }
}