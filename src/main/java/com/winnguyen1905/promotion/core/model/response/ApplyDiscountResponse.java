package com.winnguyen1905.promotion.core.model.response;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;
import com.winnguyen1905.promotion.core.model.CustomerCart;

import lombok.Builder;

@Builder
public record ApplyDiscountResponse(
    UUID discountId,
    Boolean isOrder,
    CustomerCart cart) implements AbstractModel {

  public static record ApplyShopDiscount(
      UUID shopId,
      UUID discountId,
      CustomerCart.CustomerCartWithShop customerCartWithShop) {
  }

  public static record ApplyGlobalDiscount(
      UUID discountId,
      CustomerCart customerCart
    ) {
  }
}
