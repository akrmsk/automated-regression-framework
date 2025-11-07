package com.example.test_management_api.service;

import com.example.test_management_api.model.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // <-- Import @Value
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    private final RabbitTemplate rabbitTemplate;

    // --- ADD THESE FIELDS ---
    // These will be injected from application.properties
    private final String exchangeName;
    private final String routingKey;

    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate,
                            // Inject the values from properties
                            @Value("${rabbitmq.exchange.name}") String exchangeName,
                            @Value("${rabbitmq.routing.key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public void sendTestRunJob(TestRun testRun) {
        // Use the new fields, not the old static variables
        LOGGER.info(String.format("Sending message to exchange [%s] with routing key [%s] -> %s",
                exchangeName, routingKey, testRun.toString()));

        rabbitTemplate.convertAndSend(
                exchangeName, // <-- Use the field
                routingKey,   // <-- Use the field
                testRun
        );
    }
}