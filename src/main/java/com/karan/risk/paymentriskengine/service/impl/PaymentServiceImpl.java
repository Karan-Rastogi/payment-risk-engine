package com.karan.risk.paymentriskengine.service.impl;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import com.karan.risk.paymentriskengine.dto.PaymentRequest;
import com.karan.risk.paymentriskengine.dto.PaymentResponse;
import com.karan.risk.paymentriskengine.repository.PaymentRepository;
import com.karan.risk.paymentriskengine.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing Payment from={} to={} amount={} {}",
            request.senderId(), request.receiverId(),
            request.amount(), request.currency());

        Payment payment = new Payment();
        payment.setSenderId(request.senderId());
        payment.setReceiverId(request.receiverId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setChannel(request.channel());
        payment.setIpAddress(request.ipAddress());
        payment.setDeviceId(request.deviceId());
        payment.setStatus(PaymentStatus.RECEIVED);

        Payment saved = paymentRepository.save(payment);

        log.info("Payment persisted with id={} status={}", saved.getId(), saved.getStatus());

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
