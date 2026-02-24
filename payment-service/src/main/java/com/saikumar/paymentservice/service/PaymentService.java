package com.saikumar.paymentservice.service;

import com.saikumar.paymentservice.model.Payment;
import com.saikumar.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public boolean processPayment(Long orderId, Long userId, Double amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        try {
            // simulate payment processing
            // in real world: integrate with Stripe, Razorpay, etc.
            Thread.sleep(500);

            // simulate 90% success rate for testing
            boolean success = Math.random() > 0.1;

            if (success) {
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                payment.setProcessedAt(LocalDateTime.now());
                log.info("Payment SUCCESS for order {}, txn: {}", orderId, payment.getTransactionId());
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason("Insufficient funds");
                log.warn("Payment FAILED for order {}", orderId);
            }

            paymentRepository.save(payment);
            return success;

        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Processing error: " + e.getMessage());
            paymentRepository.save(payment);
            log.error("Payment processing error for order {}", orderId, e);
            return false;
        }
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Can only refund successful payments");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("Payment {} refunded for order {}", paymentId, payment.getOrderId());
        return payment;
    }
}
