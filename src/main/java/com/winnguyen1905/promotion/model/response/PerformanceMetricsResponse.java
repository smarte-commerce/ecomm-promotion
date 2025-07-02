package com.winnguyen1905.promotion.model.response;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record PerformanceMetricsResponse(
    long impressions,
    long clicks,
    long redemptions,
    double totalDiscountGiven,
    double revenueLift,
    double conversionRate) implements AbstractModel {
} 
