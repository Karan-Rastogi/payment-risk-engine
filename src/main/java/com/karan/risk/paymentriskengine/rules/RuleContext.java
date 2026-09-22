package com.karan.risk.paymentriskengine.rules;

import com.karan.risk.paymentriskengine.domain.Payment;

import java.time.Instant;

/**
 * Immutable context passed to every rule.
 * Contains the payment under evaluation plus any auxiliary data (history, geo, etc.)
 * that rules need. Extended in later modules (Redis history, geo lookup, blacklist cache).
 */
public record RuleContext(
    Payment payment,
    Instant evaluationTime
) {
    public static RuleContext of(Payment payment) {
        return new RuleContext(payment, Instant.now());
    }
}
