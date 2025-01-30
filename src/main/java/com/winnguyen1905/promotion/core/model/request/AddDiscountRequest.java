package com.winnguyen1905.promotion.core.model.request;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.core.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;

import jakarta.validation.constraints.NotBlank;

public record AddDiscountRequest(
    String name,
    DiscountCategory discountCategory,
    String description,
    EDiscount.Scope scope,
    DiscountType discountType,
    double value,
    @NotBlank String code,
    Instant startDate,
    Instant endDate,
    Integer usageLimit,
    Integer usesCount,
    Integer limitUsagePerCutomer,
    double maxReducedValue,
    Double minOrderValue,
    Boolean isActive,
    ApplyDiscountType appliesTo,
    Set<UUID> productIds) implements AbstractModel {
}
