package com.karan.risk.paymentriskengine.service;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes payment decision events to Kafka.
 *
 * The event key is the paymentId — this ensures all events for the
 * same payment go to the same partition, preserving order per key.
 *
 * Publishing is asynchronous — the method returns immediately after
 * handing the message to Kafka's send buffer. Callbacks handle success/failure.
 */
@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final String topic;

    public PaymentEventPublisher(
        KafkaTemplate<String, PaymentEvent> kafkaTemplate,
        @Value("${spring.kafka.topic.payment-events:payment.events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Publish a PaymentEvent for the given payment.
     * Non-blocking. Failures are logged but do not roll back the payment.
     *
     * @param payment the persisted payment entity
     */
    public void publish(Payment payment) {
        PaymentEvent event = PaymentEvent.from(payment);
        String key = payment.getId().toString();

        try {
            CompletableFuture<SendResult<String, PaymentEvent>> future =
                kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish PaymentEvent for payment={} eventType={}",
                        payment.getId(), event.eventType(), ex);
                } else {
                    log.info("Published PaymentEvent: payment={} decision={} partition={} offset={}",
                        payment.getId(),
                        payment.getStatus(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

        } catch (Exception ex) {
            // Fail-safe: Kafka producer might throw synchronously if send buffer is full
            log.error("Unexpected error publishing PaymentEvent for payment={}", payment.getId(), ex);
        }
    }
}
