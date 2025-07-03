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
import com.winnguyen1905.promotion.model.request.UpdateScheduleRequest;
import com.winnguyen1905.promotion.model.request.UpdateTargetAudienceRequest;
import com.winnguyen1905.promotion.model.request.AssignVendorsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.AssignProductsToCampaignRequest;
import com.winnguyen1905.promotion.model.request.ApplyCampaignRequest;
import com.winnguyen1905.promotion.model.response.CampaignStatisticsResponse;
import com.winnguyen1905.promotion.model.response.CampaignVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.persistance.entity.ECampaign;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignStatus;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProduct;
import com.winnguyen1905.promotion.persistance.repository.CampaignRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.persistance.repository.VendorPromotionParticipationRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProductRepository;
import com.winnguyen1905.promotion.persistance.repository.specification.CampaignSpecification;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final DiscountRepository discountRepository;
  private final PromotionProgramRepository promotionProgramRepository;
  private final VendorPromotionParticipationRepository vendorPromotionParticipationRepository;
  private final PromotionProductRepository promotionProductRepository;

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
  @Transactional
  public void createCampaign(TAccountRequest accountRequest, CreateCampaignRequest request) {
    // Validation
    validateCampaignRequest(request);
    validateUserPermissions(accountRequest, "CREATE_CAMPAIGN");

    // Business rules validation
    if (request.startDate().isAfter(request.endDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
    }

    if (request.startDate().isBefore(Instant.now().minusSeconds(3600))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be more than 1 hour in the past");
    }

    if (request.budget() != null && request.budget() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget must be positive");
    }

    // Check for campaign name uniqueness for the creator (using existing methods)
    Page<ECampaign> existingCampaigns = campaignRepository.findAllByCreatedBy(accountRequest.id(),
        Pageable.ofSize(1000)); // Get a large page to check all campaigns
    boolean nameExists = existingCampaigns.getContent().stream()
        .anyMatch(c -> c.getName().equalsIgnoreCase(request.name().trim()));

    if (nameExists) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign with this name already exists");
    }

    // Create campaign with initial validation
    ECampaign campaign = ECampaign.builder()
        .name(request.name().trim())
        .description(request.description() != null ? request.description().trim() : null)
        .campaignType(request.campaignType())
        .startDate(request.startDate())
        .endDate(request.endDate())
        .budget(request.budget())
        .spentBudget(0.0)
        .targetAudience(request.targetAudience())
        .createdBy(accountRequest.id())
        .status(CampaignStatus.DRAFT)
        .build();

    // Set appropriate status based on timing
    if (request.startDate().isBefore(Instant.now().plusSeconds(60))) {
      campaign.setStatus(CampaignStatus.SCHEDULED);
    }

    ECampaign savedCampaign = campaignRepository.save(campaign);

    // Create audit log entry
    createAuditLog(accountRequest.id(), "CAMPAIGN_CREATED", savedCampaign.getId(),
        "Campaign '" + savedCampaign.getName() + "' created");
  }

  @Override
  public CampaignVm getCampaignById(TAccountRequest accountRequest, UUID id) {
    validateInputParameters(id, "Campaign ID cannot be null");

    ECampaign campaign = campaignRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    // Security check - users can only view campaigns they created or have
    // permission to view
    if (!hasPermissionToViewCampaign(accountRequest, campaign)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this campaign");
    }

    // Update campaign status if needed based on current time
    updateCampaignStatusBasedOnTime(campaign);

    return toVm(campaign);
  }

  @Override
  @Transactional
  public void updateCampaign(TAccountRequest accountRequest, UUID id, UpdateCampaignRequest request) {
    validateInputParameters(id, "Campaign ID cannot be null");
    validateInputParameters(request, "Update request cannot be null");

    ECampaign campaign = campaignRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    // Security check
    if (!hasPermissionToModifyCampaign(accountRequest, campaign)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to modify this campaign");
    }

    // Optimistic locking check
    if (request.version() != null && !Long.valueOf(campaign.getVersion()).equals(request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign has been modified by another transaction");
    }

    // Business rule: Cannot modify active campaigns in certain ways
    if (campaign.getStatus() == CampaignStatus.ACTIVE) {
      validateActiveUpdateCampaignRestrictions(request);
    }

    // Store original values for audit
    String originalName = campaign.getName();
    Instant originalStartDate = campaign.getStartDate();
    Instant originalEndDate = campaign.getEndDate();

    // Validate and apply updates
    if (request.name() != null) {
      validateCampaignName(request.name(), accountRequest.id(), campaign.getId());
      campaign.setName(request.name().trim());
    }

    if (request.description() != null) {
      campaign.setDescription(request.description().trim());
    }

    if (request.campaignType() != null) {
      // Cannot change type of active campaigns
      if (campaign.getStatus() == CampaignStatus.ACTIVE) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Cannot change campaign type while campaign is active");
      }
      campaign.setCampaignType(request.campaignType());
    }

    if (request.startDate() != null || request.endDate() != null) {
      Instant newStartDate = request.startDate() != null ? request.startDate() : campaign.getStartDate();
      Instant newEndDate = request.endDate() != null ? request.endDate() : campaign.getEndDate();

      validateDateRange(newStartDate, newEndDate, campaign.getStatus());

      if (request.startDate() != null)
        campaign.setStartDate(request.startDate());
      if (request.endDate() != null)
        campaign.setEndDate(request.endDate());
    }

    if (request.status() != null) {
      validateStatusTransition(campaign.getStatus(), request.status());
      campaign.setStatus(request.status());
    }

    if (request.budget() != null) {
      validateBudgetUpdate(campaign, request.budget());
      campaign.setBudget(request.budget());
    }

    if (request.targetAudience() != null) {
      campaign.setTargetAudience(request.targetAudience());
    }

    ECampaign savedCampaign = campaignRepository.save(campaign);

    // Create detailed audit log
    createDetailedUpdateAuditLog(accountRequest.id(), savedCampaign, originalName,
        originalStartDate, originalEndDate, request);
  }

  @Override
  @Transactional
  public void deleteCampaign(TAccountRequest accountRequest, UUID id) {
    validateInputParameters(id, "Campaign ID cannot be null");

    ECampaign campaign = campaignRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    // Security check
    if (!hasPermissionToModifyCampaign(accountRequest, campaign)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to delete this campaign");
    }

    // Business rule: Cannot delete active campaigns
    if (campaign.getStatus() == CampaignStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot delete active campaign. Please pause or complete it first");
    }

    // Business rule: Cannot delete campaigns with spent budget
    if (campaign.getSpentBudget() != null && campaign.getSpentBudget() > 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot delete campaign with spent budget. Campaign has financial history");
    }

    // Check for dependent entities and handle cascade deletion
    handleCascadeDeletion(campaign);

    // Store campaign info for audit before deletion
    String campaignName = campaign.getName();
    CampaignStatus campaignStatus = campaign.getStatus();

    campaignRepository.deleteById(id);

    createAuditLog(accountRequest.id(), "CAMPAIGN_DELETED", id,
        "Campaign '" + campaignName + "' with status " + campaignStatus + " was deleted");
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
    validateInputParameters(request, "Assignment request cannot be null");
    validateInputParameters(request.campaignId(), "Campaign ID cannot be null");

    ECampaign campaign = campaignRepository.findById(request.campaignId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    // Security check
    if (!hasPermissionToModifyCampaign(accountRequest, campaign)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to modify this campaign");
    }

    // Note: Optimistic locking would require version field in
    // AssignDiscountsToCampaignRequest
    // Skipping optimistic locking check for discount assignment

    if (request.discountIds() == null || request.discountIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No discount ids provided");
    }

    // Validate all discounts exist and are active
    long count = discountRepository.countByIdIn(request.discountIds());
    if (count != request.discountIds().size()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some discounts not found");
    }

    // Verify discounts are valid for assignment
    List<EDiscount> discounts = discountRepository.findAllById(request.discountIds());
    validateDiscountsForCampaignAssignment(discounts, campaign);

    // Get or create promotion program for this campaign
    EPromotionProgram program = getOrCreatePromotionProgramForCampaign(campaign, accountRequest.id());

    // Assign each discount to the promotion program
    int assignedCount = 0;
    for (EDiscount discount : discounts) {
      if (discount.getProgram() == null) {
        discount.setProgram(program);
        discountRepository.save(discount);
        assignedCount++;
      } else if (!discount.getProgram().getId().equals(program.getId())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Discount '" + discount.getName() + "' is already assigned to another program");
      }
    }

    createAuditLog(accountRequest.id(), "DISCOUNTS_ASSIGNED_TO_CAMPAIGN", campaign.getId(),
        assignedCount + " discounts assigned to campaign '" + campaign.getName() + "'");
  }

  @Override
  public void assignVendorsToCampaign(TAccountRequest accountRequest, AssignVendorsToCampaignRequest request) {
    if (request.vendorIds() == null || request.vendorIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No vendor ids provided");
    }

    ECampaign campaign = campaignRepository.findById(request.campaignId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    if (request.version() != null && !Long.valueOf(campaign.getVersion()).equals(request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign has been modified by another transaction");
    }

    // Retrieve or create a default promotion program for this campaign
    EPromotionProgram program;
    if (campaign.getPromotionPrograms() == null || campaign.getPromotionPrograms().isEmpty()) {
      program = EPromotionProgram.builder()
          .campaign(campaign)
          .name(campaign.getName() + " Program")
          .description("Auto generated program for vendor assignment")
          .programType(EPromotionProgram.ProgramType.DISCOUNT)
          .startDate(campaign.getStartDate())
          .endDate(campaign.getEndDate())
          .createdBy(accountRequest.id())
          .build();
      promotionProgramRepository.save(program);
      campaign.getPromotionPrograms().add(program);
    } else {
      program = campaign.getPromotionPrograms().get(0);
    }

    // Assign each vendor
    for (UUID vendorId : request.vendorIds()) {
      if (vendorPromotionParticipationRepository.countByProgramIdAndVendorId(program.getId(), vendorId) > 0)
        continue; // already assigned

      EVendorPromotionParticipation participation = EVendorPromotionParticipation.builder()
          .program(program)
          .vendorId(vendorId)
          .vendorContributionRate(0.0)
          .expectedDiscountRate(0.0)
          .build();
      vendorPromotionParticipationRepository.save(participation);
    }
  }

  @Override
  public void assignProductsToCampaign(TAccountRequest accountRequest, AssignProductsToCampaignRequest request) {
    if (request.productIds() == null || request.productIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No product ids provided");
    }

    ECampaign campaign = campaignRepository.findById(request.campaignId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    if (request.version() != null && !Long.valueOf(campaign.getVersion()).equals(request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign has been modified by another transaction");
    }

    // Use first program (assume exists)
    if (campaign.getPromotionPrograms() == null || campaign.getPromotionPrograms().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign has no promotion program to assign products");
    }
    EPromotionProgram program = campaign.getPromotionPrograms().get(0);

    for (UUID productId : request.productIds()) {
      if (promotionProductRepository.countByProgramIdAndProductId(program.getId(), productId) > 0)
        continue; // already linked

      EPromotionProduct promotionProduct = EPromotionProduct.builder()
          .program(program)
          .productId(productId)
          .vendorId(program.getCreatedBy()) // simplistic
          .originalPrice(0.0)
          .promotionPrice(0.0)
          .discountAmount(0.0)
          .discountPercentage(0.0)
          .build();
      promotionProductRepository.save(promotionProduct);
    }
  }

  @Override
  public void updateCampaignSchedule(TAccountRequest accountRequest, UUID campaignId, UpdateScheduleRequest request) {
    ECampaign campaign = campaignRepository.findById(campaignId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

    if (!Long.valueOf(campaign.getVersion()).equals(request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign has been modified by another transaction");
    }
    campaign.setStartDate(request.startDate());
    campaign.setEndDate(request.endDate());
    campaignRepository.save(campaign);
  }

  @Override
  public void updateTargetAudience(TAccountRequest accountRequest, UUID campaignId,
      UpdateTargetAudienceRequest request) {
    ECampaign campaign = campaignRepository.findById(campaignId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    if (!Long.valueOf(campaign.getVersion()).equals(request.version())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign has been modified by another transaction");
    }
    campaign.setTargetAudience(request.targetAudience());
    campaignRepository.save(campaign);
  }

  @Override
  public com.winnguyen1905.promotion.model.response.PerformanceMetricsResponse getPerformanceMetrics(
      TAccountRequest accountRequest, UUID campaignId) {
    // TODO: gather metrics from analytics table; return mock for now
    return com.winnguyen1905.promotion.model.response.PerformanceMetricsResponse.builder()
        .impressions(0)
        .clicks(0)
        .redemptions(0)
        .totalDiscountGiven(0)
        .revenueLift(0)
        .conversionRate(0)
        .build();
  }

  @Override
  public PagedResponse<CampaignVm> getActiveCampaigns(TAccountRequest accountRequest, SearchCampaignRequest request,
      Pageable pageable) {
    // Force status ACTIVE and date range now
    SearchCampaignRequest activeReq = SearchCampaignRequest.builder()
        .name(request == null ? null : request.name())
        .campaignType(request == null ? null : request.campaignType())
        .createdBy(request == null ? null : request.createdBy())
        .status(ECampaign.CampaignStatus.ACTIVE)
        .includeExpired(false)
        .build();
    return getCampaigns(accountRequest, activeReq, pageable);
  }

  @Override
  public com.winnguyen1905.promotion.model.response.ApplyDiscountResponse applyCampaign(TAccountRequest accountRequest,
      ApplyCampaignRequest request) {
    // TODO: integrate discounts and price statistics similarly to
    // DiscountServiceImpl
    throw new UnsupportedOperationException("applyCampaign not yet implemented");
  }

  // Private helper methods
  private void validateCampaignRequest(CreateCampaignRequest request) {
    if (request.name() == null || request.name().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name is required");
    }
    if (request.name().length() > 100) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name must not exceed 100 characters");
    }
    if (request.campaignType() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign type is required");
    }
    if (request.startDate() == null || request.endDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date and end date are required");
    }
  }

  private void validateUserPermissions(TAccountRequest accountRequest, String permission) {
    // TODO: Implement actual permission checking logic
    // For now, just basic validation
    if (accountRequest == null || accountRequest.id() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid user authentication required");
    }
  }

  private void validateInputParameters(Object parameter, String errorMessage) {
    if (parameter == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
    }
  }

  private boolean hasPermissionToViewCampaign(TAccountRequest accountRequest, ECampaign campaign) {
    // Users can view campaigns they created or if they have admin role
    return campaign.getCreatedBy().equals(accountRequest.id()) ||
        hasAdminRole(accountRequest);
  }

  private boolean hasAdminRole(TAccountRequest accountRequest) {
    // TODO: Implement role checking logic
    return true; // Temporary - allow all for now
  }

  private void updateCampaignStatusBasedOnTime(ECampaign campaign) {
    Instant now = Instant.now();

    if (campaign.getStatus() == CampaignStatus.SCHEDULED &&
        campaign.getStartDate().isBefore(now)) {
      campaign.setStatus(CampaignStatus.ACTIVE);
      campaignRepository.save(campaign);
    } else if (campaign.getStatus() == CampaignStatus.ACTIVE &&
        campaign.getEndDate().isBefore(now)) {
      campaign.setStatus(CampaignStatus.COMPLETED);
      campaignRepository.save(campaign);
    }
  }

  private void createAuditLog(UUID userId, String action, UUID entityId, String description) {
    // TODO: Implement audit logging
    // For now, just log to console or use a logger
    System.out.println("AUDIT: User " + userId + " performed " + action + " on " + entityId + ": " + description);
  }

  private boolean hasPermissionToModifyCampaign(TAccountRequest accountRequest, ECampaign campaign) {
    return campaign.getCreatedBy().equals(accountRequest.id()) || hasAdminRole(accountRequest);
  }

  private void validateActiveUpdateCampaignRestrictions(UpdateCampaignRequest request) {
    // Active campaigns have restrictions on what can be modified
    if (request.startDate() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot modify start date of active campaign");
    }
  }

  private void validateCampaignName(String newName, UUID userId, UUID campaignId) {
    if (newName == null || newName.trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name cannot be empty");
    }
    if (newName.length() > 100) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name must not exceed 100 characters");
    }

    // Check for name uniqueness (excluding current campaign)
    Page<ECampaign> existingCampaigns = campaignRepository.findAllByCreatedBy(userId, Pageable.ofSize(1000));
    boolean nameExists = existingCampaigns.getContent().stream()
        .anyMatch(c -> !c.getId().equals(campaignId) && c.getName().equalsIgnoreCase(newName.trim()));

    if (nameExists) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Campaign with this name already exists");
    }
  }

  private void validateDateRange(Instant startDate, Instant endDate, CampaignStatus currentStatus) {
    if (startDate.isAfter(endDate)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
    }

    // Cannot set start date in the past for non-active campaigns
    if (currentStatus != CampaignStatus.ACTIVE && startDate.isBefore(Instant.now().minusSeconds(3600))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Start date cannot be more than 1 hour in the past");
    }
  }

  private void validateStatusTransition(CampaignStatus currentStatus, CampaignStatus newStatus) {
    // Define valid status transitions
    boolean isValidTransition = switch (currentStatus) {
      case DRAFT -> newStatus == CampaignStatus.SCHEDULED || newStatus == CampaignStatus.ACTIVE ||
          newStatus == CampaignStatus.CANCELLED;
      case SCHEDULED -> newStatus == CampaignStatus.ACTIVE || newStatus == CampaignStatus.CANCELLED;
      case ACTIVE -> newStatus == CampaignStatus.PAUSED || newStatus == CampaignStatus.COMPLETED ||
          newStatus == CampaignStatus.CANCELLED;
      case PAUSED -> newStatus == CampaignStatus.ACTIVE || newStatus == CampaignStatus.CANCELLED;
      case COMPLETED, CANCELLED -> false; // Terminal states
    };

    if (!isValidTransition) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid status transition from " + currentStatus + " to " + newStatus);
    }
  }

  private void validateBudgetUpdate(ECampaign campaign, Double newBudget) {
    if (newBudget <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Budget must be positive");
    }

    // Cannot reduce budget below spent amount
    if (campaign.getSpentBudget() != null && newBudget < campaign.getSpentBudget()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Budget cannot be less than already spent amount: " + campaign.getSpentBudget());
    }
  }

  private void createDetailedUpdateAuditLog(UUID userId, ECampaign campaign, String originalName,
      Instant originalStartDate, Instant originalEndDate, UpdateCampaignRequest request) {
    StringBuilder changes = new StringBuilder();

    if (request.name() != null && !originalName.equals(campaign.getName())) {
      changes.append("Name: '").append(originalName).append("' -> '").append(campaign.getName()).append("'; ");
    }
    if (request.startDate() != null && !originalStartDate.equals(campaign.getStartDate())) {
      changes.append("Start Date: ").append(originalStartDate).append(" -> ").append(campaign.getStartDate())
          .append("; ");
    }
    if (request.endDate() != null && !originalEndDate.equals(campaign.getEndDate())) {
      changes.append("End Date: ").append(originalEndDate).append(" -> ").append(campaign.getEndDate()).append("; ");
    }

    createAuditLog(userId, "CAMPAIGN_UPDATED", campaign.getId(),
        "Campaign updated: " + changes.toString());
  }

  private void handleCascadeDeletion(ECampaign campaign) {
    // Check and handle deletion of related promotion programs
    if (campaign.getPromotionPrograms() != null && !campaign.getPromotionPrograms().isEmpty()) {
      for (EPromotionProgram program : campaign.getPromotionPrograms()) {
        // Note: The following methods would need to be added to the respective
        // repositories:
        // - VendorPromotionParticipationRepository.findAllByProgramId(UUID programId)
        // - PromotionProductRepository.findAllByProgramId(UUID programId) (this one
        // exists)

        // Remove promotion products
        List<EPromotionProduct> promotionProducts = promotionProductRepository.findAllByProgramId(program.getId());
        if (!promotionProducts.isEmpty()) {
          promotionProductRepository.deleteAll(promotionProducts);
        }

        // For vendor participations, we'd need to add the repository method or use JPA
        // cascade
        // TODO: Add findAllByProgramId method to VendorPromotionParticipationRepository
        // For now, rely on JPA cascade deletion or handle manually when the method is
        // added

        // Note: Discounts and other related entities should be handled by JPA cascade
        // settings
        // The entities are configured with proper cascade relationships
      }
    }
  }

  private void validateDiscountsForCampaignAssignment(List<EDiscount> discounts, ECampaign campaign) {
    Instant now = Instant.now();

    for (EDiscount discount : discounts) {
      // Check if discount is active
      if (!discount.getIsActive()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Discount '" + discount.getName() + "' is not active");
      }

      // Check if discount is within valid date range
      if (discount.getStartDate().isAfter(now) || discount.getEndDate().isBefore(now)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Discount '" + discount.getName() + "' is not currently valid (check dates)");
      }

      // Check if discount dates align with campaign dates
      if (discount.getStartDate().isBefore(campaign.getStartDate()) ||
          discount.getEndDate().isAfter(campaign.getEndDate())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Discount '" + discount.getName() + "' date range must be within campaign date range");
      }

      // Check if discount has reached usage limit
      if (discount.getUsageLimitTotal() != null &&
          discount.getUsageCount() >= discount.getUsageLimitTotal()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Discount '" + discount.getName() + "' has reached its usage limit");
      }
    }
  }

  private EPromotionProgram getOrCreatePromotionProgramForCampaign(ECampaign campaign, UUID userId) {
    // Check if campaign already has a promotion program
    if (campaign.getPromotionPrograms() != null && !campaign.getPromotionPrograms().isEmpty()) {
      return campaign.getPromotionPrograms().get(0); // Return first program
    }

    // Create a new promotion program for this campaign
    EPromotionProgram program = EPromotionProgram.builder()
        .campaign(campaign)
        .name(campaign.getName() + " - Default Program")
        .description("Auto-generated program for campaign: " + campaign.getName())
        .programType(EPromotionProgram.ProgramType.DISCOUNT)
        .startDate(campaign.getStartDate())
        .endDate(campaign.getEndDate())
        .priority(1)
        .isStackable(false)
        .platformCommissionRate(0.05) // Default 5%
        .requiredVendorContribution(0.0)
        .visibility(EPromotionProgram.Visibility.PUBLIC)
        .autoApply(false)
        .status(EPromotionProgram.ProgramStatus.ACTIVE)
        .createdBy(userId)
        .build();

    EPromotionProgram savedProgram = promotionProgramRepository.save(program);

    // Add to campaign's program list
    if (campaign.getPromotionPrograms() == null) {
      campaign.setPromotionPrograms(List.of(savedProgram));
    } else {
      campaign.getPromotionPrograms().add(savedProgram);
    }

    return savedProgram;
  }
}
