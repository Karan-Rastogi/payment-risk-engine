package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VelocityRuleTest {

    private VelocityRule rule;

    @BeforeEach
    void setUp() {
        // 3 transactions max, 10-minute window
        rule = new VelocityRule(3, 10);
    }

    @Test
    @DisplayName("Under limit -> pass")
    void underLimit_pass() {
        RuleResult result = null;
        for (int i = 0; i < 3; i++) {
            result = rule.evaluate(ctx("S1"));
        }

        assertThat(result).isNotNull();
        assertThat(result.score()).isZero();
    }

    @Test
    @DisplayName("Exceed limit -> fires with rising score")
    void exceedLimit_fires() {
        for (int i = 0; i < 3; i++) {
            rule.evaluate(ctx("S2"));
        }

        RuleResult result = rule.evaluate(ctx("S2"));

        assertThat(result.score()).isEqualTo(20); // 1 excess * 20
    }

    @Test
    @DisplayName("Different senders have independent counters")
    void differentSenders_independentCounters() {
        for (int i = 0; i < 3; i++) {
            rule.evaluate(ctx("A"));
        }

        RuleResult result = rule.evaluate(ctx("B"));

        assertThat(result.score()).isZero();
    }

    private RuleContext ctx(String senderId) {
        Payment p = new Payment();
        p.setSenderId(senderId);
        return new RuleContext(p, Instant.now(), null);
    }
}
