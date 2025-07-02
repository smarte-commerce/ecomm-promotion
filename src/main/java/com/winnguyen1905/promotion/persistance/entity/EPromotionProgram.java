package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
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
@Table(name = "promotion_programs", schema = "public")
public class EPromotionProgram {
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private ECampaign campaign;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", nullable = false)
    private ProgramType programType;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "priority")
    private Integer priority = 1;

    @Column(name = "is_stackable")
    private Boolean isStackable = false;

    @Column(name = "platform_commission_rate")
    private Double platformCommissionRate = 0.0;

    @Column(name = "required_vendor_contribution")
    private Double requiredVendorContribution = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "usage_limit_global")
    private Integer usageLimitGlobal;

    @Column(name = "usage_count_global")
    private Integer usageCountGlobal = 0;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "terms_url")
    private String termsUrl;

    @Column(name = "auto_apply")
    private Boolean autoApply = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProgramStatus status = ProgramStatus.DRAFT;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EPromotionRule> promotionRules = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EPromotionAction> promotionActions = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EVendorPromotionParticipation> vendorParticipations = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EPromotionProduct> promotionProducts = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<ECustomerPromotionUsage> customerUsages = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EPromotionCommission> promotionCommissions = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EPromotionAnalytics> promotionAnalytics = new ArrayList<>();

    @Default
    @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
    private List<EDiscount> discounts = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.priority == null) {
            this.priority = 1;
        }
        if (this.isStackable == null) {
            this.isStackable = false;
        }
        if (this.platformCommissionRate == null) {
            this.platformCommissionRate = 0.0;
        }
        if (this.requiredVendorContribution == null) {
            this.requiredVendorContribution = 0.0;
        }
        if (this.visibility == null) {
            this.visibility = Visibility.PUBLIC;
        }
        if (this.usageCountGlobal == null) {
            this.usageCountGlobal = 0;
        }
        if (this.autoApply == null) {
            this.autoApply = false;
        }
        if (this.status == null) {
            this.status = ProgramStatus.DRAFT;
        }
    }

    public enum ProgramType {
        DISCOUNT, CASHBACK, POINTS, GIFT, SHIPPING, BUNDLE
    }

    public enum Visibility {
        PUBLIC, INVITE_ONLY, MEMBER_ONLY, VIP_ONLY
    }

    public enum ProgramStatus {
        DRAFT, ACTIVE, PAUSED, EXPIRED, CANCELLED
    }
}
