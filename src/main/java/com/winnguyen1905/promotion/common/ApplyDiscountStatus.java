package com.winnguyen1905.promotion.common;

public enum ApplyDiscountStatus {
  REVIEW("review"), COMMIT("commit");

  final String ApplyDiscountStatus;

  ApplyDiscountStatus(String ApplyDiscountStatus) {
    this.ApplyDiscountStatus = ApplyDiscountStatus;
  }

  public String getApplyDiscountStatus() {
    return this.ApplyDiscountStatus;
  }
}
