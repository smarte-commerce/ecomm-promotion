package com.winnguyen1905.promotion.model.response;

import java.time.Instant;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record DiscountValidityResponse(
    boolean isValid,
    String code,
    String message,
    Double minOrderValue,
    Double maxReducedValue,
    Instant expiryDate
) implements AbstractModel {
  @Builder
  public DiscountValidityResponse(
      boolean isValid,
      String code,
      String message,
      Double minOrderValue,
      Double maxReducedValue,
      Instant expiryDate) {
    this.isValid = isValid;
    this.code = code;
    this.message = message;
    this.minOrderValue = minOrderValue;
    this.maxReducedValue = maxReducedValue;
    this.expiryDate = expiryDate;
  }
}
