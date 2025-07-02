package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record PromotionAnalyticsVm(
    UUID id,
    UUID programId,
    String programName,
    LocalDate date,
    Integer totalOrders,
    Double totalRevenue,
    Double totalDiscountGiven,
    Integer totalCustomers,
    Integer newCustomers,
    Integer returningCustomers,
    Double conversionRate,
    Double averageOrderValue,
    Double roi,
    Integer vendorParticipationCount,
    JsonNode topPerformingProducts,
    Instant createdAt
) implements AbstractModel {
} 
