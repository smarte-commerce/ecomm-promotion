package com.winnguyen1905.promotion.core.controller.garbage;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.common.ApplyDiscountStatus;
import com.winnguyen1905.promotion.core.service.DiscountService;
import com.winnguyen1905.promotion.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.model.request.AssignCategoriesRequest;
import com.winnguyen1905.promotion.model.request.AssignProductsRequest;
import com.winnguyen1905.promotion.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.model.request.UpdateDiscountStatusRequest;
import com.winnguyen1905.promotion.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.model.response.DiscountVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.ResponseMessage;
import com.winnguyen1905.promotion.secure.TAccountRequest;
import com.winnguyen1905.promotion.util.OptionalExtractor;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("discounts")
@RequiredArgsConstructor
public class DiscountControllerImpl {

  // private final DiscountService discountService;

  // // ADMIN API

  // @PostMapping
  // @ResponseStatus(HttpStatus.CREATED)
  // @ResponseMessage(message = "Create discount success")
  // public ResponseEntity<Void> createDiscount(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @RequestBody AddDiscountRequest request) {
  //   discountService.addDiscount(accountRequest.id(), request);
  //   return ResponseEntity.status(HttpStatus.CREATED).build();
  // }

  // // @GetMapping
  // // @ResponseMessage(message = "Get all discounts success")
  // // public ResponseEntity<PagedResponse<DiscountVm>> getAllDiscounts(
  // // @AccountRequest TAccountRequest accountRequest,
  // // Pageable pageable) {
  // // return ResponseEntity.ok(discountService.getAllDiscounts(pageable));
  // // }

  // @GetMapping("/{id}")
  // @ResponseMessage(message = "Get discount detail success")
  // public ResponseEntity<DiscountVm> getDiscountById(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID id) {
  //   return ResponseEntity.ok(discountService.getDiscountById(id));
  // }

  // // @PutMapping("/{id}")
  // // @ResponseMessage(message = "Update discount success")
  // // public ResponseEntity<Void> updateDiscount(
  // // @AccountRequest TAccountRequest accountRequest,
  // // @PathVariable UUID id,
  // // @RequestBody UpdateDiscountRequest request) {
  // // discountService.updateDiscount(accountRequest.id(), request);
  // // return ResponseEntity.ok().build();
  // // }

  // // @DeleteMapping("/{id}")
  // // @ResponseMessage(message = "Delete discount success")
  // // public ResponseEntity<Void> deleteDiscount(
  // // @AccountRequest TAccountRequest accountRequest,
  // // @PathVariable UUID id) {
  // // discountService.deleteDiscount(id);
  // // return ResponseEntity.noContent().build();
  // // }

  // @PatchMapping("/{id}/status")
  // @ResponseMessage(message = "Update discount status success")
  // public ResponseEntity<Void> updateDiscountStatus(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID id,
  //     @RequestBody UpdateDiscountStatusRequest request) {
  //   discountService.updateDiscountStatus(id, request);
  //   return ResponseEntity.ok().build();
  // }

  // @PostMapping("/{id}/assign-products")
  // @ResponseMessage(message = "Assign products to discount success")
  // public ResponseEntity<Void> assignProducts(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID id,
  //     @RequestBody AssignProductsRequest assignProductsRequest) {
  //   discountService.assignProducts(id, assignProductsRequest);
  //   return ResponseEntity.ok().build();
  // }

  // @PostMapping("/{id}/assign-categories")
  // @ResponseMessage(message = "Assign categories to discount success")
  // public ResponseEntity<Void> assignCategories(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID id,
  //     @RequestBody AssignCategoriesRequest assignCategoriesRequest) {
  //   discountService.assignCategories(id, assignCategoriesRequest.categoryIds());
  //   return ResponseEntity.ok().build();
  // }

  // // SHOP API
  // @PostMapping("/create")
  // @ResponseMessage(message = "Create shop discount success")
  // public ResponseEntity<Void> createShopDiscount(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     @RequestBody AddDiscountRequest request) {
  //   discountService.addShopDiscount(accountRequest.id(), shopId, request);
  //   return ResponseEntity.status(HttpStatus.CREATED).build();
  // }

  // @GetMapping("/shops/{shopId}")
  // @ResponseMessage(message = "Get shop discounts success")
  // public ResponseEntity<PagedResponse<DiscountVm>> getShopDiscounts(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     Pageable pageable) {
  //   return ResponseEntity.ok(discountService.getShopDiscounts(shopId, pageable));
  // }

  // // @GetMapping("/{shops/{shopId}/{id}}")
  // // @ResponseMessage(message = "Get shop discount detail success")
  // // public ResponseEntity<DiscountVm> getShopDiscountById(
  // //     @AccountRequest TAccountRequest accountRequest,
  // //     @PathVariable UUID shopId,
  // //     @PathVariable UUID id) {
  // //   return ResponseEntity.ok(discountService.getShopDiscountById(shopId, id));
  // // }

  // @PutMapping("/shops/{shopId}/{id}")
  // @ResponseMessage(message = "Update shop discount success")
  // public ResponseEntity<Void> updateShopDiscount(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     @PathVariable UUID id,
  //     @RequestBody UpdateDiscountRequest request) {
  //   discountService.updateShopDiscount(accountRequest.id(), shopId, id, request);
  //   return ResponseEntity.ok().build();
  // }

  // @DeleteMapping("/shops/{shopId}/{id}")
  // @ResponseMessage(message = "Delete shop discount success")
  // public ResponseEntity<Void> deleteShopDiscount(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     @PathVariable UUID id) {
  //   discountService.deleteShopDiscount(shopId, id);
  //   return ResponseEntity.noContent().build();
  // }

  // @PatchMapping("/shops/{shopId}/{id}/status")
  // @ResponseMessage(message = "Update shop discount status success")
  // public ResponseEntity<Void> updateShopDiscountStatus(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     @PathVariable UUID id,
  //     @RequestBody UpdateDiscountStatusRequest request) {
  //   discountService.updateShopDiscountStatus(shopId, id, request.isActive());
  //   return ResponseEntity.ok().build();
  // }

  // @PostMapping("/shops/{shopId}/{id}/assign-products")
  // @ResponseMessage(message = "Assign products to shop discount success")
  // public ResponseEntity<Void> assignShopProducts(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     @PathVariable UUID id,
  //     @RequestBody AssignProductsRequest request) {
  //   discountService.assignShopProducts(shopId, id, request.productIds());
  //   return ResponseEntity.ok().build();
  // }

  // @PostMapping("/shops/{shopId}/{id}/assign-buyers")
  // @ResponseMessage(message = "Assign buyers to shop discount success")
  // public ResponseEntity<Void> assignBuyers(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     @PathVariable UUID id,
  //     @RequestBody AssignBuyersRequest request) {
  //   discountService.assignBuyers(shopId, id, request.buyerIds());
  //   return ResponseEntity.ok().build();
  // }

  // // CUSTOMER API

  // @GetMapping("/available")
  // @ResponseMessage(message = "Get available discounts success")
  // public ResponseEntity<PagedResponse<DiscountVm>> getAvailableDiscounts(
  //     @AccountRequest TAccountRequest accountRequest,
  //     Pageable pageable) {
  //   return ResponseEntity.ok(discountService.getAvailableDiscounts(accountRequest.id(), pageable));
  // }

  // @GetMapping("/shops/{shopId}/available")
  // @ResponseMessage(message = "Get available shop discounts success")
  // public ResponseEntity<PagedResponse<DiscountVm>> getAvailableShopDiscounts(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID shopId,
  //     Pageable pageable) {
  //   return ResponseEntity.ok(discountService.getAvailableShopDiscounts(accountRequest.id(), shopId, pageable));
  // }

  // @PostMapping("/claim/{code}")
  // @ResponseMessage(message = "Claim discount success")
  // public ResponseEntity<Void> claimDiscount(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable String code) {
  //   discountService.claimDiscount(accountRequest.id(), code);
  //   return ResponseEntity.ok().build();
  // }

  // @GetMapping("/users/{userId}")
  // @ResponseMessage(message = "Get user discounts success")
  // public ResponseEntity<PagedResponse<DiscountVm>> getUserDiscounts(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @PathVariable UUID userId,
  //     Pageable pageable) {
  //   return ResponseEntity.ok(discountService.getUserDiscounts(userId, pageable));
  // }

  // @PostMapping("/cart/apply-discount")
  // @ResponseMessage(message = "Apply discount to cart success")
  // public ResponseEntity<ApplyDiscountResponse> applyDiscountToCart(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @RequestBody ApplyDiscountRequest request) {
  //   return ResponseEntity.ok(discountService.applyDiscountForCart(accountRequest.id(), request));
  // }

  // @DeleteMapping("/cart/remove-discount")
  // @ResponseMessage(message = "Remove discount from cart success")
  // public ResponseEntity<Void> removeDiscountFromCart(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @RequestBody ApplyDiscountRequest request) {
  //   discountService.removeDiscountFromCart(accountRequest.id(), request);
  //   return ResponseEntity.ok().build();
  // }

  
  // @GetMapping("/check-validity")
  // @ResponseMessage(message = "Check discount validity success")
  // public ResponseEntity<DiscountValidityResponse> checkDiscountValidity(
  //     @AccountRequest TAccountRequest accountRequest,
  //     @RequestParam String code) {
  //   return ResponseEntity.ok(discountService.checkDiscountValidity(accountRequest.id(), code));
  // }
}
