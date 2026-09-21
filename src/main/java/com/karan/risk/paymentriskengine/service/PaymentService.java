package com.karan.risk.paymentriskengine.service;

import com.karan.risk.paymentriskengine.dto.PaymentRequest;
import com.karan.risk.paymentriskengine.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
}
