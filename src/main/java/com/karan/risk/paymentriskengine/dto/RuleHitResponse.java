package com.karan.risk.paymentriskengine.dto;

import com.karan.risk.paymentriskengine.domain.RuleHit;

import java.time.Instant;

public record RuleHitResponse (
    String ruleName,
    int score,
    String reason,
    Instant createdAt
){
    public static RuleHitResponse from(RuleHit hit) {
        return new RuleHitResponse(
            hit.getRuleName(),
            hit.getScore(),
            hit.getReason(),
            hit.getCreatedAt()
        );
    }
}
