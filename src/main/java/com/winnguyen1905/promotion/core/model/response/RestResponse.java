package com.winnguyen1905.promotion.core.model.response;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record RestResponse<T>(Integer statusCode, String error, Object message, T data) implements AbstractModel {
  @Builder
  public RestResponse(Integer statusCode, String error, Object message, T data) {
    this.statusCode = statusCode;
    this.error = error;
    this.message = message;
    this.data = data;
  }
}
