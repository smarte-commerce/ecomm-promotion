package com.winnguyen1905.promotion.core.model.response;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record PriceStatisticsResponse(
  Double totalProductPrice,
  Double totalShipPrice,
  Double amountShipReduced,
  Double amountProductReduced,
  Double finalPrice
) implements AbstractModel {}
