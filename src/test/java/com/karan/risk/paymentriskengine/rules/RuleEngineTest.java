package com.karan.risk.paymentriskengine.rules;

import com.karan.risk.paymentriskengine.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    @Test
    @DisplayName("aggregateScore sums rule scores")
    void aggregateScore_sumsCorrectly() {
        RuleEngine engine = new RuleEngine(List.of());

        List<RuleResult> results = List.of(
            RuleResult.hit("R1", 30, "x"),
            RuleResult.hit("R2", 40, "y"),
            RuleResult.pass("R3"));

        assertThat(engine.aggregateScore(results)).isEqualTo(70);
    }

    @Test
    @DisplayName("aggregateScore caps at 100")
    void aggregateScore_capsAt100() {
        RuleEngine engine = new RuleEngine(List.of());

        List<RuleResult> results = List.of(
            RuleResult.hit("R1", 80, "x"),
            RuleResult.hit("R2", 60, "y"),
            RuleResult.hit("R3", 50, "z"));

        assertThat(engine.aggregateScore(results)).isEqualTo(100);
    }

    @Test
    @DisplayName("Empty rule list -> score 0")
    void aggregateScore_emptyList() {
        RuleEngine engine = new RuleEngine(List.of());

        assertThat(engine.aggregateScore(List.of())).isZero();
    }

    @Test
    @DisplayName("Throwing rule is treated as pass (fail-safe)")
    void evaluateAll_throwingRuleTreatedAsPass() {
        Rule throwingRule = new Rule() {
            @Override
            public String name() { return "THROWING"; }

            @Override
            public RuleResult evaluate(RuleContext context) {
                throw new RuntimeException("boom");
            }
        };

        RuleEngine engine = new RuleEngine(List.of(throwingRule));
        Payment p = new Payment();
        p.setSenderId("S1");

        List<RuleResult> results = engine.evaluateAll(RuleContext.of(p, null));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).score()).isZero();
        assertThat(results.get(0).ruleName()).isEqualTo("THROWING");
    }
}
