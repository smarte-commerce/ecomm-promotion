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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.CampaignService;
import com.winnguyen1905.promotion.model.request.AssignDiscountsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.CreateCampaignRequest;
import com.winnguyen1905.promotion.model.request.SearchCampaignRequest;
import com.winnguyen1905.promotion.model.request.UpdateCampaignRequest;
import com.winnguyen1905.promotion.model.response.CampaignStatisticsResponse;
import com.winnguyen1905.promotion.model.response.CampaignVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
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
      @org.springframework.web.bind.annotation.RequestParam Instant start,
      @org.springframework.web.bind.annotation.RequestParam Instant end) {
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
} 
