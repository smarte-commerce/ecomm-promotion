package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.DiscountService;
import com.winnguyen1905.promotion.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.model.request.AssignCategoriesRequest;
import com.winnguyen1905.promotion.model.request.AssignProductsRequest;
import com.winnguyen1905.promotion.model.request.CheckoutRequest;
import com.winnguyen1905.promotion.model.request.ComprehensiveDiscountRequest;
import com.winnguyen1905.promotion.model.request.SearchDiscountRequest;
import com.winnguyen1905.promotion.model.request.UpdateDiscountStatusRequest;
import com.winnguyen1905.promotion.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.model.response.ComprehensiveDiscountResponse;
import com.winnguyen1905.promotion.model.response.DiscountValidityResponse;
import com.winnguyen1905.promotion.model.response.DiscountVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.ResponseMessage;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("discounts")
public class DiscountController {

  private final DiscountService discountService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseMessage(message = "Create discount success")
  public ResponseEntity<Void> createDiscount(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody AddDiscountRequest request) {
    discountService.createDiscount(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get discount detail success")
  public ResponseEntity<DiscountVm> getDiscountById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    return ResponseEntity.ok(discountService.getDiscountById(accountRequest, id));
  }

  @PatchMapping("/{id}/status")
  @ResponseMessage(message = "Update discount status success")
  public ResponseEntity<Void> updateDiscountStatus(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody UpdateDiscountStatusRequest request) {
    discountService.updateDiscountStatus(accountRequest, request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/assign-products")
  @ResponseMessage(message = "Assign products to discount success")
  public ResponseEntity<Void> assignProducts(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody AssignProductsRequest assignProductsRequest) {
    discountService.assignProducts(accountRequest, assignProductsRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/assign-categories")
  @ResponseMessage(message = "Assign categories to discount success")
  public ResponseEntity<Void> assignCategories(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody AssignCategoriesRequest assignCategoriesRequest) {
    discountService.assignCategories(accountRequest, assignCategoriesRequest);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/shops/{shopId}/{id}")
  @ResponseMessage(message = "Update shop discount success")
  public ResponseEntity<Void> updatePartialDiscount(TAccountRequest accountRequest,
      UpdateDiscountStatusRequest request) {
    return null;
  }

  public ResponseEntity<Void> deleteDiscount(TAccountRequest accountRequest, UUID id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteDiscount'");
  }

  public ResponseEntity<Void> claimDiscount(TAccountRequest accountRequest, UUID discountId) {
    this.discountService.claimDiscount(accountRequest, discountId);
    return ResponseEntity.ok().build();
  }

  public ResponseEntity<DiscountValidityResponse> checkDiscountValidity(TAccountRequest accountRequest,
      UUID discountId) {
    return ResponseEntity.ok(discountService.checkDiscountValidity(accountRequest, discountId));
  }

  public ResponseEntity<PagedResponse<DiscountVm>> getDiscounts(TAccountRequest accountRequest,
      SearchDiscountRequest request, Pageable pageable) {
    return ResponseEntity.ok(discountService.getDiscounts(accountRequest, request, pageable));
  }

  @PostMapping("/apply-discount-shop")
  public PriceStatisticsResponse postMethodName(@AccountRequest TAccountRequest accountRequest, @RequestBody CheckoutRequest request) {
    return discountService.applyDiscountToShop(accountRequest, request);
  }

  @PostMapping("/apply-to-cart")
  public ResponseEntity<ApplyDiscountResponse> applyDiscountToCart(@AccountRequest TAccountRequest accountRequest,
      @RequestBody ApplyDiscountRequest request) {
    return ResponseEntity.ok(discountService.applyDiscountToCart(accountRequest, request));
  }

  @PostMapping("/apply-to-order")
  @ResponseMessage(message = "Apply discount to order success")
  public ResponseEntity<ApplyDiscountResponse> applyDiscountToOrder(@AccountRequest TAccountRequest accountRequest,
      @RequestBody ApplyDiscountRequest request) {
    return ResponseEntity.ok(discountService.applyDiscountToOrder(accountRequest, request));
  }

  @PostMapping("/apply-comprehensive")
  @ResponseMessage(message = "Apply comprehensive discounts success")
  public ResponseEntity<ComprehensiveDiscountResponse> applyComprehensiveDiscounts(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody ComprehensiveDiscountRequest request) {
    return ResponseEntity.ok(discountService.applyComprehensiveDiscounts(accountRequest, request));
  }

}
