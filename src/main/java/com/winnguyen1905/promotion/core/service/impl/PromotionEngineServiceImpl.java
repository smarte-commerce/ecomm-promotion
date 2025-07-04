package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.promotion.core.service.DiscountService;
import com.winnguyen1905.promotion.core.service.DiscountUsageService;
import com.winnguyen1905.promotion.core.service.PromotionAnalyticsService;
import com.winnguyen1905.promotion.core.service.PromotionCommissionService;
import com.winnguyen1905.promotion.core.service.PromotionEngineService;
import com.winnguyen1905.promotion.exception.BadRequestException;
import com.winnguyen1905.promotion.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.model.request.CreateDiscountUsageRequest;
import com.winnguyen1905.promotion.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EFlashSale;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramStatus;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.FlashSaleRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionEngineServiceImpl implements PromotionEngineService {

  private final PromotionProgramRepository programRepository;
  private final DiscountRepository discountRepository;
  private final FlashSaleRepository flashSaleRepository;
  private final DiscountService discountService;
  private final DiscountUsageService discountUsageService;
  private final PromotionCommissionService commissionService;
  private final PromotionAnalyticsService analyticsService;

  @Override
  public ApplyDiscountResponse applyBestPromotions(TAccountRequest accountRequest, ApplyDiscountRequest request) {
    log.info("Applying best promotions for customer: {}", accountRequest.id());

    // Delegate to the existing DiscountService which has the optimized logic
    return discountService.applyDiscountToCart(accountRequest, request);
  }

  @Override
  public void processOrderCommissions(UUID orderId, UUID customerId, Double orderAmount, List<UUID> vendorIds) {
    log.info("Processing commissions for order: {}", orderId);

    // Find active programs that apply to this order
    List<EPromotionProgram> activePrograms = programRepository.findActivePrograms(Instant.now());

    for (EPromotionProgram program : activePrograms) {
      for (UUID vendorId : vendorIds) {
        // Calculate commission based on program rules
        calculateAndDistributeCommission(program.getId(), vendorId, orderId, customerId, orderAmount, 0.0);
      }
    }
  }

  @Override
  public boolean validatePromotionEligibility(UUID customerId, UUID programId, ApplyDiscountRequest request) {
    EPromotionProgram program = programRepository.findById(programId).orElse(null);
    if (program == null || program.getStatus() != ProgramStatus.ACTIVE) {
      return false;
    }

    // Check date validity
    Instant now = Instant.now();
    if (now.isBefore(program.getStartDate()) || now.isAfter(program.getEndDate())) {
      return false;
    }

    // Use existing discount service for usage limit validation
    return checkUsageLimits(customerId, programId, null);
  }

  @Override
  public Double calculatePromotionDiscount(UUID programId, ApplyDiscountRequest request) {
    EPromotionProgram program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return 0.0;
    }

    // Get associated discounts and use existing discount service calculation
    List<EDiscount> discounts = program.getDiscounts();
    Double maxDiscount = 0.0;

    for (EDiscount discount : discounts) {
      if (discount.getIsActive() &&
          discount.getStartDate().isBefore(Instant.now()) &&
          discount.getEndDate().isAfter(Instant.now())) {

        // Delegate to discount service for accurate calculation
        try {
          ApplyDiscountResponse response = discountService.applyDiscountToCart(null,
              ApplyDiscountRequest.builder()
                  .discountId(discount.getId())
                  .shopId(request.shopId())
                  .customerId(request.customerId())
                  .customerCartWithShop(request.customerCartWithShop())
                  .build());

          if (response.priceStatisticsResponse() != null) {
            Double discountAmount = response.priceStatisticsResponse().amountProductReduced() +
                response.priceStatisticsResponse().amountShipReduced();
            maxDiscount = Math.max(maxDiscount, discountAmount);
          }
        } catch (Exception e) {
          log.warn("Failed to calculate discount for discount ID: {}", discount.getId(), e);
        }
      }
    }

    return maxDiscount;
  }

  @Override
  public List<UUID> findApplicablePromotions(TAccountRequest accountRequest, ApplyDiscountRequest request) {
    List<EPromotionProgram> activePrograms = programRepository.findActivePrograms(Instant.now());

    return activePrograms.stream()
        .filter(program -> program.getVisibility() == EPromotionProgram.Visibility.PUBLIC)
        .map(EPromotionProgram::getId)
        .collect(Collectors.toList());
  }

  @Override
  public void recordPromotionUsage(UUID customerId, UUID programId, UUID discountId, UUID orderId,
      Double discountAmount) {
    CreateDiscountUsageRequest usageRequest = new CreateDiscountUsageRequest(
        customerId, programId, discountId, orderId, 1, discountAmount, 0.0, 0, null);

    discountUsageService.recordDiscountUsage(null, usageRequest);
  }

  @Override
  public void updatePromotionAnalytics(UUID programId, UUID customerId, Double orderAmount, Double discountAmount) {
    analyticsService.updateAnalyticsFromOrder(null, programId, orderAmount, discountAmount);
  }

  @Override
  public boolean checkUsageLimits(UUID customerId, UUID programId, UUID discountId) {
    if (discountId != null) {
      Integer usageCount = discountUsageService.getCustomerDiscountUsageCount(customerId, discountId);
      EDiscount discount = discountRepository.findById(discountId).orElse(null);

      if (discount != null && discount.getUsageLimitPerCustomer() != null) {
        return usageCount < discount.getUsageLimitPerCustomer();
      }
    }

    // Check program-level limits
    EPromotionProgram program = programRepository.findById(programId).orElse(null);
    if (program != null && program.getUsageLimitGlobal() != null) {
      return program.getUsageCountGlobal() < program.getUsageLimitGlobal();
    }

    return true;
  }

  @Override
  public void calculateAndDistributeCommission(UUID programId, UUID vendorId, UUID orderId,
      UUID customerId, Double orderAmount, Double discountAmount) {
    EPromotionProgram program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return;
    }

    // Calculate commission based on program settings
    Double commissionRate = program.getPlatformCommissionRate() != null ? program.getPlatformCommissionRate() : 0.0;

    if (commissionRate > 0) {
      commissionService.createCommission(programId, vendorId, orderId, customerId, orderAmount, discountAmount);
    }
  }

  @Override
  public boolean processFlashSalePurchase(UUID flashSaleId, UUID customerId, Integer quantity) {
    EFlashSale flashSale = flashSaleRepository.findById(flashSaleId).orElse(null);
    if (flashSale == null) {
      throw new BadRequestException("Flash sale not found");
    }

    // Check if flash sale is active
    if (flashSale.getStatus() != EFlashSale.Status.LIVE) {
      throw new BadRequestException("Flash sale is not active");
    }

    // Check quantity availability
    Integer availableQuantity = flashSale.getMaxQuantity() - flashSale.getSoldQuantity();
    if (quantity > availableQuantity) {
      throw new BadRequestException("Insufficient quantity available");
    }

    // Update sold quantity
    int updated = flashSaleRepository.updateSoldQuantity(flashSaleId, quantity);
    return updated > 0;
  }

  @Override
  public boolean validatePromotionStacking(List<UUID> programIds) {
    List<EPromotionProgram> programs = programRepository.findAllById(programIds);

    // Check if any non-stackable programs are included
    boolean hasNonStackable = programs.stream()
        .anyMatch(program -> !program.getIsStackable());

    if (hasNonStackable && programs.size() > 1) {
      return false;
    }

    return true;
  }

  @Override
  public List<UUID> getCustomerAvailablePromotions(UUID customerId) {
    List<EPromotionProgram> activePrograms = programRepository.findActivePrograms(Instant.now());

    return activePrograms.stream()
        .filter(program -> validatePromotionEligibility(customerId, program.getId(), null))
        .map(EPromotionProgram::getId)
        .collect(Collectors.toList());
  }

  // Removed calculateDiscountAmount - using DiscountService instead
}
