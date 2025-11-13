package com.example.test_management_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// REMOVE these imports
// import org.springframework.amqp.core.Binding;
// import org.springframework.amqp.core.BindingBuilder;
// import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // REMOVE these @Value fields
    // @Value("${rabbitmq.queue.name}")
    // private String queueName;
    //
    // @Value("${rabbitmq.routing.key}")
    // private String routingKey;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;


    // --- REMOVE THIS BEAN ---
    // @Bean
    // public Queue queue() {
    //     // We will add DLQ logic here later
    //     return new Queue(queueName, true);
    // }

    // --- KEEP THIS BEAN ---
    // Define the exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    // --- REMOVE THIS BEAN ---
    // Bind the queue to the exchange
    // @Bean
    // public Binding binding(Queue queue, TopicExchange exchange) {
    //     return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    // }

    // Set up JSON message conversion (for sending Java objects)
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // This module is critical for sending Java 8+ dates (like LocalDateTime)
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // Configure the RabbitTemplate to use our JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}