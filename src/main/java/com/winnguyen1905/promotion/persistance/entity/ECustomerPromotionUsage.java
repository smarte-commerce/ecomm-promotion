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
@Table(name = "customer_promotion_usage", schema = "public")
public class ECustomerPromotionUsage {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "usage_count")
    private Integer usageCount = 1;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "cashback_amount")
    private Double cashbackAmount = 0.0;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    @CreationTimestamp
    @Column(name = "usage_date", updatable = false)
    private Instant usageDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.SUCCESS;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.usageCount == null) {
            this.usageCount = 1;
        }
        if (this.cashbackAmount == null) {
            this.cashbackAmount = 0.0;
        }
        if (this.pointsEarned == null) {
            this.pointsEarned = 0;
        }
        if (this.status == null) {
            this.status = Status.SUCCESS;
        }
    }

    public enum Status {
        SUCCESS, FAILED, PENDING, CANCELLED
    }
} 
