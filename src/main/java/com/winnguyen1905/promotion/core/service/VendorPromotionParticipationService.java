package com.winnguyen1905.promotion.core.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.request.VendorParticipationRequest;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.VendorParticipationVm;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.Status;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface VendorPromotionParticipationService {

  void requestParticipation(TAccountRequest accountRequest, VendorParticipationRequest request);

  VendorParticipationVm getParticipationById(TAccountRequest accountRequest, UUID id);

  PagedResponse<VendorParticipationVm> getVendorParticipations(TAccountRequest accountRequest,
      UUID vendorId,
      Pageable pageable);

  PagedResponse<VendorParticipationVm> getProgramParticipations(TAccountRequest accountRequest,
      UUID programId,
      Pageable pageable);

  PagedResponse<VendorParticipationVm> getParticipationsByStatus(TAccountRequest accountRequest,
      Status status,
      Pageable pageable);

  void updateParticipationStatus(TAccountRequest accountRequest, UUID id, Status status, String reason);

  void approveParticipation(TAccountRequest accountRequest, UUID id);

  void rejectParticipation(TAccountRequest accountRequest, UUID id, String reason);

  void withdrawParticipation(TAccountRequest accountRequest, UUID id, String reason);

  void updateParticipation(TAccountRequest accountRequest, UUID id, VendorParticipationRequest request);

  void deleteParticipation(TAccountRequest accountRequest, UUID id);

  void calculatePerformanceMetrics(UUID participationId);
}
