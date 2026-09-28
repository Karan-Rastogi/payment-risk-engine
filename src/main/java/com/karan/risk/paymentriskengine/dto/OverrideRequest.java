package com.karan.risk.paymentriskengine.dto;

import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OverrideRequest(
    @NotNull(message = "status is required")
    PaymentStatus status,

    @NotBlank(message = "reason is required")
    @Size(max = 500, message = "reason must not exceed 500 characters")
    String reason
) {
}
