package com.saikumar.paymentservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    private Long userId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method = PaymentMethod.CARD;

    private String transactionId;

    private String failureReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentStatus {
        PENDING, PROCESSING, SUCCESS, FAILED, REFUNDED
    }

    public enum PaymentMethod {
        CARD, UPI, NET_BANKING, WALLET
    }
}
