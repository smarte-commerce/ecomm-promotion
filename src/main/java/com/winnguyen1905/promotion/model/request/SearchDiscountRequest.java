package com.winnguyen1905.promotion.model.request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record SearchDiscountRequest(
    String code,                    // Search by discount code
    Boolean isActive,              // Filter by active status
    Double minDiscountValue,       // Min discount amount/percentage
    Double maxDiscountValue,       // Max discount amount/percentage
    Double minOrderValue,          // Min order value required
    Double maxOrderValue,          // Max order value allowed
    Instant startDate,             // Valid from date
    Instant endDate,              // Valid to date
    UUID shopId,                  // Filter by shop
    List<UUID> productIds,        // Filter by products
    List<UUID> categoryIds,       // Filter by categories
    String discountType,          // PERCENTAGE or FIXED_AMOUNT
    Boolean isPublic,             // Public or private discount
    Integer minUsageLimit,        // Min usage limit
    Integer maxUsageLimit,        // Max usage limit
    Boolean includeExpired        // Include expired discounts
) implements AbstractModel {
  @Builder
  public SearchDiscountRequest(
      String code,
      Boolean isActive,
      Double minDiscountValue,
      Double maxDiscountValue,
      Double minOrderValue,
      Double maxOrderValue,
      Instant startDate,
      Instant endDate,
      UUID shopId,
      List<UUID> productIds,
      List<UUID> categoryIds,
      String discountType,
      Boolean isPublic,
      Integer minUsageLimit,
      Integer maxUsageLimit,
      Boolean includeExpired) {
    this.code = code;
    this.isActive = isActive;
    this.minDiscountValue = minDiscountValue;
    this.maxDiscountValue = maxDiscountValue;
    this.minOrderValue = minOrderValue;
    this.maxOrderValue = maxOrderValue;
    this.startDate = startDate;
    this.endDate = endDate;
    this.shopId = shopId;
    this.productIds = productIds;
    this.categoryIds = categoryIds;
    this.discountType = discountType;
    this.isPublic = isPublic;
    this.minUsageLimit = minUsageLimit;
    this.maxUsageLimit = maxUsageLimit;
    this.includeExpired = includeExpired;
  }
}
