package com.winnguyen1905.promotion.common;

public enum DiscountUsageStatus {
  SUCCESS("SUCCESS"),
  FAILED("FAILED"),
  SPENDING("SPENDING");

  private final String discountUsageStatus;

  DiscountUsageStatus(String discountUsageStatus) {
    this.discountUsageStatus = discountUsageStatus;
  }

  public String getDiscountUsageStatus() {
    return discountUsageStatus;
  }
}
