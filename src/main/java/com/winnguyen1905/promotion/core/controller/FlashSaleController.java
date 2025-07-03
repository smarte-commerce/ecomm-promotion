package com.winnguyen1905.promotion.core.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.FlashSaleService;
import com.winnguyen1905.promotion.model.request.CreateFlashSaleRequest;
import com.winnguyen1905.promotion.model.response.FlashSaleVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/flash-sales")
@RequiredArgsConstructor
// (name = "Flash Sales", description = "Flash Sale Management API")
public class FlashSaleController {

  private final FlashSaleService flashSaleService;

  @PostMapping
  @Operation(summary = "Create a new flash sale")
  public ResponseEntity<RestResponse<Void>> createFlashSale(
      TAccountRequest accountRequest,
      @Valid @RequestBody CreateFlashSaleRequest request) {

    flashSaleService.createFlashSale(accountRequest, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RestResponse.<Void>builder()
            .message("Flash sale created successfully")
            .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get flash sale by ID")
  public ResponseEntity<RestResponse<FlashSaleVm>> getFlashSale(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    FlashSaleVm flashSale = flashSaleService.getFlashSaleById(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<FlashSaleVm>builder()
        .data(flashSale)
        .message("Flash sale retrieved successfully")
        .build());
  }

  @GetMapping("/program/{programId}")
  @Operation(summary = "Get flash sale by program ID")
  public ResponseEntity<RestResponse<FlashSaleVm>> getFlashSaleByProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID programId) {

    FlashSaleVm flashSale = flashSaleService.getFlashSaleByProgramId(accountRequest, programId);

    return ResponseEntity.ok(RestResponse.<FlashSaleVm>builder()
        .data(flashSale)
        .message("Flash sale retrieved successfully")
        .build());
  }

  @GetMapping
  @Operation(summary = "Get all flash sales")
  public ResponseEntity<RestResponse<PagedResponse<FlashSaleVm>>> getFlashSales(
      TAccountRequest accountRequest,
      Pageable pageable) {

    PagedResponse<FlashSaleVm> flashSales = flashSaleService.getFlashSales(accountRequest, pageable);

    return ResponseEntity.ok(RestResponse.<PagedResponse<FlashSaleVm>>builder()
        .data(flashSales)
        .message("Flash sales retrieved successfully")
        .build());
  }

  @GetMapping("/active")
  @Operation(summary = "Get active flash sales")
  public ResponseEntity<RestResponse<List<FlashSaleVm>>> getActiveFlashSales(
      TAccountRequest accountRequest) {

    List<FlashSaleVm> flashSales = flashSaleService.getActiveFlashSales(accountRequest);

    return ResponseEntity.ok(RestResponse.<List<FlashSaleVm>>builder()
        .data(flashSales)
        .message("Active flash sales retrieved successfully")
        .build());
  }

  @GetMapping("/upcoming")
  @Operation(summary = "Get upcoming flash sales")
  public ResponseEntity<RestResponse<List<FlashSaleVm>>> getUpcomingFlashSales(
      TAccountRequest accountRequest) {

    List<FlashSaleVm> flashSales = flashSaleService.getUpcomingFlashSales(accountRequest);

    return ResponseEntity.ok(RestResponse.<List<FlashSaleVm>>builder()
        .data(flashSales)
        .message("Upcoming flash sales retrieved successfully")
        .build());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update flash sale")
  public ResponseEntity<RestResponse<Void>> updateFlashSale(
      TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody CreateFlashSaleRequest request) {

    flashSaleService.updateFlashSale(accountRequest, id, request);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Flash sale updated successfully")
        .build());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete flash sale")
  public ResponseEntity<RestResponse<Void>> deleteFlashSale(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    flashSaleService.deleteFlashSale(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Flash sale deleted successfully")
        .build());
  }

  @PostMapping("/{id}/purchase")
  @Operation(summary = "Purchase flash sale item")
  public ResponseEntity<RestResponse<Void>> purchaseFlashSaleItem(
      TAccountRequest accountRequest,
      @PathVariable UUID id,
      @RequestParam Integer quantity) {

    flashSaleService.purchaseFlashSaleItem(accountRequest, id, quantity);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Flash sale item purchased successfully")
        .build());
  }
}
