package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.common.DiscountUsageStatus;
import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record DiscountUsageVm(
    UUID id,
    UUID customerId,
    UUID programId,
    String programName,
    UUID discountId,
    String discountName,
    String discountCode,
    UUID orderId,
    Integer usageCount,
    Double discountAmount,
    Double cashbackAmount,
    Integer pointsEarned,
    Instant usageDate,
    DiscountUsageStatus usageStatus
) implements AbstractModel {
} 
