package com.winnguyen1905.promotion.core.service;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;

public interface DiscountService {
  void addDiscount(UUID userId, AddDiscountRequest addDiscountRequest);
  void updateDiscount(UUID userId, UpdateDiscountRequest updateDiscountRequest);
  ApplyDiscountResponse applyDiscountForCart(UUID customerId, ApplyDiscountRequest applyDiscountRequest);
  ApplyDiscountResponse applyDiscountForOrder(UUID customerId, ApplyDiscountRequest applyDiscountRequest);
}
