package com.winnguyen1905.promotion.core.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.core.model.CustomerCart;
import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;

public interface ApplyDiscountRequest {
  UUID discountId();
  DiscountCategory discountCategory();
  PriceStatisticsResponse priceStatisticsResponse();

  public static record ApplyShopDiscount(
      UUID shopId,
      UUID discountId,
      CustomerCartWithShop customerCartWithShop,
      PriceStatisticsResponse priceStatisticsResponse) implements ApplyDiscountRequest {

    public CustomerCartWithShop customerCart() {
      return customerCartWithShop;
    }

    @Override
    public DiscountCategory discountCategory() {
      return null;
    }
  }

  public static record ApplyGlobalDiscount(
      UUID discountId,
      CustomerCart customerCart,
      DiscountCategory discountCategory,
      PriceStatisticsResponse priceStatisticsResponse) implements ApplyDiscountRequest {

    public CustomerCart customerCart() {
      return customerCart;
    }
  }
}
