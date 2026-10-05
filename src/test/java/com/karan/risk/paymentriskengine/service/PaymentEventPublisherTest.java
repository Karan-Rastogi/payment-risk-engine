package com.karan.risk.paymentriskengine.service;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import com.karan.risk.paymentriskengine.dto.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentEventPublisherTest {

    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private PaymentEventPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new PaymentEventPublisher(kafkaTemplate, "payment.events");

        when(kafkaTemplate.send(anyString(), anyString(), any(PaymentEvent.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("publish sends event with paymentId as key")
    void publish_sendsWithPaymentIdAsKey() {
        Payment payment = samplePayment();

        publisher.publish(payment);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);

        verify(kafkaTemplate, times(1)).send(
            eq("payment.events"),
            keyCaptor.capture(),
            eventCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo(payment.getId().toString());

        PaymentEvent sent = eventCaptor.getValue();
        assertThat(sent.eventType()).isEqualTo(PaymentEvent.TYPE_PAYMENT_DECIDED);
        assertThat(sent.paymentId()).isEqualTo(payment.getId());
        assertThat(sent.senderId()).isEqualTo("USER_001");
        assertThat(sent.receiverId()).isEqualTo("USER_002");
        assertThat(sent.amount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(sent.currency()).isEqualTo("INR");
        assertThat(sent.decision()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("event has unique eventId each time")
    void publish_generatesUniqueEventId() {
        Payment payment = samplePayment();

        publisher.publish(payment);
        publisher.publish(payment);

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(kafkaTemplate, times(2)).send(
            eq("payment.events"), anyString(), eventCaptor.capture());

        var events = eventCaptor.getAllValues();
        assertThat(events.get(0).eventId()).isNotEqualTo(events.get(1).eventId());
    }

    @Test
    @DisplayName("publish with Kafka failure does not throw")
    void publish_handlesKafkaFailureGracefully() {
        when(kafkaTemplate.send(anyString(), anyString(), any(PaymentEvent.class)))
            .thenThrow(new RuntimeException("Kafka unavailable"));

        Payment payment = samplePayment();

        publisher.publish(payment);

        verify(kafkaTemplate, times(1)).send(
            eq("payment.events"), anyString(), any(PaymentEvent.class));
    }

    private Payment samplePayment() {
        Payment p = new Payment();
        p.setId(UUID.randomUUID());
        p.setSenderId("USER_001");
        p.setReceiverId("USER_002");
        p.setAmount(new BigDecimal("5000"));
        p.setCurrency("INR");
        p.setChannel("UPI");
        p.setStatus(PaymentStatus.APPROVED);
        p.setCreatedAt(Instant.now());
        return p;
    }
}
