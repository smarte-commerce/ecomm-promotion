package com.winnguyen1905.promotion.core.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;
import com.winnguyen1905.promotion.core.model.request.CustomerCart.CustomerCartWithShop;

import lombok.Builder;

@Builder
public record ApplyDiscountRequest(
    UUID shopId,
    UUID discountId,
    UUID customerId,
    UUID shopDiscountId,
    UUID shippingDiscountId,
    UUID globallyDiscountId,
    CustomerCartWithShop customerCartWithShop) implements AbstractModel {

  @Builder
  public ApplyDiscountRequest(
      UUID shopId,
      UUID discountId,
      UUID customerId,
      UUID shopDiscountId,
      UUID shippingDiscountId,
      UUID globallyDiscountId,
      CustomerCartWithShop customerCartWithShop) {
    this.shopId = shopId;
    this.discountId = discountId;
    this.customerId = customerId;
    this.shopDiscountId = shopDiscountId;
    this.shippingDiscountId = shippingDiscountId;
    this.globallyDiscountId = globallyDiscountId;
    this.customerCartWithShop = customerCartWithShop;
  }
}
