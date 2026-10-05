package com.karan.risk.paymentriskengine.dto;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published to Kafka when a payment is processed.
 *
 * This is the contract with downstream services — notification,
 * analytics, reporting, case management. All consume from
 * payment.events topic.
 *
 * Schema is intentionally flat and self-contained — consumers should
 * not need to query the database to understand an event.
 */
public record PaymentEvent(
    UUID eventId,
    String eventType,
    UUID paymentId,
    String senderId,
    String receiverId,
    BigDecimal amount,
    String currency,
    String channel,
    PaymentStatus decision,
    Instant occurredAt
) {

    public static final String TYPE_PAYMENT_DECIDED = "PAYMENT_DECIDED";

    public static PaymentEvent from(Payment payment) {
        return new PaymentEvent(
            UUID.randomUUID(),
            TYPE_PAYMENT_DECIDED,
            payment.getId(),
            payment.getSenderId(),
            payment.getReceiverId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getChannel(),
            payment.getStatus(),
            payment.getCreatedAt()
        );
    }
}
