package com.winnguyen1905.promotion.core.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.common.ApplyDiscountStatus;
import com.winnguyen1905.promotion.core.model.Discount;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;

public interface DiscountService {

  PriceStatisticsResponse applyDiscountFactory(UUID customerId, ApplyDiscountRequest applyDiscountRequest);

  // Discount handleCreateDiscountCode(Discount discount, UUID shopId);

  // Discount handleGetDiscount(UUID id);

  // Discount handleGetAllDiscountCodesByShop(UUID shopId, Pageable pageable);

  // Discount handleGetAllProductsRelateDiscountCode(Discount discount, Pageable pageable);

  // PriceStatisticsResponse handleApplyDiscountCodeForCart(UUID customerId, ApplyDiscountRequest applyDiscountRequest, ApplyDiscountStatus applyDiscountStatus);

  // Boolean handleVerifyDiscountCode(UUID id);

  // void handleDeleteDiscountCode(UUID id);

  // void handleCancelDiscountCode(UUID id, String username);

  // void handleCancelDiscountForCart(Discount discount, UUID customerId);
}
