package com.winnguyen1905.promotion.core.controller;

import java.time.Instant;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.CampaignService;
import com.winnguyen1905.promotion.model.request.AssignDiscountsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.CreateCampaignRequest;
import com.winnguyen1905.promotion.model.request.SearchCampaignRequest;
import com.winnguyen1905.promotion.model.request.UpdateCampaignRequest;
import com.winnguyen1905.promotion.model.request.UpdateScheduleRequest;
import com.winnguyen1905.promotion.model.request.UpdateTargetAudienceRequest;
import com.winnguyen1905.promotion.model.request.AssignVendorsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.AssignProductsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.ApplyCampaignRequest;
import com.winnguyen1905.promotion.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.model.response.CampaignStatisticsResponse;
import com.winnguyen1905.promotion.model.response.CampaignVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PerformanceMetricsResponse;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campaigns", description = "Campaign Management API")
public class CampaignController {

  private final CampaignService campaignService;

  @PostMapping
  @Operation(summary = "Create a new campaign")
  public ResponseEntity<RestResponse<Void>> createCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody CreateCampaignRequest request) {
    campaignService.createCampaign(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RestResponse.<Void>builder()
            .statusCode(HttpStatus.CREATED.value())
            .message("Campaign created successfully")
            .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get campaign by ID")
  public ResponseEntity<RestResponse<CampaignVm>> getCampaignById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    CampaignVm campaign = campaignService.getCampaignById(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<CampaignVm>builder()
        .statusCode(HttpStatus.OK.value())
        .data(campaign)
        .message("Campaign retrieved successfully")
        .build());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update campaign")
  public ResponseEntity<RestResponse<Void>> updateCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCampaignRequest request) {
    campaignService.updateCampaign(accountRequest, id, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Campaign updated successfully")
        .build());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete campaign")
  public ResponseEntity<RestResponse<Void>> deleteCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    campaignService.deleteCampaign(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Campaign deleted successfully")
        .build());
  }

  @PatchMapping("/{id}/activate")
  @Operation(summary = "Activate campaign")
  public ResponseEntity<RestResponse<Void>> activateCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    campaignService.activateCampaign(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Campaign activated successfully")
        .build());
  }

  @PatchMapping("/{id}/deactivate")
  @Operation(summary = "Deactivate campaign")
  public ResponseEntity<RestResponse<Void>> deactivateCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    campaignService.deactivateCampaign(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Campaign deactivated successfully")
        .build());
  }

  @PostMapping("/search")
  @Operation(summary = "Search campaigns with filters")
  public ResponseEntity<RestResponse<PagedResponse<CampaignVm>>> getCampaigns(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody SearchCampaignRequest request,
      Pageable pageable) {
    PagedResponse<CampaignVm> campaigns = campaignService.getCampaigns(accountRequest, request, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<CampaignVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(campaigns)
        .message("Campaigns retrieved successfully")
        .build());
  }

  @GetMapping("/active")
  @Operation(summary = "Get active campaigns")
  public ResponseEntity<RestResponse<PagedResponse<CampaignVm>>> getActiveCampaigns(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody SearchCampaignRequest request,
      Pageable pageable) {
    PagedResponse<CampaignVm> campaigns = campaignService.getActiveCampaigns(accountRequest, request, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<CampaignVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(campaigns)
        .message("Active campaigns retrieved successfully")
        .build());
  }

  @GetMapping("/date-range")
  @Operation(summary = "Get campaigns by date range")
  public ResponseEntity<RestResponse<List<CampaignVm>>> getCampaignsByDateRange(
      @AccountRequest TAccountRequest accountRequest,
      @RequestParam Instant start,
      @RequestParam Instant end) {
    List<CampaignVm> campaigns = campaignService.getCampaignsByDateRange(accountRequest, start, end);
    return ResponseEntity.ok(RestResponse.<List<CampaignVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(campaigns)
        .message("Campaigns by date range retrieved successfully")
        .build());
  }

  @GetMapping("/{campaignId}/statistics")
  @Operation(summary = "Get campaign statistics")
  public ResponseEntity<RestResponse<CampaignStatisticsResponse>> getCampaignStatistics(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId) {
    CampaignStatisticsResponse statistics = campaignService.getCampaignStatistics(accountRequest, campaignId);
    return ResponseEntity.ok(RestResponse.<CampaignStatisticsResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(statistics)
        .message("Campaign statistics retrieved successfully")
        .build());
  }

  @PostMapping("/{campaignId}/assign-discounts")
  @Operation(summary = "Assign discounts to campaign")
  public ResponseEntity<RestResponse<Void>> assignDiscountsToCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @Valid @RequestBody List<UUID> discountIds) {
    AssignDiscountsToCampaignRequest request = AssignDiscountsToCampaignRequest.builder()
        .campaignId(campaignId)
        .discountIds(discountIds)
        .build();
    campaignService.assignDiscountsToCampaign(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Discounts assigned to campaign successfully")
        .build());
  }

  @PostMapping("/{campaignId}/assign-vendors")
  @Operation(summary = "Assign vendors to campaign")
  public ResponseEntity<RestResponse<Void>> assignVendors(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @Valid @RequestBody List<UUID> vendorIds) {
    AssignVendorsToCampaignRequest request = AssignVendorsToCampaignRequest.builder()
        .campaignId(campaignId)
        .vendorIds(vendorIds)
        .build();
    campaignService.assignVendorsToCampaign(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Vendors assigned to campaign successfully")
        .build());
  }

  @PostMapping("/{campaignId}/assign-products")
  @Operation(summary = "Assign products to campaign")
  public ResponseEntity<RestResponse<Void>> assignProducts(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @Valid @RequestBody List<UUID> productIds) {
    AssignProductsToCampaignRequest request = AssignProductsToCampaignRequest.builder()
        .campaignId(campaignId)
        .productIds(productIds)
        .build();
    campaignService.assignProductsToCampaign(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Products assigned to campaign successfully")
        .build());
  }

  @PostMapping("/{campaignId}/apply")
  @Operation(summary = "Apply campaign to customer order")
  public ResponseEntity<RestResponse<ApplyDiscountResponse>> applyCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @Valid @RequestBody ApplyCampaignRequest request) {
    ApplyDiscountResponse result = campaignService.applyCampaign(accountRequest, request);
    return ResponseEntity.ok(RestResponse.<ApplyDiscountResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(result)
        .message("Campaign applied successfully")
        .build());
  }

  @PatchMapping("/{campaignId}/schedule")
  @Operation(summary = "Update campaign schedule")
  public ResponseEntity<RestResponse<Void>> updateSchedule(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @Valid @RequestBody UpdateScheduleRequest request) {
    campaignService.updateCampaignSchedule(accountRequest, campaignId, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Campaign schedule updated successfully")
        .build());
  }

  @PatchMapping("/{campaignId}/target-audience")
  @Operation(summary = "Update campaign target audience")
  public ResponseEntity<RestResponse<Void>> updateTargetAudience(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @Valid @RequestBody UpdateTargetAudienceRequest request) {
    campaignService.updateTargetAudience(accountRequest, campaignId, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Campaign target audience updated successfully")
        .build());
  }

  @GetMapping("/{campaignId}/performance")
  @Operation(summary = "Get campaign performance metrics")
  public ResponseEntity<RestResponse<PerformanceMetricsResponse>> getPerformance(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId) {
    PerformanceMetricsResponse metrics = campaignService.getPerformanceMetrics(accountRequest, campaignId);
    return ResponseEntity.ok(RestResponse.<PerformanceMetricsResponse>builder()
        .statusCode(HttpStatus.OK.value())
        .data(metrics)
        .message("Campaign performance metrics retrieved successfully")
        .build());
  }
}
