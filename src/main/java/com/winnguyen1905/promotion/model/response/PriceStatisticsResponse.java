package com.winnguyen1905.promotion.model.response;

import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record PriceStatisticsResponse(
    UUID discountId,
    Double totalProductPrice,
    Double totalShipFee,
    Double totalPrice,
    Double amountShipReduced,
    Double totalShopProductDiscount,
    Double totalGlobalProductDiscount,
    Double totalGlobalShippingDiscount,
    String shippingDiscountType, // FIXED or PERCENTAGE
    Double amountProductReduced,
    Double finalPrice) implements AbstractModel {
  @Builder
  public PriceStatisticsResponse(
      UUID discountId,
      Double totalProductPrice,
      Double totalShipFee,
      Double totalPrice,

      Double amountShipReduced,
      Double totalShopProductDiscount,
      Double totalGlobalProductDiscount,
      Double totalGlobalShippingDiscount,
      String shippingDiscountType, // FIXED or PERCENTAGE
      Double amountProductReduced,
      Double finalPrice) {
    this.discountId = discountId;
    this.totalProductPrice = totalProductPrice;
    this.totalShipFee = totalShipFee;
    this.totalPrice = totalPrice;
    this.amountShipReduced = amountShipReduced;
    this.totalShopProductDiscount = totalShopProductDiscount;
    this.totalGlobalProductDiscount = totalGlobalProductDiscount;
    this.totalGlobalShippingDiscount = totalGlobalShippingDiscount;
    this.shippingDiscountType = shippingDiscountType;
    this.amountProductReduced = amountProductReduced;
    this.finalPrice = finalPrice;
  }
}
