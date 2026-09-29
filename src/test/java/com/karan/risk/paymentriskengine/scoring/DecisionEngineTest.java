package com.karan.risk.paymentriskengine.scoring;

import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionEngineTest {

    private DecisionEngine decisionEngine;

    @BeforeEach
    void setUp() {
        decisionEngine = new DecisionEngine(30, 70);
    }

    @Test
    @DisplayName("Score 0 -> APPROVED")
    void scoreZero_approved() {
        assertThat(decisionEngine.decide(0)).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("Score 29 (just below review) -> APPROVED")
    void score29_approved() {
        assertThat(decisionEngine.decide(29)).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("Score 30 (boundary) -> REVIEW")
    void score30_review() {
        assertThat(decisionEngine.decide(30)).isEqualTo(PaymentStatus.REVIEW);
    }

    @Test
    @DisplayName("Score 69 (just below decline) -> REVIEW")
    void score69_review() {
        assertThat(decisionEngine.decide(69)).isEqualTo(PaymentStatus.REVIEW);
    }

    @Test
    @DisplayName("Score 70 (boundary) -> DECLINED")
    void score70_declined() {
        assertThat(decisionEngine.decide(70)).isEqualTo(PaymentStatus.DECLINED);
    }

    @Test
    @DisplayName("Score 100 -> DECLINED")
    void score100_declined() {
        assertThat(decisionEngine.decide(100)).isEqualTo(PaymentStatus.DECLINED);
    }
}
