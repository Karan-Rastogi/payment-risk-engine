package com.karan.risk.paymentriskengine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration.
 *
 * Defines the payment.events topic — Spring Boot auto-configures
 * the KafkaTemplate and producer factories from spring.kafka.* properties.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.topic.payment-events:payment.events}")
    private String paymentEventsTopic;

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentEventsTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
