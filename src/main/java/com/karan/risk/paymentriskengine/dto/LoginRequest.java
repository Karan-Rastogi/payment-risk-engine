package com.karan.risk.paymentriskengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "username is required")
    @Size(max = 64)
    String username,

    @NotBlank(message = "password is required")
    @Size(min = 1, max = 128)
    String password
) {
}
