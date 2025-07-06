package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.common.DiscountUsageStatus;
import com.winnguyen1905.promotion.model.AbstractModel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateDiscountUsageRequest(
    @NotNull UUID customerId,
    @NotNull UUID programId,
    @NotNull UUID discountId,
    UUID orderId,
    Integer usageCount,
    @Positive Double discountAmount,
    Double cashbackAmount,
    Integer pointsEarned,
    DiscountUsageStatus usageStatus
) implements AbstractModel {
} 
