package com.winnguyen1905.promotion.common;

public enum DiscountType {
  PERCENTAGE("percentage"),
  FIXED_AMOUNT("fixed_amount");

  private final String discountType;

  DiscountType(String discountType) {
    this.discountType = discountType;
  }

  public String getDiscountType() {
    return this.discountType;
  }
}
