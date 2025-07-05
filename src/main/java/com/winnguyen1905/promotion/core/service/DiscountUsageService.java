package com.winnguyen1905.promotion.core.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.request.CreateDiscountUsageRequest;
import com.winnguyen1905.promotion.model.response.DiscountUsageVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface DiscountUsageService {

  void recordDiscountUsage(TAccountRequest accountRequest, CreateDiscountUsageRequest request);

  DiscountUsageVm getDiscountUsageById(TAccountRequest accountRequest, UUID id);

  PagedResponse<DiscountUsageVm> getCustomerDiscountUsage(TAccountRequest accountRequest,
      UUID customerId,
      Pageable pageable);

  PagedResponse<DiscountUsageVm> getDiscountUsageHistory(TAccountRequest accountRequest,
      UUID discountId,
      Pageable pageable);

  PagedResponse<DiscountUsageVm> getProgramUsageHistory(TAccountRequest accountRequest,
      UUID programId,
      Pageable pageable);

  Integer getCustomerDiscountUsageCount(UUID customerId, UUID discountId);

  boolean canCustomerUseDiscount(UUID customerId, UUID discountId);

  void refundDiscountUsage(TAccountRequest accountRequest, UUID usageId);
}
