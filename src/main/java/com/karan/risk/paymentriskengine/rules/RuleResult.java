package com.karan.risk.paymentriskengine.rules;

public record RuleResult(
    String ruleName,
    int score,
    String reason
) {
    public static RuleResult pass(String ruleName) {
        return new RuleResult(ruleName, 0, "No risk detected");
    }

    public static RuleResult hit(String ruleName, int score, String reason) {
        return new RuleResult(ruleName, score, reason);
    }
}
