package com.winnguyen1905.promotion.core.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.request.AssignDiscountsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.CreateCampaignRequest;
import com.winnguyen1905.promotion.model.request.SearchCampaignRequest;
import com.winnguyen1905.promotion.model.request.UpdateCampaignRequest;
import com.winnguyen1905.promotion.model.response.CampaignStatisticsResponse;
import com.winnguyen1905.promotion.model.response.CampaignVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface CampaignService {

  void createCampaign(TAccountRequest accountRequest, CreateCampaignRequest request);

  CampaignVm getCampaignById(TAccountRequest accountRequest, UUID id);

  void updateCampaign(TAccountRequest accountRequest, UUID id, UpdateCampaignRequest request);

  void deleteCampaign(TAccountRequest accountRequest, UUID id);

  void activateCampaign(TAccountRequest accountRequest, UUID id);

  void deactivateCampaign(TAccountRequest accountRequest, UUID id);

  PagedResponse<CampaignVm> getCampaigns(TAccountRequest accountRequest, SearchCampaignRequest request, Pageable pageable);

  List<CampaignVm> getCampaignsByDateRange(TAccountRequest accountRequest, Instant start, Instant end);

  CampaignStatisticsResponse getCampaignStatistics(TAccountRequest accountRequest, UUID campaignId);

  void assignDiscountsToCampaign(TAccountRequest accountRequest, AssignDiscountsToCampaignRequest request);
} 
