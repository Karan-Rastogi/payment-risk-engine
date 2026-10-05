package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import com.karan.risk.paymentriskengine.rules.velocity.RedisVelocityCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class VelocityRuleTest {

    private RedisVelocityCounter velocityCounter;
    private VelocityRule rule;

    @BeforeEach
    void setUp() {
        velocityCounter = Mockito.mock(RedisVelocityCounter.class);
        // 3 transactions max, 10-minute window
        rule = new VelocityRule(velocityCounter, 3, 10);
    }

    @Test
    @DisplayName("Under limit -> pass")
    void underLimit_pass() {
        when(velocityCounter.recordAndCount(eq("S1"), any(Duration.class)))
            .thenReturn(3);

        RuleResult result = rule.evaluate(ctx("S1"));

        assertThat(result.score()).isZero();
    }

    @Test
    @DisplayName("Exceed limit -> fires with rising score")
    void exceedLimit_fires() {
        when(velocityCounter.recordAndCount(eq("S2"), any(Duration.class)))
            .thenReturn(4);

        RuleResult result = rule.evaluate(ctx("S2"));

        assertThat(result.score()).isEqualTo(20); // 1 excess * 20
    }

    @Test
    @DisplayName("Different senders are independent (Redis-side)")
    void differentSenders_independent() {
        when(velocityCounter.recordAndCount(eq("A"), any(Duration.class)))
            .thenReturn(3);
        when(velocityCounter.recordAndCount(eq("B"), any(Duration.class)))
            .thenReturn(1);

        assertThat(rule.evaluate(ctx("A")).score()).isZero();
        assertThat(rule.evaluate(ctx("B")).score()).isZero();
    }

    @Test
    @DisplayName("Redis failure -> treated as 0 (fail-safe)")
    void redisFailure_pass() {
        when(velocityCounter.recordAndCount(eq("S3"), any(Duration.class)))
            .thenReturn(0);

        RuleResult result = rule.evaluate(ctx("S3"));

        assertThat(result.score()).isZero();
    }

    @Test
    @DisplayName("Null sender -> pass without Redis call")
    void nullSender_pass() {
        Payment p = new Payment();
        p.setSenderId(null);

        RuleResult result = rule.evaluate(new RuleContext(p, Instant.now(), null));

        assertThat(result.score()).isZero();
        Mockito.verify(velocityCounter, Mockito.never())
            .recordAndCount(ArgumentMatchers.anyString(), any(Duration.class));
    }

    private RuleContext ctx(String senderId) {
        Payment p = new Payment();
        p.setSenderId(senderId);
        return new RuleContext(p, Instant.now(), null);
    }
}
