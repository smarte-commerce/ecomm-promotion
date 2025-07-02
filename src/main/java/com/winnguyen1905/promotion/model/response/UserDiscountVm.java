package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record UserDiscountVm(
    UUID id,
    UUID customerId,
    UUID discountId,
    String discountName,
    String discountCode,
    DiscountType discountType,
    DiscountCategory discountCategory,
    Double value,
    Double maxDiscountAmount,
    Double minOrderValue,
    Instant startDate,
    Instant endDate,
    Integer usageLimitPerCustomer,
    Integer usageCount,
    Boolean isActive,
    Boolean isPublic
) implements AbstractModel {
} 
