package com.karan.risk.paymentriskengine.scoring;

import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Converts a raw risk score into a business decision.
 *
 * Threshold policy (configurable):
 *   - score < reviewThreshold           -> APPROVED
 *   - score < declineThreshold          -> REVIEW
 *   - score >= declineThreshold         -> DECLINED
 */
@Component
public class DecisionEngine {
    private static final Logger log = LoggerFactory.getLogger(DecisionEngine.class);

    private final int reviewThreshold;
    private final int declineThreshold;

    public DecisionEngine(
        @Value("${scoring.thresholds.review:30}") int reviewThreshold,
        @Value("${scoring.thresholds.decline:70}") int declineThreshold) {
        this.reviewThreshold = reviewThreshold;
        this.declineThreshold = declineThreshold;
    }

    public PaymentStatus decide(int totalScore) {
        if (totalScore >= declineThreshold) {
            log.warn("Decision: DECLINED (score={} >= {})", totalScore, declineThreshold);
            return PaymentStatus.DECLINED;
        }
        if (totalScore >= reviewThreshold) {
            log.info("Decision: REVIEW (score={} >= {})", totalScore, reviewThreshold);
            return PaymentStatus.REVIEW;
        }
        log.debug("Decision: APPROVED (score={})", totalScore);
        return PaymentStatus.APPROVED;
    }
}
