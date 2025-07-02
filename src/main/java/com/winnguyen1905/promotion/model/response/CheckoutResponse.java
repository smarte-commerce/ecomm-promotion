package com.winnguyen1905.promotion.model.response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

public record CheckoutResponse(PriceStatisticsResponse priceStatistics, List<CheckoutItemResponse> checkoutItems) {

  public CheckoutResponse {
    if (checkoutItems == null) {
      checkoutItems = new ArrayList<>();
    }
  }

  public record CheckoutItemResponse(UUID cartId, PriceStatisticsResponse priceStatistics) {
    @Builder
    public CheckoutItemResponse(UUID cartId, PriceStatisticsResponse priceStatistics) {
      this.cartId = cartId;
      this.priceStatistics = priceStatistics;
    }
  }
}
