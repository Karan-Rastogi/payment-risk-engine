package com.karan.risk.paymentriskengine.rules.impl;

import com.karan.risk.paymentriskengine.rules.Rule;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Flags payments whose amount exceeds configured thresholds.
 *
 * Score increases in tiers:
 *   - amount <= elevatedThreshold  -> 0  (pass)
 *   - amount <= highThreshold      -> 30 (elevated)
 *   - amount <= criticalThreshold  -> 60 (high)
 *   - amount >  criticalThreshold  -> 90 (critical)
 *
 * All thresholds are currency-agnostic for now (assumed same currency).
 * Real systems would normalize by currency and user history.
 */
@Component
public class AmountThresholdRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(AmountThresholdRule.class);

    private static final String RULE_NAME = "AMOUNT_THRESHOLD";

    private final BigDecimal elevatedThreshold;
    private final BigDecimal highThreshold;
    private final BigDecimal criticalThreshold;

    public AmountThresholdRule(
        @Value("${rules.amount.elevated:10000}") BigDecimal elevatedThreshold,
        @Value("${rules.amount.high:100000}") BigDecimal highThreshold,
        @Value("${rules.amount.critical:1000000}") BigDecimal criticalThreshold) {
        this.elevatedThreshold = elevatedThreshold;
        this.highThreshold = highThreshold;
        this.criticalThreshold = criticalThreshold;
    }

    @Override
    public String name() {
        return RULE_NAME;
    }

    @Override
    public RuleResult evaluate(RuleContext context) {
        BigDecimal amount = context.payment().getAmount();
        String currency = context.payment().getCurrency();

        if(amount == null) {
            return RuleResult.pass(RULE_NAME);
        }

        if (amount.compareTo(criticalThreshold) > 0) {
            return fire(amount, currency, 90, "CRITICAL");
        }

        if (amount.compareTo(highThreshold) > 0) {
            return fire(amount, currency, 60, "HIGH");
        }
        if (amount.compareTo(elevatedThreshold) > 0) {
            return fire(amount, currency, 30, "ELEVATED");
        }
        return RuleResult.pass(RULE_NAME);
    }

    private RuleResult fire(BigDecimal amount, String currency, int score, String tier) {
        String reason = String.format(
            "%s amount %s %s exceeds threshold",
            tier, amount.toPlainString(), currency);
        log.warn("Amount rule fired: {}", reason);
        return RuleResult.hit(RULE_NAME, score, reason);
    }

}
