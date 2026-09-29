package com.karan.risk.paymentriskengine.service;

import com.karan.risk.paymentriskengine.domain.RuleHit;
import com.karan.risk.paymentriskengine.rules.RuleResult;

import java.util.List;
import java.util.UUID;

public interface RuleHitService {

    /**
     * Persist every rule result for a given payment.
     */
    void recordHits(UUID paymentId, List<RuleResult> results);

    /**
     * Fetch all rule hits for a payment — used by admin API and audit.
     */
    List<RuleHit> getHitsForPayment(UUID paymentId);
}
