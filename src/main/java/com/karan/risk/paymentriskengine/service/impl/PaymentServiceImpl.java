package com.karan.risk.paymentriskengine.service.impl;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import com.karan.risk.paymentriskengine.dto.PaymentRequest;
import com.karan.risk.paymentriskengine.dto.PaymentResponse;
import com.karan.risk.paymentriskengine.repository.PaymentRepository;
import com.karan.risk.paymentriskengine.rules.RuleContext;
import com.karan.risk.paymentriskengine.rules.RuleEngine;
import com.karan.risk.paymentriskengine.rules.RuleResult;
import com.karan.risk.paymentriskengine.scoring.DecisionEngine;
import com.karan.risk.paymentriskengine.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final RuleEngine ruleEngine;
    private final DecisionEngine decisionEngine;


    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              RuleEngine ruleEngine,
                              DecisionEngine decisionEngine) {
        this.paymentRepository = paymentRepository;
        this.ruleEngine = ruleEngine;
        this.decisionEngine = decisionEngine;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing Payment from={} to={} amount={} {}",
            request.senderId(), request.receiverId(),
            request.amount(), request.currency());

        // Step 1: Build entity
        Payment payment = new Payment();
        payment.setSenderId(request.senderId());
        payment.setReceiverId(request.receiverId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setChannel(request.channel());
        payment.setIpAddress(request.ipAddress());
        payment.setIpCountry(request.ipCountry());
        payment.setDeviceId(request.deviceId());
        payment.setStatus(PaymentStatus.RECEIVED);

        // Step 2: Run rule engine
        RuleContext context = RuleContext.of(payment, null); // homeCountry null for now
        List<RuleResult> ruleResults = ruleEngine.evaluateAll(context);

        // Step 3: Aggregate + decide
        int totalScore = ruleEngine.aggregateScore(ruleResults);
        PaymentStatus decision = decisionEngine.decide(totalScore);

        log.info("Risk evaluation: totalScore={} decision={} ruleHits={}",
            totalScore, decision,
            ruleResults.stream().filter(r -> r.score() > 0).count());

        // Step 4: Apply decision
        payment.setStatus(decision);

        // Step 5: Persist
        Payment saved = paymentRepository.save(payment);

        return new PaymentResponse(
            saved.getId(),
            saved.getSenderId(),
            saved.getReceiverId(),
            saved.getAmount(),
            saved.getCurrency(),
            saved.getChannel(),
            saved.getStatus(),
            saved.getCreatedAt()
        );
    }
}
