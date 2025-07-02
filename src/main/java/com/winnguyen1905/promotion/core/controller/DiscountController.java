package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.core.annotation.MergedAnnotations.Search;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.winnguyen1905.promotion.model.request.*;
import com.winnguyen1905.promotion.model.response.*;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface DiscountController {
    // Admin endpoints
    ResponseEntity<Void> createDiscount(TAccountRequest accountRequest, AddDiscountRequest request);
    ResponseEntity<DiscountVm> getDiscountById(TAccountRequest accountRequest, @PathVariable UUID id);
    ResponseEntity<Void> updateDiscountStatus(TAccountRequest accountRequest, UpdateDiscountStatusRequest request);
    ResponseEntity<Void> assignProducts(TAccountRequest accountRequest, AssignProductsRequest request);
    ResponseEntity<Void> assignCategories(TAccountRequest accountRequest, AssignCategoriesRequest request);
    ResponseEntity<Void> updatePartialDiscount(TAccountRequest accountRequest, UpdateDiscountStatusRequest request);
    ResponseEntity<Void> deleteDiscount(TAccountRequest accountRequest, @PathVariable UUID id);
    ResponseEntity<Void> claimDiscount(TAccountRequest accountRequest, UUID discountId);
    ResponseEntity<DiscountValidityResponse> checkDiscountValidity(TAccountRequest accountRequest, @PathVariable UUID discountId);
    ResponseEntity<PagedResponse<DiscountVm>> getDiscounts(TAccountRequest accountRequest, SearchDiscountRequest request, Pageable pageable);
    
    ResponseEntity<ApplyDiscountResponse> applyDiscountToCart(TAccountRequest accountRequest, ApplyDiscountRequest request);
    ResponseEntity<ApplyDiscountResponse> applyDiscountToOrder(TAccountRequest accountRequest, ApplyDiscountRequest request);

    // // Shop endpoints
    // ResponseEntity<Void> updateShopDiscount(TAccountRequest accountRequest, UUID shopId, UpdateDiscountRequest request);
    // ResponseEntity<Void> updateShopDiscountStatus(TAccountRequest accountRequest, UUID shopId, UpdateDiscountStatusRequest request);
    // ResponseEntity<Void> assignShopProducts(TAccountRequest accountRequest, UUID shopId, AssignProductsRequest request);
    // // ResponseEntity<Void> assignBuyers(TAccountRequest accountRequest, UUID shopId, AssignBuyersRequest request);

    // // Customer endpoints
    // ResponseEntity<PagedResponse<DiscountVm>> getAvailableDiscounts(TAccountRequest accountRequest, Pageable pageable);
    // ResponseEntity<PagedResponse<DiscountVm>> getAvailableShopDiscounts(TAccountRequest accountRequest, UUID shopId, Pageable pageable);

}
