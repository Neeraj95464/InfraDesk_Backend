package com.InfraDesk.entity;

import com.InfraDesk.enums.PaymentMode;
import com.InfraDesk.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Column(length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // SUCCESS, FAILED, REFUNDED, PENDING

    private String notes;

    private LocalDateTime paidAt;

    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.paidAt = this.paidAt == null ? LocalDateTime.now() : this.paidAt;
    }

    @Column(nullable = false)
    private Boolean isDeleted = false;

}
