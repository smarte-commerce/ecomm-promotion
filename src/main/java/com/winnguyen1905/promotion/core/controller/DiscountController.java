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

import com.winnguyen1905.promotion.common.ApplyDiscountStatus;
import com.winnguyen1905.promotion.core.model.Discount;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.core.service.DiscountService;
import com.winnguyen1905.promotion.util.OptionalExtractor;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("discounts")
public class DiscountController {
  private final DiscountService discountService;

  // @GetMapping("/")
  // public ResponseEntity<Discount> getAllDiscountCodeWithProducts(Pageable pageable,
  //     @RequestBody Discount discount) {
  //   return ResponseEntity.ok(this.discountService.handleGetAllProductsRelateDiscountCode(discount, pageable));
  // }

  // @GetMapping("/shop/{shop-id}")
  // public ResponseEntity<Discount> getAllDiscountCodesbyShop(Pageable pageable,
  //     @PathVariable("shop-id") UUID shopId) {
  //   return ResponseEntity.ok(this.discountService.handleGetAllDiscountCodesByShop(shopId, pageable));
  // }

  // @PostMapping("/apply")
  // public ResponseEntity<PriceStatisticsResponse> getAmountApplyDiscountForCart(@RequestBody ApplyDiscountRequest discount) {
  //   UUID customerId = OptionalExtractor.extractUserId();
  //   return ResponseEntity.ok().body(
  //       this.discountService.handleApplyDiscountCodeForCart(customerId, discount, ApplyDiscountStatus.REVIEW));
  // }

  // @PostMapping("/cancel")
  // public ResponseEntity<Void> postMethodName(@RequestBody Discount discount) {
  //   UUID customerId = OptionalExtractor.extractUserId();
  //   this.discountService.handleCancelDiscountForCart(discount, customerId);
  //   return ResponseEntity.noContent().build();
  // }

  // API FOR SHOPOWNER

  // @PostMapping
  // public ResponseEntity<Discount> createDiscountCode(@RequestBody @Valid Discount discount) {
  //   UUID shopId = OptionalExtractor.extractUserId();
  //   return ResponseEntity.status(HttpStatus.CREATED.value())
  //       .body(this.discountService.handleCreateDiscountCode(discount, shopId));
  // }
}
