package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.winnguyen1905.promotion.core.service.VendorPromotionParticipationService;
import com.winnguyen1905.promotion.model.request.VendorParticipationRequest;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.VendorParticipationVm;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.Status;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.persistance.repository.VendorPromotionParticipationRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorPromotionParticipationServiceImpl implements VendorPromotionParticipationService {

  private final VendorPromotionParticipationRepository participationRepository;
  private final PromotionProgramRepository programRepository;

  private VendorParticipationVm toVm(EVendorPromotionParticipation entity) {
    if (entity == null)
      return null;
    return VendorParticipationVm.builder()
        .id(entity.getId())
        .vendorId(entity.getVendorId())
        .programId(entity.getProgram() != null ? entity.getProgram().getId() : null)
        .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
        .participationType(entity.getParticipationType())
        .status(entity.getStatus())
        .vendorContributionRate(entity.getVendorContributionRate())
        .expectedDiscountRate(entity.getExpectedDiscountRate())
        .minDiscountAmount(entity.getMinDiscountAmount())
        .maxDiscountAmount(entity.getMaxDiscountAmount())
        .productSelection(entity.getProductSelection())
        .acceptedTerms(entity.getAcceptedTerms())
        .joinedAt(entity.getJoinedAt())
        .approvedAt(entity.getApprovedAt())
        .approvedBy(entity.getApprovedBy())
        .withdrawalReason(entity.getWithdrawalReason())
        .performanceMetrics(entity.getPerformanceMetrics())
        .build();
  }

  @Override
  @Transactional
  public void requestParticipation(TAccountRequest accountRequest, VendorParticipationRequest request) {
    log.info("Vendor {} requesting participation in program {}", request.vendorId(), request.programId());

    // Validate program exists
    EPromotionProgram program = programRepository.findById(request.programId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion program not found"));

    // Check if participation already exists
    long existingCount = participationRepository.countByProgramIdAndVendorId(request.programId(), request.vendorId());
    if (existingCount > 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Vendor already has participation request for this program");
    }

    // Validate request data
    validateParticipationRequest(request);

    // Create participation
    EVendorPromotionParticipation participation = EVendorPromotionParticipation.builder()
        .vendorId(request.vendorId())
        .program(program)
        .participationType(request.participationType())
        .vendorContributionRate(request.vendorContributionRate())
        .expectedDiscountRate(request.expectedDiscountRate())
        .minDiscountAmount(request.minDiscountAmount())
        .maxDiscountAmount(request.maxDiscountAmount())
        .productSelection(request.productSelection())
        .acceptedTerms(request.acceptedTerms())
        .status(Status.PENDING)
        .build();

    participationRepository.save(participation);
    log.info("Participation request created with ID: {}", participation.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public VendorParticipationVm getParticipationById(TAccountRequest accountRequest, UUID id) {
    EVendorPromotionParticipation participation = participationRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

    // Security check - vendors can only view their own participations, admins can
    // view all
    validateParticipationAccess(accountRequest, participation);

    return toVm(participation);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<VendorParticipationVm> getVendorParticipations(TAccountRequest accountRequest, UUID vendorId,
      Pageable pageable) {

    // Security check - vendors can only view their own participations
    if (!accountRequest.id().equals(vendorId) && !hasAdminRole(accountRequest)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to view vendor participations");
    }

    List<EVendorPromotionParticipation> participations = participationRepository.findAllByVendorId(vendorId);
    return mapListToPagedResponse(participations, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<VendorParticipationVm> getProgramParticipations(TAccountRequest accountRequest, UUID programId,
      Pageable pageable) {

    // Only admins can view all program participations
    validateAdminAccess(accountRequest);

    // Simple implementation - would need proper repository method
    List<EVendorPromotionParticipation> allParticipations = participationRepository.findAll();
    List<EVendorPromotionParticipation> programParticipations = allParticipations.stream()
        .filter(p -> p.getProgram() != null && p.getProgram().getId().equals(programId))
        .collect(Collectors.toList());

    return mapListToPagedResponse(programParticipations, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<VendorParticipationVm> getParticipationsByStatus(TAccountRequest accountRequest, Status status,
      Pageable pageable) {

    // Only admins can view participations by status
    validateAdminAccess(accountRequest);

    List<EVendorPromotionParticipation> allParticipations = participationRepository.findAll();
    List<EVendorPromotionParticipation> statusParticipations = allParticipations.stream()
        .filter(p -> p.getStatus() == status)
        .collect(Collectors.toList());

    return mapListToPagedResponse(statusParticipations, pageable);
  }

  @Override
  @Transactional
  public void updateParticipationStatus(TAccountRequest accountRequest, UUID id, Status status, String reason) {
    validateAdminAccess(accountRequest);

    EVendorPromotionParticipation participation = participationRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

    validateStatusTransition(participation.getStatus(), status);

    participation.setStatus(status);
    if (reason != null) {
      participation.setWithdrawalReason(reason);
    }

    if (status == Status.APPROVED) {
      participation.setApprovedAt(Instant.now());
      participation.setApprovedBy(accountRequest.id());
    }

    participationRepository.save(participation);
    log.info("Participation {} status updated to {} by {}", id, status, accountRequest.id());
  }

  @Override
  @Transactional
  public void approveParticipation(TAccountRequest accountRequest, UUID id) {
    updateParticipationStatus(accountRequest, id, Status.APPROVED, null);
  }

  @Override
  @Transactional
  public void rejectParticipation(TAccountRequest accountRequest, UUID id, String reason) {
    if (reason == null || reason.trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
    }
    updateParticipationStatus(accountRequest, id, Status.REJECTED, reason);
  }

  @Override
  @Transactional
  public void withdrawParticipation(TAccountRequest accountRequest, UUID id, String reason) {
    EVendorPromotionParticipation participation = participationRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

    // Only the vendor can withdraw their own participation
    if (!participation.getVendorId().equals(accountRequest.id()) && !hasAdminRole(accountRequest)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to withdraw this participation");
    }

    if (participation.getStatus() == Status.WITHDRAWN) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Participation already withdrawn");
    }

    participation.setStatus(Status.WITHDRAWN);
    participation.setWithdrawalReason(reason);
    participationRepository.save(participation);

    log.info("Participation {} withdrawn by vendor {}", id, accountRequest.id());
  }

  @Override
  @Transactional
  public void updateParticipation(TAccountRequest accountRequest, UUID id, VendorParticipationRequest request) {
    EVendorPromotionParticipation participation = participationRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

    // Only the vendor can update their own participation
    if (!participation.getVendorId().equals(accountRequest.id())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to update this participation");
    }

    // Only allow updates if status is PENDING
    if (participation.getStatus() != Status.PENDING) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Can only update participation with PENDING status");
    }

    validateParticipationRequest(request);

    // Update allowed fields
    participation.setVendorContributionRate(request.vendorContributionRate());
    participation.setExpectedDiscountRate(request.expectedDiscountRate());
    participation.setMinDiscountAmount(request.minDiscountAmount());
    participation.setMaxDiscountAmount(request.maxDiscountAmount());
    participation.setProductSelection(request.productSelection());
    participation.setAcceptedTerms(request.acceptedTerms());

    participationRepository.save(participation);
    log.info("Participation {} updated by vendor {}", id, accountRequest.id());
  }

  @Override
  @Transactional
  public void calculatePerformanceMetrics(UUID participationId) {
    EVendorPromotionParticipation participation = participationRepository.findById(participationId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

    // Simple implementation - in real scenario would calculate from actual data
    // This would integrate with analytics services to calculate:
    // - Total orders influenced by this participation
    // - Revenue generated
    // - Discount amounts provided
    // - Customer acquisition metrics
    // - ROI calculations

    // For now, just update timestamp to indicate metrics were calculated
    participation.setPerformanceMetrics(null); // Would set actual metrics JSON
    participationRepository.save(participation);

    log.info("Performance metrics calculated for participation {}", participationId);
  }

  @Override
  @Transactional
  public void deleteParticipation(TAccountRequest accountRequest, UUID id) {
    validateAdminAccess(accountRequest);

    EVendorPromotionParticipation participation = participationRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participation not found"));

    // Only allow deletion of non-active participations
    if (participation.getStatus() == Status.APPROVED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot delete active participation. Withdraw it first.");
    }

    participationRepository.delete(participation);
    log.info("Participation {} deleted by admin {}", id, accountRequest.id());
  }

  // Private helper methods
  private void validateParticipationRequest(VendorParticipationRequest request) {
    if (request.vendorContributionRate() < 0 || request.vendorContributionRate() > 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Vendor contribution rate must be between 0 and 1");
    }

    if (request.expectedDiscountRate() < 0 || request.expectedDiscountRate() > 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Expected discount rate must be between 0 and 1");
    }

    if (request.minDiscountAmount() != null && request.maxDiscountAmount() != null &&
        request.minDiscountAmount() > request.maxDiscountAmount()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Minimum discount amount cannot be greater than maximum");
    }

    if (!request.acceptedTerms()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Terms and conditions must be accepted");
    }
  }

  private void validateParticipationAccess(TAccountRequest accountRequest,
      EVendorPromotionParticipation participation) {
    if (!participation.getVendorId().equals(accountRequest.id()) && !hasAdminRole(accountRequest)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to view this participation");
    }
  }

  private void validateAdminAccess(TAccountRequest accountRequest) {
    if (!hasAdminRole(accountRequest)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }
  }

  private boolean hasAdminRole(TAccountRequest accountRequest) {
    // TODO: Implement actual role checking logic
    // For now, assume all users have admin access
    return true;
  }

  private void validateStatusTransition(Status currentStatus, Status newStatus) {
    boolean isValidTransition = switch (currentStatus) {
      case PENDING -> newStatus == Status.APPROVED || newStatus == Status.REJECTED;
      case APPROVED -> newStatus == Status.SUSPENDED || newStatus == Status.WITHDRAWN;
      case REJECTED -> false; // Terminal state
      case SUSPENDED -> newStatus == Status.APPROVED || newStatus == Status.WITHDRAWN;
      case WITHDRAWN -> false; // Terminal state
    };

    if (!isValidTransition) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid status transition from " + currentStatus + " to " + newStatus);
    }
  }

  private PagedResponse<VendorParticipationVm> mapListToPagedResponse(List<EVendorPromotionParticipation> list,
      Pageable pageable) {
    int page = pageable.getPageNumber();
    int size = pageable.getPageSize();
    int fromIndex = Math.min(page * size, list.size());
    int toIndex = Math.min(fromIndex + size, list.size());

    List<VendorParticipationVm> slice = list.subList(fromIndex, toIndex).stream()
        .map(this::toVm)
        .collect(Collectors.toList());

    long total = list.size();
    long totalPages = size == 0 ? 1 : (long) Math.ceil((double) total / size);

    return PagedResponse.<VendorParticipationVm>builder()
        .results(slice)
        .page(page)
        .size(size)
        .totalElements(total)
        .totalPages(totalPages)
        .maxPageItems(size)
        .build();
  }
}
