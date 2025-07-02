package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProduct.Status;

import lombok.Builder;

@Builder
public record PromotionProductVm(
    UUID id,
    UUID programId,
    String programName,
    UUID productId,
    String productName,
    UUID vendorId,
    String vendorName,
    Double originalPrice,
    Double promotionPrice,
    Double discountAmount,
    Double discountPercentage,
    Integer stockAllocated,
    Integer stockSold,
    Integer priority,
    Boolean isFeatured,
    Status status,
    Instant addedAt,
    Instant updatedAt
) implements AbstractModel {
} 
