package com.karan.risk.paymentriskengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration properties, bound from spring.security.jwt.*
 *
 * @param secret        HMAC-SHA256 signing key (min 32 chars)
 * @param expirationMs  token validity in milliseconds
 * @param issuer        issuer claim in the token
 */
@ConfigurationProperties(prefix = "spring.security.jwt")
public record JwtProperties(
    String secret,
    long expirationMs,
    String issuer
) {
}
