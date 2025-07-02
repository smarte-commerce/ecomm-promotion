package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discounts", schema = "public")
public class EDiscount {

    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private EPromotionProgram program;

    @Enumerated(EnumType.STRING)
    @Column(name = "creator_type", nullable = false)
    private CreatorType creatorType;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DiscountType discountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to", nullable = false)
    private ApplyDiscountType appliesTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private DiscountCategory discountCategory;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "code", unique = true)
    private String code;

    @Min(value = 0)
    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "min_order_value")
    private Double minOrderValue;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "usage_limit_total")
    private Integer usageLimitTotal;

    @Column(name = "usage_limit_per_customer")
    private Integer usageLimitPerCustomer = 1;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "auto_apply")
    private Boolean autoApply = false;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "target_customer_tiers", columnDefinition = "jsonb")
    private JsonNode targetCustomerTiers;

    @Column(name = "geographic_restrictions", columnDefinition = "jsonb")
    private JsonNode geographicRestrictions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Default
    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EDiscountUsage> discountUsages = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EUserDiscount> userDiscounts = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE })
    private List<EProductDiscount> productDiscounts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.usageLimitPerCustomer == null) {
            this.usageLimitPerCustomer = 1;
        }
        if (this.usageCount == null) {
            this.usageCount = 0;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isPublic == null) {
            this.isPublic = true;
        }
        if (this.autoApply == null) {
            this.autoApply = false;
        }
    }

    public enum CreatorType {
        ADMIN, VENDOR, SYSTEM
    }

    // Legacy getters for backward compatibility
    @Deprecated
    public Double getMaxReducedValue() {
        return maxDiscountAmount;
    }

    @Deprecated
    public void setMaxReducedValue(Double maxReducedValue) {
        this.maxDiscountAmount = maxReducedValue;
    }

    @Deprecated
    public int getUsageLimit() {
        return usageLimitTotal != null ? usageLimitTotal : 0;
    }

    @Deprecated
    public void setUsageLimit(int usageLimit) {
        this.usageLimitTotal = usageLimit;
    }

    @Deprecated
    public int getLimitUsagePerCutomer() {
        return usageLimitPerCustomer != null ? usageLimitPerCustomer : 1;
    }

    @Deprecated
    public void setLimitUsagePerCutomer(int limitUsagePerCustomer) {
        this.usageLimitPerCustomer = limitUsagePerCustomer;
    }

    @Deprecated
    public UUID getShopId() {
        return vendorId;
    }

    @Deprecated
    public void setShopId(UUID shopId) {
        this.vendorId = shopId;
    }
}
