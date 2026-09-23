package com.karan.risk.paymentriskengine.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Persisted record of a single rule evaluation for a payment.
 *
 * This is the audit trail — every rule that ran (whether it fired or not)
 * is recorded here. Compliance teams can reconstruct exactly why a
 * payment received a particular decision.
 */
@Entity
@Table(
    name = "rule_hits",
    indexes = {
        @Index(name = "idx_rule_hits_payment_id", columnList = "payment_id"),
        @Index(name = "idx_rule_hits_rule_name", columnList = "rule_name"),
        @Index(name = "idx_rule_hits_created_at", columnList = "created_at")
    }
)
public class RuleHit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "rule_name", nullable = false, length = 64)
    private String ruleName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters and setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
