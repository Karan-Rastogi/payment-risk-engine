package com.karan.risk.paymentriskengine.dto;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentDetailResponse (
    UUID id,
    String senderId,
    String receiverId,
    BigDecimal amount,
    String currency,
    String channel,
    PaymentStatus status,
    Instant createdAt,
    List<RuleHitResponse> ruleHits
) {
    public static PaymentDetailResponse from(Payment payment, List<RuleHitResponse> hits) {
        return new PaymentDetailResponse(
            payment.getId(),
            payment.getSenderId(),
            payment.getReceiverId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getChannel(),
            payment.getStatus(),
            payment.getCreatedAt(),
            hits
        );
    }
}
