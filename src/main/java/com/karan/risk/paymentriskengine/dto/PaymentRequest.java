package com.karan.risk.paymentriskengine.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest (
    @NotBlank(message = "senderId is required")
    @Size(max = 64, message = "senderId must not exceed 64 characters")
    String senderId,


    @NotBlank(message = "receiverId is required")
    @Size(max = 64, message = "receiverId must not exceed 64 characters")
    String receiverId,

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "amount must have up to 15 integer digits and 4 decimal places")
    BigDecimal amount,


    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    String currency,

    @NotBlank(message = "channel is required")
    @Pattern(regexp = "UPI|CARD|WIRE|SWIFT", message = "channel must be one of UPI, CARD, WIRE, SWIFT")
    String channel,

    @Size(max = 45, message = "ipAddress must not exceed 45 characters")
    String ipAddress,

    @Size(max = 128, message = "deviceId must not exceed 128 characters")
    String deviceId

){}
