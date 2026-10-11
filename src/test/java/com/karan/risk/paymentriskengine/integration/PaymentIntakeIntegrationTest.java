package com.karan.risk.paymentriskengine.integration;

import com.karan.risk.paymentriskengine.AbstractIntegrationTest;
import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import com.karan.risk.paymentriskengine.dto.PaymentRequest;
import com.karan.risk.paymentriskengine.dto.PaymentResponse;
import com.karan.risk.paymentriskengine.repository.PaymentRepository;
import com.karan.risk.paymentriskengine.repository.RuleHitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test for payment intake flow.
 * Uses real HTTP, real PostgreSQL, real Redis, real Kafka.
 */
class PaymentIntakeIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RuleHitRepository ruleHitRepository;

    private RestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:" + port + "/api/v1/payments";
    }

    @Test
    @DisplayName("Low-risk payment is APPROVED and persisted with audit trail")
    void lowRiskPayment_approvedAndPersisted() {
        PaymentRequest request = new PaymentRequest(
            "USER_IT_001", "USER_IT_002",
            new BigDecimal("5000"), "INR", "UPI",
            "1.2.3.4", "IN", "device-001");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            baseUrl, request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        PaymentResponse body = response.getBody();
        assertThat(body.id()).isNotNull();
        assertThat(body.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(body.senderId()).isEqualTo("USER_IT_001");

        Payment saved = paymentRepository.findById(body.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        var ruleHits = ruleHitRepository.findByPaymentId(body.id());
        assertThat(ruleHits).hasSize(4);
        assertThat(ruleHits)
            .extracting("ruleName")
            .containsExactlyInAnyOrder(
                "VELOCITY", "AMOUNT_THRESHOLD", "GEO_ANOMALY", "BLACKLIST");
    }

    @Test
    @DisplayName("High-risk payment is DECLINED and persisted")
    void highRiskPayment_declined() {
        PaymentRequest request = new PaymentRequest(
            "USER_IT_003", "FRAUD_999",
            new BigDecimal("500000"), "INR", "UPI",
            "1.2.3.4", "IR", "device-002");

        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            baseUrl, request, PaymentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(PaymentStatus.DECLINED);

        var ruleHits = ruleHitRepository.findByPaymentId(response.getBody().id());
        assertThat(ruleHits).hasSize(4);

        long firedRules = ruleHits.stream().filter(h -> h.getScore() > 0).count();
        assertThat(firedRules).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Velocity rule fires after threshold using real Redis")
    void velocityRule_firesAfterThreshold() {
        String senderId = "USER_IT_VELOCITY";

        // Send 5 payments — all APPROVED
        for (int i = 0; i < 5; i++) {
            PaymentRequest request = new PaymentRequest(
                senderId, "USER_IT_002",
                new BigDecimal("1000"), "INR", "UPI",
                "1.2.3.4", "IN", "device-003");

            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
                baseUrl, request, PaymentResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().status()).isEqualTo(PaymentStatus.APPROVED);
        }

        // 6th payment — velocity rule fires (score=20, still APPROVED since 20 < 30)
        PaymentRequest sixth = new PaymentRequest(
            senderId, "USER_IT_002",
            new BigDecimal("1000"), "INR", "UPI",
            "1.2.3.4", "IN", "device-003");

        ResponseEntity<PaymentResponse> response6 = restTemplate.postForEntity(
            baseUrl, sixth, PaymentResponse.class);

        assertThat(response6.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response6.getBody()).isNotNull();

        // Verify velocity rule hit on the 6th payment
        var ruleHits6 = ruleHitRepository.findByPaymentId(response6.getBody().id());
        var velocityHit6 = ruleHits6.stream()
            .filter(h -> "VELOCITY".equals(h.getRuleName()))
            .findFirst()
            .orElseThrow();
        assertThat(velocityHit6.getScore()).isEqualTo(20);

        // 7th payment — velocity score climbs to 40 ≥ 30 → REVIEW
        PaymentRequest seventh = new PaymentRequest(
            senderId, "USER_IT_002",
            new BigDecimal("1000"), "INR", "UPI",
            "1.2.3.4", "IN", "device-003");

        ResponseEntity<PaymentResponse> response7 = restTemplate.postForEntity(
            baseUrl, seventh, PaymentResponse.class);

        assertThat(response7.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response7.getBody()).isNotNull();
        assertThat(response7.getBody().status()).isEqualTo(PaymentStatus.REVIEW);

        // Verify velocity rule score is 40 on the 7th payment
        var ruleHits7 = ruleHitRepository.findByPaymentId(response7.getBody().id());
        var velocityHit7 = ruleHits7.stream()
            .filter(h -> "VELOCITY".equals(h.getRuleName()))
            .findFirst()
            .orElseThrow();
        assertThat(velocityHit7.getScore()).isEqualTo(40);
    }
}
