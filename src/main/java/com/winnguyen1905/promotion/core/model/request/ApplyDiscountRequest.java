package com.winnguyen1905.promotion.core.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;

import lombok.Builder;

@Builder
public record ApplyDiscountRequest(
    UUID shopId,
    UUID discountId,
    UUID customerId,
    UUID shopDiscountId,
    UUID shippingDiscountId,
    UUID globallyDiscountId,
    CustomerCartWithShop customerCartWithShop) {}
