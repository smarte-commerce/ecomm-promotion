package com.winnguyen1905.promotion.core.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CheckoutRequest implements AbstractModel {
  private UUID shopId;
  private double total;
  private UUID shippingDiscountId;
  private UUID shopProductDiscountId;
  private UUID globalProductDiscountId;
}
