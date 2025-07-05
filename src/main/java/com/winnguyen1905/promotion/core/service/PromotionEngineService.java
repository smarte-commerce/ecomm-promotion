package com.winnguyen1905.promotion.core.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

/**
 * Core promotion engine service that handles complex business logic
 * for applying promotions, validating rules, and calculating discounts
 */
public interface PromotionEngineService {

  /**
   * Apply the best available promotions to a customer's order
   */
  ApplyDiscountResponse applyBestPromotions(TAccountRequest accountRequest, ApplyDiscountRequest request);

  /**
   * Calculate commission for all applicable vendors in an order
   */
  void processOrderCommissions(UUID orderId, UUID customerId, Double orderAmount, List<UUID> vendorIds);

  /**
   * Validate if a customer is eligible for a specific promotion
   */
  boolean validatePromotionEligibility(UUID customerId, UUID programId, ApplyDiscountRequest request);

  /**
   * Calculate discount amount for a specific promotion
   */
  Double calculatePromotionDiscount(UUID programId, ApplyDiscountRequest request);

  /**
   * Find all applicable promotions for a customer's order
   */
  List<UUID> findApplicablePromotions(TAccountRequest accountRequest, ApplyDiscountRequest request);

  /**
   * Process usage tracking when a promotion is applied
   */
  void recordPromotionUsage(UUID customerId, UUID programId, UUID discountId, UUID orderId, Double discountAmount);

  /**
   * Update analytics when a promotion is used
   */
  void updatePromotionAnalytics(UUID programId, UUID customerId, Double orderAmount, Double discountAmount);

  /**
   * Check if promotion usage limits are exceeded
   */
  boolean checkUsageLimits(UUID customerId, UUID programId, UUID discountId);

  /**
   * Calculate vendor contribution and platform commission
   */
  void calculateAndDistributeCommission(UUID programId, UUID vendorId, UUID orderId,
      UUID customerId, Double orderAmount, Double discountAmount);

  /**
   * Process flash sale purchase and update inventory
   */
  boolean processFlashSalePurchase(UUID flashSaleId, UUID customerId, Integer quantity);

  /**
   * Validate promotion stacking rules
   */
  boolean validatePromotionStacking(List<UUID> programIds);

  /**
   * Get customer's available promotions based on tier and history
   */
  List<UUID> getCustomerAvailablePromotions(UUID customerId);
}
