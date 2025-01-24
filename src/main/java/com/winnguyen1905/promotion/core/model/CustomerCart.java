package com.winnguyen1905.promotion.core.model;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;

import lombok.Builder;

@Builder
public record CustomerCart(
    UUID productDiscountId,
    UUID shippingDiscountId,
    List<CustomerCartWithShop> cartByShops,
    PriceStatisticsResponse priceStatistic) implements AbstractModel {

  @Builder
  public static record CustomerCartWithShop(
      UUID shopId,
      UUID discountId,
      List<CartItem> cartItems,
      PriceStatisticsResponse priceStatistic) {
  }

  @Builder
  public static record CartItem(
      int quantity,
      double price,
      Boolean isSelected,
      UUID productVariantId) implements AbstractModel {
  }
}
