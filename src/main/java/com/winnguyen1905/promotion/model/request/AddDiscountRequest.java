package com.winnguyen1905.promotion.model.request;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;

public record AddDiscountRequest(
    String name,
    DiscountCategory discountCategory,
    String description,
    EDiscount.CreatorType creatorType,
    DiscountType discountType,
    double value,
    String code,
    Instant startDate,
    Instant endDate,
    Integer usageLimit,
    Integer usesCount,
    Integer limitUsagePerCutomer,
    double maxReducedValue,
    Double minOrderValue,
    Boolean isActive,
    Set<String> categories,
    ApplyDiscountType appliesTo,
    Set<UUID> productIds) implements AbstractModel {
}
