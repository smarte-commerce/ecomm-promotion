package com.winnguyen1905.promotion.core.service.impl;

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

import com.winnguyen1905.promotion.core.service.DiscountUsageService;
import com.winnguyen1905.promotion.model.request.CreateDiscountUsageRequest;
import com.winnguyen1905.promotion.model.response.DiscountUsageVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountUsageServiceImpl implements DiscountUsageService {

  private final DiscountUsageRepository discountUsageRepository;
  private final DiscountRepository discountRepository;

  private DiscountUsageVm toVm(EDiscountUsage entity) {
    if (entity == null) {
      return null;
    }
    return DiscountUsageVm.builder()
        .id(entity.getId())
        .customerId(entity.getCustomerId())
        .programId(entity.getProgram() != null ? entity.getProgram().getId() : null)
        .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
        .discountId(entity.getDiscount() != null ? entity.getDiscount().getId() : null)
        .discountName(entity.getDiscount() != null ? entity.getDiscount().getName() : null)
        .discountCode(entity.getDiscount() != null ? entity.getDiscount().getCode() : null)
        .orderId(entity.getOrderId())
        .usageCount(entity.getUsageCount())
        .discountAmount(entity.getDiscountAmount())
        .cashbackAmount(entity.getCashbackAmount())
        .pointsEarned(entity.getPointsEarned())
        .usageDate(entity.getUsageDate())
        .usageStatus(entity.getUsageStatus())
        .build();
  }

  @Override
  @Transactional
  public void recordDiscountUsage(TAccountRequest accountRequest, CreateDiscountUsageRequest request) {
    EDiscount discount = discountRepository.findById(request.discountId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount not found"));

    EDiscountUsage usage = EDiscountUsage.builder()
        .customerId(request.customerId())
        .program(discount.getProgram())
        .discount(discount)
        .orderId(request.orderId())
        .usageCount(request.usageCount() != null ? request.usageCount() : 1)
        .discountAmount(request.discountAmount())
        .cashbackAmount(request.cashbackAmount() != null ? request.cashbackAmount() : 0.0)
        .pointsEarned(request.pointsEarned() != null ? request.pointsEarned() : 0)
        .usageStatus(request.usageStatus())
        .build();
    discountUsageRepository.save(usage);
  }

  @Override
  @Transactional(readOnly = true)
  public DiscountUsageVm getDiscountUsageById(TAccountRequest accountRequest, UUID id) {
    EDiscountUsage usage = discountUsageRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usage not found"));
    return toVm(usage);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<DiscountUsageVm> getCustomerDiscountUsage(TAccountRequest accountRequest, UUID customerId,
      Pageable pageable) {
    Specification<EDiscountUsage> spec = (root, query, cb) -> cb.equal(root.get("customerId"), customerId);
    Page<EDiscountUsage> page = discountUsageRepository.findAll(spec, pageable);
    List<DiscountUsageVm> vms = page.getContent().stream().map(this::toVm).collect(Collectors.toList());
    return buildPagedResponse(page, vms);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<DiscountUsageVm> getDiscountUsageHistory(TAccountRequest accountRequest, UUID discountId,
      Pageable pageable) {
    Specification<EDiscountUsage> spec = (root, query, cb) -> cb.equal(root.join("discount").get("id"), discountId);
    Page<EDiscountUsage> page = discountUsageRepository.findAll(spec, pageable);
    return buildPagedResponse(page, page.getContent().stream().map(this::toVm).collect(Collectors.toList()));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<DiscountUsageVm> getProgramUsageHistory(TAccountRequest accountRequest, UUID programId,
      Pageable pageable) {
    Specification<EDiscountUsage> spec = (root, query, cb) -> cb.equal(root.join("program").get("id"), programId);
    Page<EDiscountUsage> page = discountUsageRepository.findAll(spec, pageable);
    return buildPagedResponse(page, page.getContent().stream().map(this::toVm).collect(Collectors.toList()));
  }

  @Override
  public Integer getCustomerDiscountUsageCount(UUID customerId, UUID discountId) {
    return discountUsageRepository.countByDiscountIdAndCustomerId(discountId, customerId);
  }

  @Override
  public boolean canCustomerUseDiscount(UUID customerId, UUID discountId) {
    EDiscount discount = discountRepository.findById(discountId).orElse(null);
    if (discount == null)
      return false;
    int usage = getCustomerDiscountUsageCount(customerId, discountId);
    return discount.getUsageLimitPerCustomer() == null || usage < discount.getUsageLimitPerCustomer();
  }

  @Override
  @Transactional
  public void refundDiscountUsage(TAccountRequest accountRequest, UUID usageId) {
    EDiscountUsage usage = discountUsageRepository.findById(usageId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usage not found"));
    discountUsageRepository.delete(usage);
  }

  private PagedResponse<DiscountUsageVm> buildPagedResponse(Page<?> page, List<DiscountUsageVm> data) {
    return PagedResponse.<DiscountUsageVm>builder()
        .results(data)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .maxPageItems(page.getSize())
        .build();
  }
}
