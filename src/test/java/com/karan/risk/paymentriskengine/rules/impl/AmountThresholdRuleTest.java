package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AmountThresholdRuleTest {

    private AmountThresholdRule rule;

    @BeforeEach
    void setUp() {
        rule = new AmountThresholdRule(
            new BigDecimal("10000"),
            new BigDecimal("100000"),
            new BigDecimal("1000000"));
    }

    @Test
    @DisplayName("Amount below elevated -> pass (score 0)")
    void belowElevated_pass() {
        RuleResult result = rule.evaluate(ctx(new BigDecimal("5000")));

        assertThat(result.score()).isZero();
    }

    @Test
    @DisplayName("Amount equal to elevated -> pass (boundary)")
    void equalElevated_pass() {
        RuleResult result = rule.evaluate(ctx(new BigDecimal("10000")));

        assertThat(result.score()).isZero();
    }

    @Test
    @DisplayName("Amount above elevated -> score 30")
    void aboveElevated_score30() {
        RuleResult result = rule.evaluate(ctx(new BigDecimal("50000")));

        assertThat(result.score()).isEqualTo(30);
    }

    @Test
    @DisplayName("Amount above high -> score 60")
    void aboveHigh_score60() {
        RuleResult result = rule.evaluate(ctx(new BigDecimal("500000")));

        assertThat(result.score()).isEqualTo(60);
    }

    @Test
    @DisplayName("Amount above critical -> score 90")
    void aboveCritical_score90() {
        RuleResult result = rule.evaluate(ctx(new BigDecimal("5000000")));

        assertThat(result.score()).isEqualTo(90);
    }

    private RuleContext ctx(BigDecimal amount) {
        Payment p = new Payment();
        p.setAmount(amount);
        p.setCurrency("INR");
        return RuleContext.of(p, null);
    }
}
