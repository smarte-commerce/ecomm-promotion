package com.winnguyen1905.promotion.core.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PromotionCommissionVm;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission.PaymentStatus;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface PromotionCommissionService {

  void createCommission(UUID programId, UUID vendorId, UUID orderId, UUID customerId,
      Double orderAmount, Double discountAmount);

  PromotionCommissionVm getCommissionById(TAccountRequest accountRequest, UUID id);

  PagedResponse<PromotionCommissionVm> getVendorCommissions(TAccountRequest accountRequest,
      UUID vendorId,
      Pageable pageable);

  PagedResponse<PromotionCommissionVm> getProgramCommissions(TAccountRequest accountRequest,
      UUID programId,
      Pageable pageable);

  PagedResponse<PromotionCommissionVm> getCommissionsByStatus(TAccountRequest accountRequest,
      PaymentStatus status,
      Pageable pageable);

  void updatePaymentStatus(TAccountRequest accountRequest, UUID id, PaymentStatus status, String transactionId);

  void processCommissionPayment(TAccountRequest accountRequest, UUID id);

  void processVendorCommissionPayments(UUID vendorId);

  void processPendingCommissions();

  Double getTotalVendorCommission(UUID vendorId, PaymentStatus status);

  Double getTotalProgramCommission(UUID programId);
}
