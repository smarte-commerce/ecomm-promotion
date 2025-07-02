package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record DiscountVm(
    String name,
    DiscountCategory discountCategory,
    String description,
    EDiscount.CreatorType creatorType,
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
    Set<String> categories,
    Set<UUID> productIds) implements AbstractModel {
  @Builder
  public DiscountVm(
      String name,
      DiscountCategory discountCategory,
      String description,
      EDiscount.CreatorType creatorType,
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
      Set<String> categories,
      Set<UUID> productIds) {
    this.name = name;
    this.discountCategory = discountCategory;
    this.description = description;
    this.creatorType = creatorType;
    this.discountType = discountType;
    this.value = value;
    this.code = code;
    this.startDate = startDate;
    this.endDate = endDate;
    this.usageLimit = usageLimit;
    this.usesCount = usesCount;
    this.limitUsagePerCutomer = limitUsagePerCutomer;
    this.maxReducedValue = maxReducedValue;
    this.minOrderValue = minOrderValue;
    this.isActive = isActive;
    this.appliesTo = appliesTo;
    this.categories = categories;
    this.productIds = productIds;
  }
}
