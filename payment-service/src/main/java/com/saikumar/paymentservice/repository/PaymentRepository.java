package com.saikumar.paymentservice.repository;

import com.saikumar.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
