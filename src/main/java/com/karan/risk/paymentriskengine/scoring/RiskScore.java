package com.karan.risk.paymentriskengine.scoring;

import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import com.karan.risk.paymentriskengine.rules.RuleResult;

import java.util.List;

/**
 * Aggregated risk score for a single payment evaluation.
 *
 * @param totalScore   combined risk score (0-100)
 * @param decision     resulting status (APPROVED / REVIEW / DECLINED)
 * @param ruleResults  individual rule outputs for audit and explainability
 */
public record RiskScore(
    int totalScore,
    PaymentStatus decision,
    List<RuleResult> ruleResults
) {
}
