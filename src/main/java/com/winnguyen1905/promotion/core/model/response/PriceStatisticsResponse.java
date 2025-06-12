package com.winnguyen1905.promotion.core.model.response;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record PriceStatisticsResponse(
    UUID discountId,
    Double totalProductPrice,
    Double totalShipFee,
    Double totalPrice,
    Double amountShipReduced,
    Double totalDiscountVoucher,
    Double amountProductReduced,
    Double finalPrice) implements AbstractModel {
  @Builder
  public PriceStatisticsResponse(
      UUID discountId,
      Double totalProductPrice,
      Double totalShipFee,
      Double totalPrice,

      Double amountShipReduced,
      Double totalDiscountVoucher,
      Double amountProductReduced,
      Double finalPrice) {
    this.discountId = discountId;
    this.totalProductPrice = totalProductPrice;
    this.totalShipFee = totalShipFee;
    this.totalPrice = totalPrice;
    this.amountShipReduced = amountShipReduced;
    this.totalDiscountVoucher = totalDiscountVoucher;
    this.amountProductReduced = amountProductReduced;
    this.finalPrice = finalPrice;
  }
}
