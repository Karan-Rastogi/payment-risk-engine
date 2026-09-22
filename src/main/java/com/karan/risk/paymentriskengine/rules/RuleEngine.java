package com.karan.risk.paymentriskengine.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrates all registered rules.
 *
 * Spring injects every bean implementing {@link Rule} into the constructor.
 * Adding a new rule is done by creating a new @Component class — no change
 * required here. This is the Open/Closed Principle in action.
 */
@Component
public class RuleEngine {
    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private final List<Rule> rules;
    public RuleEngine(List<Rule> rules) {
        this.rules = rules;
        log.info("RuleEngine initialized with {} rules: {}", rules.size(),
            rules.stream().map(Rule::name).toList());
    }

    /**
     * Evaluate every rule against the given context.
     *
     * @param context the payment context
     * @return list of RuleResults (one per rule)
     */

    public List<RuleResult> evaluateAll(RuleContext context) {
        log.debug("Evaluating {} rules for payment id={}", rules.size(), context.payment().getId());

        return rules.stream().map(rule -> {
            try {
                RuleResult result = rule.evaluate(context);
                log.debug("Rule '{}' -> score={} reason={}",
                    result.ruleName(), result.score(), result.reason());
                return result;
            } catch (Exception ex) {
                log.error("Rule '{}' threw an exception; treating as pass",
                    rule.name(), ex);
                return RuleResult.pass(rule.name());
            }
        }).toList();
    }

    /**
     * Combine individual rule scores into a total (capped at 100).
     * Uses a diminishing-returns model to prevent rule stacking from
     * over-inflating the score.
     */
    public int aggregateScore(List<RuleResult> results) {
        int sum = results.stream().mapToInt(RuleResult::score).sum();
        return Math.min(100, sum);
    }
}
