package com.winnguyen1905.promotion.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.request.*;
import com.winnguyen1905.promotion.model.response.*;
import com.winnguyen1905.promotion.secure.TAccountRequest;

//     ResponseEntity<Void> createDiscount(TAccountRequest accountRequest, AddDiscountRequest request);
//     ResponseEntity<DiscountVm> getDiscountById(TAccountRequest accountRequest, @PathVariable UUID id);
//     ResponseEntity<Void> updateDiscountStatus(TAccountRequest accountRequest, UUID id, UpdateDiscountStatusRequest request);
//     ResponseEntity<Void> assignProducts(TAccountRequest accountRequest, UUID id, AssignProductsRequest request);
//     ResponseEntity<Void> assignCategories(TAccountRequest accountRequest, UUID id, AssignCategoriesRequest request);
//     ResponseEntity<Void> updatePartialDiscount(TAccountRequest accountRequest, UUID id, UpdateDiscountStatusRequest request);
//     ResponseEntity<Void> deleteDiscount(TAccountRequest accountRequest, @PathVariable UUID id);
//     ResponseEntity<Void> claimDiscount(TAccountRequest accountRequest, String code);
//     ResponseEntity<DiscountValidityResponse> checkDiscountValidity(TAccountRequest accountRequest, @PathVariable UUID discountId);
//     ResponseEntity<PagedResponse<DiscountVm>> getDiscounts(TAccountRequest accountRequest, SearchDiscountRequest request, Pageable pageable);
//     ResponseEntity<ApplyDiscountResponse> applyDiscountToCart(TAccountRequest accountRequest, ApplyDiscountRequest request);
//     ResponseEntity<ApplyDiscountResponse> applyDiscountToOrder(TAccountRequest accountRequest, ApplyDiscountRequest request);

public interface DiscountService {
  // Admin endpoints
  void createDiscount(TAccountRequest accountRequest, AddDiscountRequest request);

  DiscountVm getDiscountById(TAccountRequest accountRequest, UUID id);

  void updateDiscountStatus(TAccountRequest accountRequest, UpdateDiscountStatusRequest request);

  void assignProducts(TAccountRequest accountRequest, AssignProductsRequest request);

  void unassignProducts(TAccountRequest accountRequest, AssignProductsRequest request);

  void assignCategories(TAccountRequest accountRequest, AssignCategoriesRequest request);

  void unassignCategories(TAccountRequest accountRequest, AssignCategoriesRequest request);

  PagedResponse<DiscountVm> getDiscounts(TAccountRequest accountRequest, SearchDiscountRequest request,
      Pageable pageable);

  void claimDiscount(TAccountRequest accountRequest, UUID discountId);

  DiscountValidityResponse checkDiscountValidity(TAccountRequest accountRequest, UUID discountId);

  ApplyDiscountResponse applyDiscountToCart(TAccountRequest accountRequest, ApplyDiscountRequest request);

  ApplyDiscountResponse applyDiscountToOrder(TAccountRequest accountRequest, ApplyDiscountRequest request);

  ComprehensiveDiscountResponse applyComprehensiveDiscounts(TAccountRequest accountRequest, ComprehensiveDiscountRequest request);

  PriceStatisticsResponse applyDiscountToShop(TAccountRequest accountRequest, CheckoutRequest request);

  // // Shop endpoints
  // void createShopDiscount(TAccountRequest accountRequest, UUID shopId,
  // AddDiscountRequest request);
  // PagedResponse<DiscountVm> getShopDiscounts(TAccountRequest accountRequest,
  // UUID shopId, Pageable pageable);
  // void updateShopDiscount(TAccountRequest accountRequest, UUID shopId, UUID id,
  // UpdateDiscountRequest request);
  // void deleteShopDiscount(TAccountRequest accountRequest, UUID shopId, UUID
  // id);
  // void updateShopDiscountStatus(TAccountRequest accountRequest, UUID shopId,
  // UUID id, UpdateDiscountStatusRequest request);
  // void assignShopProducts(TAccountRequest accountRequest, UUID shopId, UUID id,
  // AssignProductsRequest request);
  // // void assignBuyers(TAccountRequest accountRequest, UUID shopId, UUID id,
  // AssignBuyersRequest request);

  // // Customer endpoints
  // PagedResponse<DiscountVm> getAvailableDiscounts(TAccountRequest
  // accountRequest, Pageable pageable);
  // PagedResponse<DiscountVm> getAvailableShopDiscounts(TAccountRequest
  // accountRequest, UUID shopId, Pageable pageable);
  // PagedResponse<DiscountVm> getUserDiscounts(TAccountRequest accountRequest,
  // UUID userId, Pageable pageable);
  // void removeDiscountFromCart(TAccountRequest accountRequest,
  // ApplyDiscountRequest request);
}
