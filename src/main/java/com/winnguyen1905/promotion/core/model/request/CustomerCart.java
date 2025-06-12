package com.winnguyen1905.promotion.core.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;

import lombok.Builder;

@Builder
public record CustomerCart(
    UUID productDiscountId,
    UUID shippingDiscountId,
    List<CustomerCartWithShop> cartByShops,
    PriceStatisticsResponse priceStatistic) implements AbstractModel {
  @Builder
  public CustomerCart(
      UUID productDiscountId,
      UUID shippingDiscountId,
      List<CustomerCartWithShop> cartByShops,
      PriceStatisticsResponse priceStatistic) {
    this.productDiscountId = productDiscountId;
    this.shippingDiscountId = shippingDiscountId;
    this.cartByShops = cartByShops;
    this.priceStatistic = priceStatistic;
  }

  @Builder
  public static record CustomerCartWithShop(
      UUID shopId,
      List<CartItem> cartItems,
      PriceStatisticsResponse priceStatistic) {
    @Builder
    public CustomerCartWithShop(
        UUID shopId,
        List<CartItem> cartItems,
        PriceStatisticsResponse priceStatistic) {
      this.shopId = shopId;
      this.cartItems = cartItems;
      this.priceStatistic = priceStatistic;
    }
  }

  @Builder
  public static record CartItem(
      int quantity,
      double price,
      Boolean isSelected,
      UUID productVariantId) implements AbstractModel {
    @Builder
    public CartItem(
        int quantity,
        double price,
        Boolean isSelected,
        UUID productVariantId) {
      this.quantity = quantity;
      this.price = price;
      this.isSelected = isSelected;
      this.productVariantId = productVariantId;
    }
  }
}
