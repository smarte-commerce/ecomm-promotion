package com.winnguyen1905.promotion.model.response;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record CampaignStatisticsResponse(
    long totalPrograms,
    long totalDiscounts,
    double budget,
    double spentBudget,
    double budgetRemaining) implements AbstractModel {
} 
