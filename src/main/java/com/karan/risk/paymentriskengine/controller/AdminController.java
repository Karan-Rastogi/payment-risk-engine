package com.karan.risk.paymentriskengine.controller;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import com.karan.risk.paymentriskengine.dto.OverrideRequest;
import com.karan.risk.paymentriskengine.dto.PaymentDetailResponse;
import com.karan.risk.paymentriskengine.dto.RuleHitResponse;
import com.karan.risk.paymentriskengine.exception.PaymentNotFoundException;
import com.karan.risk.paymentriskengine.repository.PaymentRepository;
import com.karan.risk.paymentriskengine.service.RuleHitService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
public class AdminController {
    public static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final PaymentRepository paymentRepository;
    private final RuleHitService ruleHitService;

    public AdminController(PaymentRepository paymentRepository,
                           RuleHitService ruleHitService) {
        this.paymentRepository = paymentRepository;
        this.ruleHitService = ruleHitService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> getPaymentDetail(@PathVariable UUID id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(id));

        List<RuleHitResponse> hits = ruleHitService.getHitsForPayment(id)
            .stream()
            .map(RuleHitResponse::from)
            .toList();

        return ResponseEntity.ok(PaymentDetailResponse.from(payment, hits));
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<PaymentDetailResponse>> getFlaggedPayments() {
        List<Payment> flagged = paymentRepository.findByStatusIn(
            List.of(PaymentStatus.REVIEW, PaymentStatus.DECLINED));

        List<PaymentDetailResponse> response = flagged.stream()
            .map(p -> {
                List<RuleHitResponse> hits = ruleHitService.getHitsForPayment(p.getId())
                    .stream().map(RuleHitResponse::from).toList();
                return PaymentDetailResponse.from(p, hits);
            })
            .toList();

        log.info("Fetched {} flagged payments", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/override")
    public ResponseEntity<PaymentDetailResponse> overrideDecision(
        @PathVariable UUID id,
        @Valid @RequestBody OverrideRequest request) {

        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(id));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(request.status());
        Payment saved = paymentRepository.save(payment);

        log.warn("Manual override: payment={} {} -> {} reason='{}'",
            id, oldStatus, request.status(), request.reason());

        List<RuleHitResponse> hits = ruleHitService.getHitsForPayment(id)
            .stream().map(RuleHitResponse::from).toList();

        return ResponseEntity.ok(PaymentDetailResponse.from(saved, hits));
    }
}
