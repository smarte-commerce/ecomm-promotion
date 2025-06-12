package com.winnguyen1905.promotion.common;

public enum DiscountType {
  PERCENTAGE("PERCENTAGE"),
  FIXED_AMOUNT("FIXED_AMOUNT");

  private final String discountType;

  DiscountType(String discountType) {
    this.discountType = discountType;
  }

  public String getDiscountType() {
    return this.discountType;
  }
}
