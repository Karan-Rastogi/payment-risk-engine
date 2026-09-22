package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.rules.Rule;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Detects transaction velocity anomalies.
 * If the same sender initiates more than {@code maxTransactions} payments
 * within {@code windowMinutes}, the rule fires with a rising score.
 * NOTE: Uses in-memory storage for now. Migrated to Redis in a later module
 * for distributed, cross-instance consistency.
 */
@Component
public class VelocityRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(VelocityRule.class);

    private static final String RULE_NAME = "VELOCITY";

    private final int maxTransactions;
    private final Duration window;

    // senderId -> timestamps of recent transactions
    private final Map<String, Deque<Instant>> history = new ConcurrentHashMap<>();

    public VelocityRule(
        @Value("${rules.velocity.max-transactions:5}") int maxTransactions,
        @Value("${rules.velocity.window-minutes:10}") int windowMinutes) {
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
        Instant now = context.evaluationTime();
        Instant cutoff = now.minus(window);

        Deque<Instant> timestamps = history.computeIfAbsent(senderId,
            k -> new ConcurrentLinkedDeque<>());

        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }

        timestamps.addLast(now);

        int count = timestamps.size();

        if (count <= maxTransactions) {
            return RuleResult.pass(RULE_NAME);
        }

        int excess = count - maxTransactions;
        int score = Math.min(100, excess*20);

        String reason = String.format(
            "Sender %s initiated %d transactions in last %d minutes (limit %d)",
            senderId, count, window.toMinutes(), maxTransactions);

        log.warn("Velocity rule fired: {}", reason);

        return RuleResult.hit(RULE_NAME, score, reason);
    }
}
