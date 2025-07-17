package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discounts")
@Tag(name = "Discounts", description = "Discount Management API")
public class DiscountController {

  private final DiscountService discountService;

  @PostMapping
  @Operation(summary = "Create a new discount")
  public ResponseEntity<RestResponse<Void>> createDiscount(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody AddDiscountRequest request) {
    discountService.createDiscount(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RestResponse.<Void>builder()
            .statusCode(HttpStatus.CREATED.value())
            .message("Discount created successfully")
            .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get discount by ID")
  public ResponseEntity<RestResponse<DiscountVm>> getDiscountById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    DiscountVm discount = discountService.getDiscountById(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<DiscountVm>builder()
        .statusCode(HttpStatus.OK.value())
        .data(discount)
        .message("Discount retrieved successfully")
        .build());
  }

  @PostMapping("/search")
  @Operation(summary = "Search discounts with filters")
  public ResponseEntity<RestResponse<PagedResponse<DiscountVm>>> getDiscounts(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody SearchDiscountRequest request,
      Pageable pageable) {
    PagedResponse<DiscountVm> discounts = discountService.getDiscounts(accountRequest, request, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<DiscountVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(discounts)
        .message("Discounts retrieved successfully")
        .build());
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Update discount status")
  public ResponseEntity<RestResponse<Void>> updateDiscountStatus(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateDiscountStatusRequest request) {
    discountService.updateDiscountStatus(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Discount status updated successfully")
        .build());
  }

  @PostMapping("/{id}/assign-products")
  @Operation(summary = "Assign products to discount")
  public ResponseEntity<RestResponse<Void>> assignProducts(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody AssignProductsRequest request) {
    discountService.assignProducts(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Products assigned to discount successfully")
        .build());
  }

  @DeleteMapping("/{id}/assign-products")
  @Operation(summary = "Remove products from discount")
  public ResponseEntity<RestResponse<Void>> unassignProducts(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody AssignProductsRequest request) {
    discountService.unassignProducts(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Products removed from discount successfully")
        .build());
  }

  @PostMapping("/{id}/assign-categories")
  @Operation(summary = "Assign categories to discount")
  public ResponseEntity<RestResponse<Void>> assignCategories(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody AssignCategoriesRequest request) {
    discountService.assignCategories(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Categories assigned to discount successfully")
        .build());
  }

  @DeleteMapping("/{id}/assign-categories")
  @Operation(summary = "Remove categories from discount")
  public ResponseEntity<RestResponse<Void>> unassignCategories(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody AssignCategoriesRequest request) {
    discountService.unassignCategories(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Categories removed from discount successfully")
        .build());
  }

  @PostMapping("/{discountId}/claim")
  @Operation(summary = "Claim discount for customer")
  public ResponseEntity<RestResponse<Void>> claimDiscount(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID discountId) {
    discountService.claimDiscount(accountRequest, discountId);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Discount claimed successfully")
        .build());
  }

  @GetMapping("/{discountId}/validity")
  @Operation(summary = "Check discount validity")
  public ResponseEntity<RestResponse<DiscountValidityResponse>> checkDiscountValidity(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID discountId) {
    DiscountValidityResponse validity = discountService.checkDiscountValidity(accountRequest, discountId);
    return ResponseEntity.ok(RestResponse.<DiscountValidityResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(validity)
        .message("Discount validity checked successfully")
        .build());
  }

  @PostMapping("/apply-to-shop")
  @Operation(summary = "Apply discount to shop checkout")
  public ResponseEntity<RestResponse<PriceStatisticsResponse>> applyDiscountToShop(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody CheckoutRequest request) {
    PriceStatisticsResponse result = discountService.applyDiscountToShop(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<PriceStatisticsResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(result)
        .message("Discount applied to shop successfully")
        .build());
  }

  @PostMapping("/apply-to-cart")
  @Operation(summary = "Apply discount to cart")
  public ResponseEntity<RestResponse<ApplyDiscountResponse>> applyDiscountToCart(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody ApplyDiscountRequest request) {
    ApplyDiscountResponse result = discountService.applyDiscountToCart(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<ApplyDiscountResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(result)
        .message("Discount applied to cart successfully")
        .build());
  }

  @PostMapping("/apply-to-order")
  @Operation(summary = "Apply discount to order")
  public ResponseEntity<RestResponse<ApplyDiscountResponse>> applyDiscountToOrder(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody ApplyDiscountRequest request) {
    ApplyDiscountResponse result = discountService.applyDiscountToOrder(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<ApplyDiscountResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(result)
        .message("Discount applied to order successfully")
        .build());
  }

  @PostMapping("/apply-comprehensive")
  @Operation(summary = "Apply comprehensive discounts to multiple orders")
  public ResponseEntity<RestResponse<ComprehensiveDiscountResponse>> applyComprehensiveDiscounts(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody ComprehensiveDiscountRequest request) {
    ComprehensiveDiscountResponse result = discountService.applyComprehensiveDiscounts(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<ComprehensiveDiscountResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(result)
        .message("Comprehensive discounts applied successfully")
        .build());
  }
}
