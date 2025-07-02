package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "promotion_analytics", schema = "public")
public class EPromotionAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private EPromotionProgram program;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_revenue")
    private Double totalRevenue = 0.0;

    @Column(name = "total_discount_given")
    private Double totalDiscountGiven = 0.0;

    @Column(name = "total_customers")
    private Integer totalCustomers = 0;

    @Column(name = "new_customers")
    private Integer newCustomers = 0;

    @Column(name = "returning_customers")
    private Integer returningCustomers = 0;

    @Column(name = "conversion_rate")
    private Double conversionRate = 0.0;

    @Column(name = "average_order_value")
    private Double averageOrderValue = 0.0;

    @Column(name = "roi")
    private Double roi = 0.0;

    @Column(name = "vendor_participation_count")
    private Integer vendorParticipationCount = 0;

    @Column(name = "top_performing_products", columnDefinition = "jsonb")
    private JsonNode topPerformingProducts;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.totalOrders == null) {
            this.totalOrders = 0;
        }
        if (this.totalRevenue == null) {
            this.totalRevenue = 0.0;
        }
        if (this.totalDiscountGiven == null) {
            this.totalDiscountGiven = 0.0;
        }
        if (this.totalCustomers == null) {
            this.totalCustomers = 0;
        }
        if (this.newCustomers == null) {
            this.newCustomers = 0;
        }
        if (this.returningCustomers == null) {
            this.returningCustomers = 0;
        }
        if (this.conversionRate == null) {
            this.conversionRate = 0.0;
        }
        if (this.averageOrderValue == null) {
            this.averageOrderValue = 0.0;
        }
        if (this.roi == null) {
            this.roi = 0.0;
        }
        if (this.vendorParticipationCount == null) {
            this.vendorParticipationCount = 0;
        }
    }
} 
