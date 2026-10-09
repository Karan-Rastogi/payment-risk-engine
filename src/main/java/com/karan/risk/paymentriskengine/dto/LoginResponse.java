package com.karan.risk.paymentriskengine.dto;

public record LoginResponse(
    String token,
    String type,
    long expiresIn
) {
    public static LoginResponse bearer(String token, long expiresInSeconds) {
        return new LoginResponse(token, "Bearer", expiresInSeconds);
    }
}
