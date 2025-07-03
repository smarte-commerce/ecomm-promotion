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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.DiscountUsageService;
import com.winnguyen1905.promotion.model.request.CreateDiscountUsageRequest;
import com.winnguyen1905.promotion.model.response.DiscountUsageVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.ResponseMessage;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("discount-usages")
@RequiredArgsConstructor
public class DiscountUsageController {

  private final DiscountUsageService discountUsageService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseMessage(message = "Record discount usage success")
  public ResponseEntity<Void> recordUsage(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody CreateDiscountUsageRequest request) {
    discountUsageService.recordDiscountUsage(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get discount usage detail success")
  public ResponseEntity<DiscountUsageVm> getById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    return ResponseEntity.ok(discountUsageService.getDiscountUsageById(accountRequest, id));
  }

  @GetMapping("/customer/{customerId}")
  public ResponseEntity<PagedResponse<DiscountUsageVm>> getByCustomer(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID customerId,
      Pageable pageable) {
    return ResponseEntity.ok(discountUsageService.getCustomerDiscountUsage(accountRequest, customerId, pageable));
  }

  @GetMapping("/discount/{discountId}")
  public ResponseEntity<PagedResponse<DiscountUsageVm>> getByDiscount(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID discountId,
      Pageable pageable) {
    return ResponseEntity.ok(discountUsageService.getDiscountUsageHistory(accountRequest, discountId, pageable));
  }

  @GetMapping("/program/{programId}")
  public ResponseEntity<PagedResponse<DiscountUsageVm>> getByProgram(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID programId,
      Pageable pageable) {
    return ResponseEntity.ok(discountUsageService.getProgramUsageHistory(accountRequest, programId, pageable));
  }
} 
