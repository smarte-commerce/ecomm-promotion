package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "promotion_commissions", schema = "public")
public class EPromotionCommission {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "order_amount", nullable = false)
    private Double orderAmount;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "vendor_contribution", nullable = false)
    private Double vendorContribution;

    @Column(name = "platform_contribution", nullable = false)
    private Double platformContribution;

    @Column(name = "commission_amount", nullable = false)
    private Double commissionAmount;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @Column(name = "transaction_id")
    private String transactionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private Instant processedAt;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    public enum PaymentStatus {
        PENDING, PROCESSING, PAID, FAILED
    }
} 
