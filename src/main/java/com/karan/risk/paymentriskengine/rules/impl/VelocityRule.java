package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.rules.Rule;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import com.karan.risk.paymentriskengine.rules.velocity.RedisVelocityCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Detects transaction velocity anomalies.
 *
 * Uses Redis-backed sorted sets to track transaction timestamps per sender.
 * This gives us shared state across multiple application instances —
 * critical for distributed deployment.
 *
 * If the same sender initiates more than {@code maxTransactions} payments
 * within {@code windowMinutes}, the rule fires with a rising score.
 */
@Component
public class VelocityRule implements Rule {

    private static final Logger log = LoggerFactory.getLogger(VelocityRule.class);

    private static final String RULE_NAME = "VELOCITY";

    private final RedisVelocityCounter velocityCounter;
    private final int maxTransactions;
    private final Duration window;

    public VelocityRule(
        RedisVelocityCounter velocityCounter,
        @Value("${rules.velocity.max-transactions:5}") int maxTransactions,
        @Value("${rules.velocity.window-minutes:10}") int windowMinutes) {
        this.velocityCounter = velocityCounter;
        this.maxTransactions = maxTransactions;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    @Override
    public String name() {
        return RULE_NAME;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        String senderId = context.payment().getSenderId();

        if (senderId == null || senderId.isBlank()) {
            return RuleResult.pass(RULE_NAME);
        }

        int count = velocityCounter.recordAndCount(senderId, window);

        if (count <= maxTransactions) {
            return RuleResult.pass(RULE_NAME);
        }

        int excess = count - maxTransactions;
        int score = Math.min(100, excess * 20);

        String reason = String.format(
            "Sender %s initiated %d transactions in last %d minutes (limit %d)",
            senderId, count, window.toMinutes(), maxTransactions);

        log.warn("Velocity rule fired: {}", reason);

        return RuleResult.hit(RULE_NAME, score, reason);
    }
}
