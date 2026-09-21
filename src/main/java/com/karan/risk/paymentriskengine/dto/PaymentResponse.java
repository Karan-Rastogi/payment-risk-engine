package com.karan.risk.paymentriskengine.dto;

import com.karan.risk.paymentriskengine.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    String senderId,
    String receiverId,
    BigDecimal amount,
    String currency,
    String channel,
    PaymentStatus status,
    Instant createdAt
) {}
