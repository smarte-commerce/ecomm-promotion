package com.winnguyen1905.promotion.core.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.PromotionProgramService;
import com.winnguyen1905.promotion.model.request.CreatePromotionProgramRequest;
import com.winnguyen1905.promotion.model.request.SearchPromotionProgramRequest;
import com.winnguyen1905.promotion.model.request.UpdatePromotionProgramRequest;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PromotionProgramVm;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.oas.annotations.Operation;
// Swagger imports removed to avoid dependency issues
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotion-programs")
@RequiredArgsConstructor
// @Tag(name = "Promotion Programs", description = "Promotion Program Management
// API")
public class PromotionProgramController {

  private final PromotionProgramService promotionProgramService;

  @PostMapping
  // @Operation(summary = "Create a new promotion program")
  public ResponseEntity<RestResponse<Void>> createPromotionProgram(
      TAccountRequest accountRequest,
      @Valid @RequestBody CreatePromotionProgramRequest request) {

    promotionProgramService.createPromotionProgram(accountRequest, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RestResponse.<Void>builder()
            .message("Promotion program created successfully")
            .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get promotion program by ID")
  public ResponseEntity<RestResponse<PromotionProgramVm>> getPromotionProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    PromotionProgramVm program = promotionProgramService.getPromotionProgramById(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<PromotionProgramVm>builder()
        .data(program)
        .message("Promotion program retrieved successfully")
        .build());
  }

  @GetMapping
  @Operation(summary = "Search promotion programs")
  public ResponseEntity<RestResponse<PagedResponse<PromotionProgramVm>>> getPromotionPrograms(
      TAccountRequest accountRequest,
      SearchPromotionProgramRequest searchRequest,
      Pageable pageable) {

    PagedResponse<PromotionProgramVm> programs = promotionProgramService.getPromotionPrograms(
        accountRequest, searchRequest, pageable);

    return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionProgramVm>>builder()
        .data(programs)
        .message("Promotion programs retrieved successfully")
        .build());
  }

  @GetMapping("/active")
  @Operation(summary = "Get active promotion programs")
  public ResponseEntity<RestResponse<PagedResponse<PromotionProgramVm>>> getActivePrograms(
      TAccountRequest accountRequest,
      Pageable pageable) {

    PagedResponse<PromotionProgramVm> programs = promotionProgramService.getActivePrograms(accountRequest, pageable);

    return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionProgramVm>>builder()
        .data(programs)
        .message("Active promotion programs retrieved successfully")
        .build());
  }

  @GetMapping("/my-programs")
  @Operation(summary = "Get user's promotion programs")
  public ResponseEntity<RestResponse<PagedResponse<PromotionProgramVm>>> getUserPrograms(
      TAccountRequest accountRequest,
      Pageable pageable) {

    PagedResponse<PromotionProgramVm> programs = promotionProgramService.getUserPrograms(accountRequest, pageable);

    return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionProgramVm>>builder()
        .data(programs)
        .message("User promotion programs retrieved successfully")
        .build());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update promotion program")
  public ResponseEntity<RestResponse<Void>> updatePromotionProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody UpdatePromotionProgramRequest request) {

    promotionProgramService.updatePromotionProgram(accountRequest, id, request);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Promotion program updated successfully")
        .build());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete promotion program")
  public ResponseEntity<RestResponse<Void>> deletePromotionProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    promotionProgramService.deletePromotionProgram(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Promotion program deleted successfully")
        .build());
  }

  @PostMapping("/{id}/activate")
  @Operation(summary = "Activate promotion program")
  public ResponseEntity<RestResponse<Void>> activateProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    promotionProgramService.activateProgram(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Promotion program activated successfully")
        .build());
  }

  @PostMapping("/{id}/pause")
  @Operation(summary = "Pause promotion program")
  public ResponseEntity<RestResponse<Void>> pauseProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    promotionProgramService.pauseProgram(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Promotion program paused successfully")
        .build());
  }

  @PostMapping("/{id}/expire")
  @Operation(summary = "Expire promotion program")
  public ResponseEntity<RestResponse<Void>> expireProgram(
      TAccountRequest accountRequest,
      @PathVariable UUID id) {

    promotionProgramService.expireProgram(accountRequest, id);

    return ResponseEntity.ok(RestResponse.<Void>builder()
        .message("Promotion program expired successfully")
        .build());
  }
}
