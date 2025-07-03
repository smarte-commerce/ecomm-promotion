package com.winnguyen1905.promotion.core.controller;

import java.time.Instant;
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
import org.springframework.web.bind.annotation.ResponseStatus;
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
import com.winnguyen1905.promotion.model.response.CampaignStatisticsResponse;
import com.winnguyen1905.promotion.model.response.CampaignVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PerformanceMetricsResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.ResponseMessage;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("campaigns")
public class CampaignController {

  private final CampaignService campaignService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseMessage(message = "Create campaign success")
  public ResponseEntity<Void> createCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody CreateCampaignRequest request) {
    campaignService.createCampaign(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  @ResponseMessage(message = "Get campaign detail success")
  public ResponseEntity<CampaignVm> getCampaignById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    return ResponseEntity.ok(campaignService.getCampaignById(accountRequest, id));
  }

  @PutMapping("/{id}")
  @ResponseMessage(message = "Update campaign success")
  public ResponseEntity<Void> updateCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @RequestBody UpdateCampaignRequest request) {
    campaignService.updateCampaign(accountRequest, id, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @ResponseMessage(message = "Delete campaign success")
  public ResponseEntity<Void> deleteCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    campaignService.deleteCampaign(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}/activate")
  @ResponseMessage(message = "Activate campaign success")
  public ResponseEntity<Void> activateCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    campaignService.activateCampaign(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}/deactivate")
  @ResponseMessage(message = "Deactivate campaign success")
  public ResponseEntity<Void> deactivateCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    campaignService.deactivateCampaign(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/search")
  @ResponseMessage(message = "Search campaigns success")
  public ResponseEntity<PagedResponse<CampaignVm>> getCampaigns(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody SearchCampaignRequest request,
      Pageable pageable) {
    return ResponseEntity.ok(campaignService.getCampaigns(accountRequest, request, pageable));
  }

  @GetMapping("/date-range")
  public ResponseEntity<java.util.List<CampaignVm>> getCampaignsByDateRange(
      @AccountRequest TAccountRequest accountRequest,
      @RequestParam Instant start,
      @RequestParam Instant end) {
    return ResponseEntity.ok(campaignService.getCampaignsByDateRange(accountRequest, start, end));
  }

  @GetMapping("/{campaignId}/statistics")
  public ResponseEntity<CampaignStatisticsResponse> getCampaignStatistics(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId) {
    return ResponseEntity.ok(campaignService.getCampaignStatistics(accountRequest, campaignId));
  }

  @PostMapping("/{campaignId}/assign-discounts")
  public ResponseEntity<Void> assignDiscountsToCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @RequestBody java.util.List<UUID> discountIds) {
    campaignService.assignDiscountsToCampaign(accountRequest,
        AssignDiscountsToCampaignRequest.builder().campaignId(campaignId).discountIds(discountIds).build());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/active")
  public ResponseEntity<PagedResponse<CampaignVm>> getActiveCampaigns(
      @AccountRequest TAccountRequest accountRequest,
      SearchCampaignRequest request,
      Pageable pageable) {
    return ResponseEntity.ok(campaignService.getActiveCampaigns(accountRequest, request, pageable));
  }

  @PostMapping("/{campaignId}/apply")
  public ResponseEntity<com.winnguyen1905.promotion.model.response.ApplyDiscountResponse> applyCampaign(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @RequestBody ApplyCampaignRequest request) {
    return ResponseEntity.ok(campaignService.applyCampaign(accountRequest, request));
  }

  @PatchMapping("/{campaignId}/schedule")
  public ResponseEntity<Void> updateSchedule(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @RequestBody UpdateScheduleRequest request) {
    campaignService.updateCampaignSchedule(accountRequest, campaignId, request);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{campaignId}/target-audience")
  public ResponseEntity<Void> updateTargetAudience(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @RequestBody UpdateTargetAudienceRequest request) {
    campaignService.updateTargetAudience(accountRequest, campaignId, request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{campaignId}/assign-vendors")
  public ResponseEntity<Void> assignVendors(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @RequestBody java.util.List<UUID> vendorIds) {
    campaignService.assignVendorsToCampaign(accountRequest,
        AssignVendorsToCampaignRequest.builder().campaignId(campaignId).vendorIds(vendorIds).build());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{campaignId}/products")
  public ResponseEntity<Void> assignProducts(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId,
      @RequestBody java.util.List<UUID> productIds) {
    campaignService.assignProductsToCampaign(accountRequest,
        AssignProductsToCampaignRequest.builder().campaignId(campaignId).productIds(productIds).build());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{campaignId}/performance")
  public ResponseEntity<PerformanceMetricsResponse> getPerformance(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID campaignId) {
    return ResponseEntity.ok(campaignService.getPerformanceMetrics(accountRequest, campaignId));
  }
}
