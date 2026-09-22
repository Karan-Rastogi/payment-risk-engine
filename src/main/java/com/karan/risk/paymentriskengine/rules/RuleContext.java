package com.karan.risk.paymentriskengine.rules;

import com.karan.risk.paymentriskengine.domain.Payment;

import java.time.Instant;

/**
 * Immutable context passed to every rule.
 *
 * @param payment         the payment under evaluation
 * @param evaluationTime  when the evaluation is happening
 * @param homeCountry     the sender's registered/home country (ISO 3166 alpha-2), nullable
 */
public record RuleContext(
    Payment payment,
    Instant evaluationTime,
    String homeCountry
) {
    public static RuleContext of(Payment payment) {
        return new RuleContext(payment, Instant.now(), null);
    }
    public static RuleContext of(Payment payment, String homeCountry) {
        return new RuleContext(payment, Instant.now(), homeCountry);
    }
}
