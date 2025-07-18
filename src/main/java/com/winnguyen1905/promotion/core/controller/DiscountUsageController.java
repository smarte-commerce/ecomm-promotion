package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.DiscountUsageService;
import com.winnguyen1905.promotion.model.request.CreateDiscountUsageRequest;
import com.winnguyen1905.promotion.model.response.DiscountUsageVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/discount-usages")
@RequiredArgsConstructor
@Tag(name = "Discount Usages", description = "Discount Usage Tracking API")
public class DiscountUsageController {

  private final DiscountUsageService discountUsageService;

  @PostMapping
  @Operation(summary = "Record discount usage")
  public ResponseEntity<RestResponse<Void>> recordUsage(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody CreateDiscountUsageRequest request) {
    discountUsageService.recordDiscountUsage(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RestResponse.<Void>builder()
            .statusCode(HttpStatus.CREATED.value())
            .message("Discount usage recorded successfully")
            .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get discount usage by ID")
  public ResponseEntity<RestResponse<DiscountUsageVm>> getById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    DiscountUsageVm usage = discountUsageService.getDiscountUsageById(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<DiscountUsageVm>builder()
        .statusCode(HttpStatus.OK.value())
        .data(usage)
        .message("Discount usage retrieved successfully")
        .build());
  }

  @GetMapping("/customer/{customerId}")
  @Operation(summary = "Get discount usage by customer")
  public ResponseEntity<RestResponse<PagedResponse<DiscountUsageVm>>> getByCustomer(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID customerId,
      Pageable pageable) {
    PagedResponse<DiscountUsageVm> usages = discountUsageService.getCustomerDiscountUsage(accountRequest, customerId, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<DiscountUsageVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(usages)
        .message("Customer discount usages retrieved successfully")
        .build());
  }

  @GetMapping("/discount/{discountId}")
  @Operation(summary = "Get discount usage by discount")
  public ResponseEntity<RestResponse<PagedResponse<DiscountUsageVm>>> getByDiscount(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID discountId,
      Pageable pageable) {
    PagedResponse<DiscountUsageVm> usages = discountUsageService.getDiscountUsageHistory(accountRequest, discountId, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<DiscountUsageVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(usages)
        .message("Discount usage history retrieved successfully")
        .build());
  }

  @GetMapping("/program/{programId}")
  @Operation(summary = "Get discount usage by program")
  public ResponseEntity<RestResponse<PagedResponse<DiscountUsageVm>>> getByProgram(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID programId,
      Pageable pageable) {
    PagedResponse<DiscountUsageVm> usages = discountUsageService.getProgramUsageHistory(accountRequest, programId, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<DiscountUsageVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(usages)
        .message("Program usage history retrieved successfully")
        .build());
  }
} 
