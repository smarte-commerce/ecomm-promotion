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
@Table(name = "promotion_rules", schema = "public")
public class EPromotionRule {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private RuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private Operator operator;

    @Column(name = "value", nullable = false, columnDefinition = "jsonb")
    private JsonNode value;

    @Column(name = "is_required")
    private Boolean isRequired = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.isRequired == null) {
            this.isRequired = true;
        }
    }

    public enum RuleType {
        MIN_ORDER_VALUE, MIN_QUANTITY, CUSTOMER_TIER, 
        FIRST_ORDER, REPEAT_CUSTOMER, PRODUCT_CATEGORY,
        BRAND, VENDOR, PAYMENT_METHOD, SHIPPING_METHOD,
        DAY_OF_WEEK, TIME_OF_DAY, LOCATION
    }

    public enum Operator {
        EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, IN, NOT_IN, CONTAINS
    }
} 
