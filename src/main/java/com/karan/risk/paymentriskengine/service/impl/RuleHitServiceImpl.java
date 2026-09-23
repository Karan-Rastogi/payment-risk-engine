package com.karan.risk.paymentriskengine.service.impl;

import com.karan.risk.paymentriskengine.domain.RuleHit;
import com.karan.risk.paymentriskengine.repository.RuleHitRepository;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import com.karan.risk.paymentriskengine.service.RuleHitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RuleHitServiceImpl implements RuleHitService {

    private static final Logger log = LoggerFactory.getLogger(RuleHitServiceImpl.class);

    private final RuleHitRepository ruleHitRepository;

    public RuleHitServiceImpl(RuleHitRepository ruleHitRepository) {
        this.ruleHitRepository = ruleHitRepository;
    }

    @Override
    @Transactional
    public void recordHits(UUID paymentId, List<RuleResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        List<RuleHit> entities = results.stream()
            .map(r -> toEntity(paymentId, r))
            .toList();

        ruleHitRepository.saveAll(entities);

        log.debug("Recorded {} rule hits for payment id={}", entities.size(), paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleHit> getHitsForPayment(UUID paymentId) {
        return ruleHitRepository.findByPaymentId(paymentId);
    }

    private RuleHit toEntity(UUID paymentId, RuleResult r) {
        RuleHit hit = new RuleHit();
        hit.setPaymentId(paymentId);
        hit.setRuleName(r.ruleName());
        hit.setScore(r.score());
        hit.setReason(truncate(r.reason(), 512));
        return hit;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
