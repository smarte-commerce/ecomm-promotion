package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.winnguyen1905.promotion.core.service.CampaignService;
import com.winnguyen1905.promotion.model.request.AssignDiscountsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.CreateCampaignRequest;
import com.winnguyen1905.promotion.model.request.SearchCampaignRequest;
import com.winnguyen1905.promotion.model.request.UpdateCampaignRequest;
import com.winnguyen1905.promotion.model.response.CampaignStatisticsResponse;
import com.winnguyen1905.promotion.model.response.CampaignVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.persistance.entity.ECampaign;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignStatus;
import com.winnguyen1905.promotion.persistance.repository.CampaignRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.specification.CampaignSpecification;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final DiscountRepository discountRepository;

  private CampaignVm toVm(ECampaign campaign) {
    if (campaign == null)
      return null;
    return CampaignVm.builder()
        .id(campaign.getId())
        .name(campaign.getName())
        .description(campaign.getDescription())
        .campaignType(campaign.getCampaignType())
        .startDate(campaign.getStartDate())
        .endDate(campaign.getEndDate())
        .status(campaign.getStatus())
        .budget(campaign.getBudget())
        .spentBudget(campaign.getSpentBudget())
        .targetAudience(campaign.getTargetAudience())
        .createdBy(campaign.getCreatedBy())
        .build();
  }

  @Override
  public void createCampaign(TAccountRequest accountRequest, CreateCampaignRequest request) {
    ECampaign campaign = ECampaign.builder()
        .name(request.name())
        .description(request.description())
        .campaignType(request.campaignType())
        .startDate(request.startDate())
        .endDate(request.endDate())
        .budget(request.budget())
        .spentBudget(0.0)
        .targetAudience(request.targetAudience())
        .createdBy(accountRequest.id())
        .status(CampaignStatus.DRAFT)
        .build();

    campaignRepository.save(campaign);
  }

  @Override
  public CampaignVm getCampaignById(TAccountRequest accountRequest, UUID id) {
    ECampaign campaign = campaignRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    return toVm(campaign);
  }

  @Override
  @Transactional
  public void updateCampaign(TAccountRequest accountRequest, UUID id, UpdateCampaignRequest request) {
    ECampaign campaign = campaignRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    if (request.version() != null && campaign.getVersion() != request.version()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign has been modified by another transaction");
    }

    if (request.name() != null)
      campaign.setName(request.name());
    if (request.description() != null)
      campaign.setDescription(request.description());
    if (request.campaignType() != null)
      campaign.setCampaignType(request.campaignType());
    if (request.startDate() != null)
      campaign.setStartDate(request.startDate());
    if (request.endDate() != null)
      campaign.setEndDate(request.endDate());
    if (request.status() != null)
      campaign.setStatus(request.status());
    if (request.budget() != null)
      campaign.setBudget(request.budget());
    if (request.targetAudience() != null)
      campaign.setTargetAudience(request.targetAudience());

    campaignRepository.save(campaign);
  }

  @Override
  public void deleteCampaign(TAccountRequest accountRequest, UUID id) {
    if (!campaignRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found");
    }
    campaignRepository.deleteById(id);
  }

  @Override
  public void activateCampaign(TAccountRequest accountRequest, UUID id) {
    updateStatus(id, CampaignStatus.ACTIVE);
  }

  @Override
  public void deactivateCampaign(TAccountRequest accountRequest, UUID id) {
    updateStatus(id, CampaignStatus.PAUSED);
  }

  private void updateStatus(UUID id, CampaignStatus status) {
    ECampaign campaign = campaignRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    campaign.setStatus(status);
    campaignRepository.save(campaign);
  }

  @Override
  public PagedResponse<CampaignVm> getCampaigns(TAccountRequest accountRequest, SearchCampaignRequest request,
      Pageable pageable) {
    Specification<ECampaign> spec = CampaignSpecification.withFilter(request);
    Page<ECampaign> page = campaignRepository.findAll(spec, pageable);

    List<CampaignVm> vms = page.getContent().stream().map(this::toVm).collect(Collectors.toList());
    return PagedResponse.<CampaignVm>builder()
        .maxPageItems(pageable.getPageSize())
        .page(page.getNumber())
        .size(page.getSize())
        .results(vms)
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .build();
  }

  @Override
  public List<CampaignVm> getCampaignsByDateRange(TAccountRequest accountRequest, Instant start, Instant end) {
    return campaignRepository.findByStartDateBetween(start, end).stream().map(this::toVm).collect(Collectors.toList());
  }

  @Override
  public CampaignStatisticsResponse getCampaignStatistics(TAccountRequest accountRequest, UUID campaignId) {
    ECampaign campaign = campaignRepository.findById(campaignId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    long totalPrograms = campaign.getPromotionPrograms() == null ? 0 : campaign.getPromotionPrograms().size();
    long totalDiscounts = campaign.getPromotionPrograms() == null ? 0
        : campaign.getPromotionPrograms().stream().flatMap(pp -> pp.getDiscounts().stream()).count();

    double budget = campaign.getBudget() == null ? 0.0 : campaign.getBudget();
    double spent = campaign.getSpentBudget() == null ? 0.0 : campaign.getSpentBudget();
    double remaining = budget - spent;

    return CampaignStatisticsResponse.builder()
        .totalPrograms(totalPrograms)
        .totalDiscounts(totalDiscounts)
        .budget(budget)
        .spentBudget(spent)
        .budgetRemaining(remaining)
        .build();
  }

  @Override
  @Transactional
  public void assignDiscountsToCampaign(TAccountRequest accountRequest, AssignDiscountsToCampaignRequest request) {
    ECampaign campaign = campaignRepository.findById(request.campaignId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    // For simplicity, we just update spentBudget or some logic; actual mapping should be through EPromotionProgram
    if (request.discountIds() == null || request.discountIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No discount ids provided");
    }

    long count = discountRepository.countByIdIn(request.discountIds());
    if (count != request.discountIds().size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some discounts not found");
    }
    // TODO: actual assignment logic between campaign and discounts via program entity
  }
} 
