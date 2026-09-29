package com.karan.risk.paymentriskengine.repository;

import com.karan.risk.paymentriskengine.domain.RuleHit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RuleHitRepository extends JpaRepository<RuleHit, UUID> {

    List<RuleHit> findByPaymentId(UUID paymentId);

    List<RuleHit> findByRuleName(String ruleName);

    List<RuleHit> findByPaymentIdAndScoreGreaterThan(UUID paymentId, int score);
}
