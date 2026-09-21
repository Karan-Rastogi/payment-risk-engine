package com.karan.risk.paymentriskengine.repository;

import com.karan.risk.paymentriskengine.domain.Payment;
import com.karan.risk.paymentriskengine.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findBySenderId(String senderId);

    List<Payment> findByStatus(PaymentStatus status);
}
