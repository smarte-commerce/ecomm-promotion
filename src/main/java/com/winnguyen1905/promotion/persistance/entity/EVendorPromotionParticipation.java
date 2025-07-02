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
@Table(name = "vendor_promotion_participations", schema = "public")
public class EVendorPromotionParticipation {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_type")
    private ParticipationType participationType = ParticipationType.VOLUNTARY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "vendor_contribution_rate", nullable = false)
    private Double vendorContributionRate;

    @Column(name = "expected_discount_rate", nullable = false)
    private Double expectedDiscountRate;

    @Column(name = "min_discount_amount")
    private Double minDiscountAmount;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_selection")
    private ProductSelection productSelection = ProductSelection.ALL;

    @Column(name = "accepted_terms", nullable = false)
    private Boolean acceptedTerms = false;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "withdrawal_reason", columnDefinition = "TEXT")
    private String withdrawalReason;

    @Column(name = "performance_metrics", columnDefinition = "jsonb")
    private JsonNode performanceMetrics;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.participationType == null) {
            this.participationType = ParticipationType.VOLUNTARY;
        }
        if (this.status == null) {
            this.status = Status.PENDING;
        }
        if (this.productSelection == null) {
            this.productSelection = ProductSelection.ALL;
        }
        if (this.acceptedTerms == null) {
            this.acceptedTerms = false;
        }
    }

    public enum ParticipationType {
        VOLUNTARY, MANDATORY, INVITED
    }

    public enum Status {
        PENDING, APPROVED, REJECTED, WITHDRAWN, SUSPENDED
    }

    public enum ProductSelection {
        ALL, SELECTED, CATEGORY
    }
} 
