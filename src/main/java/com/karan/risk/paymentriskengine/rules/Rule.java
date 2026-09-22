package com.karan.risk.paymentriskengine.rules;

/**
 * Strategy interface for all risk rules.
 * Each rule evaluates a payment context and returns a RuleResult
 * containing a score contribution and a human-readable reason.
 */

public interface Rule {
    /**
     * Unique name of the rule — used for logging, audit trail, and rule-hit tracking.
     */
    String name();

    /**
     * Evaluate the rule against the given context.
     *
     * @param context payment and auxiliary data
     * @return RuleResult with score contribution and reason
     */
    RuleResult evaluate(RuleContext context);
}
