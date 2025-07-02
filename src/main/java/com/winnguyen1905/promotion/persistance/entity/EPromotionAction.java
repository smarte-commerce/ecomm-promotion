package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.databind.JsonNode;

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
@Table(name = "promotion_actions", schema = "public")
public class EPromotionAction {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false)
    private Target target;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "applies_to", columnDefinition = "jsonb")
    private JsonNode appliesTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public enum ActionType {
        PERCENTAGE_DISCOUNT, FIXED_DISCOUNT, FREE_SHIPPING,
        CASHBACK_PERCENTAGE, CASHBACK_FIXED, LOYALTY_POINTS,
        FREE_GIFT, BUY_X_GET_Y, UPGRADE_SHIPPING
    }

    public enum Target {
        ORDER_TOTAL, SHIPPING, SPECIFIC_PRODUCTS, CHEAPEST_PRODUCT, MOST_EXPENSIVE
    }
} 
