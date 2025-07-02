package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "promotion_products", schema = "public")
public class EPromotionProduct {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "original_price", nullable = false)
    private Double originalPrice;

    @Column(name = "promotion_price", nullable = false)
    private Double promotionPrice;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "discount_percentage", nullable = false)
    private Double discountPercentage;

    @Column(name = "stock_allocated")
    private Integer stockAllocated = 0;

    @Column(name = "stock_sold")
    private Integer stockSold = 0;

    @Column(name = "priority")
    private Integer priority = 1;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private Instant addedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.stockAllocated == null) {
            this.stockAllocated = 0;
        }
        if (this.stockSold == null) {
            this.stockSold = 0;
        }
        if (this.priority == null) {
            this.priority = 1;
        }
        if (this.isFeatured == null) {
            this.isFeatured = false;
        }
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
    }

    public enum Status {
        ACTIVE, PAUSED, OUT_OF_STOCK, REMOVED
    }
} 
