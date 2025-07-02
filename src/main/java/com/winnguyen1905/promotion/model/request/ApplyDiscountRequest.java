package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.model.request.CustomerCart.CustomerCartWithShop;

import lombok.Builder;

@Builder
public record ApplyDiscountRequest(
    UUID shopId,
    UUID discountId,
    UUID customerId,
    UUID shopDiscountId,
    UUID shippingDiscountId,
    UUID globallyDiscountId,
    CustomerCartWithShop customerCartWithShop,
    // Optimistic locking support
    Long discountVersion,
    Long shopDiscountVersion,
    Long shippingDiscountVersion,
    Long globallyDiscountVersion) implements AbstractModel {

  @Builder
  public ApplyDiscountRequest(
      UUID shopId,
      UUID discountId,
      UUID customerId,
      UUID shopDiscountId,
      UUID shippingDiscountId,
      UUID globallyDiscountId,
      CustomerCartWithShop customerCartWithShop,
      Long discountVersion,
      Long shopDiscountVersion,
      Long shippingDiscountVersion,
      Long globallyDiscountVersion) {
    this.shopId = shopId;
    this.discountId = discountId;
    this.customerId = customerId;
    this.shopDiscountId = shopDiscountId;
    this.shippingDiscountId = shippingDiscountId;
    this.globallyDiscountId = globallyDiscountId;
    this.customerCartWithShop = customerCartWithShop;
    this.discountVersion = discountVersion;
    this.shopDiscountVersion = shopDiscountVersion;
    this.shippingDiscountVersion = shippingDiscountVersion;
    this.globallyDiscountVersion = globallyDiscountVersion;
  }

  /**
   * Creates a new ApplyDiscountRequest with updated version information
   * for optimistic locking support.
   */
  public ApplyDiscountRequest withVersions(Long discountVersion, Long shopDiscountVersion,
                                         Long shippingDiscountVersion, Long globallyDiscountVersion) {
    return new ApplyDiscountRequest(
        this.shopId,
        this.discountId,
        this.customerId,
        this.shopDiscountId,
        this.shippingDiscountId,
        this.globallyDiscountId,
        this.customerCartWithShop,
        discountVersion,
        shopDiscountVersion,
        shippingDiscountVersion,
        globallyDiscountVersion
    );
  }
}


