package com.saikumar.paymentservice.controller;

import com.saikumar.paymentservice.model.Payment;
import com.saikumar.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // GET /api/payments/order/{orderId}
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // GET /api/payments/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    // PUT /api/payments/{id}/refund
    @PutMapping("/{id}/refund")
    public ResponseEntity<Payment> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("payment-service ok");
    }
}
