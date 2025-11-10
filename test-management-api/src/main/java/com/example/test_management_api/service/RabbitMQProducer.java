package com.example.test_management_api.service;

import com.example.test_management_api.model.TestRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTestRunJob(TestRun testRun) {
        log.info("Sending job to RabbitMQ: Run ID {}", testRun.getId());

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                testRun // Send the entire TestRun object as JSON
        );

        log.info("Job sent successfully for Run ID {}", testRun.getId());
    }
}
