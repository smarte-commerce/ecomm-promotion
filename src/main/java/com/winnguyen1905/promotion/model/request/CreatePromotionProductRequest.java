package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePromotionProductRequest(
    @NotNull UUID programId,
    @NotNull UUID productId,
    @NotNull UUID vendorId,
    @Positive Double originalPrice,
    @Positive Double promotionPrice,
    @Positive Double discountAmount,
    @Positive Double discountPercentage,
    Integer stockAllocated,
    Integer priority,
    Boolean isFeatured
) implements AbstractModel {
} 
