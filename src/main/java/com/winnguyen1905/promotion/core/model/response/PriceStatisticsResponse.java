package com.winnguyen1905.promotion.core.model.response;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public record PriceStatisticsResponse(
  Double totalPrice,
  Double totalShipPrice,
  Double totalDiscountVoucher,
  Double amountShipReduced,
  Double amountProductReduced,
  Double finalPrice
) implements AbstractModel {}
