package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.winnguyen1905.promotion.core.service.PromotionCommissionService;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PromotionCommissionVm;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission.PaymentStatus;
import com.winnguyen1905.promotion.persistance.repository.PromotionCommissionRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionCommissionServiceImpl implements PromotionCommissionService {

  private final PromotionCommissionRepository commissionRepository;

  private PromotionCommissionVm toVm(EPromotionCommission entity) {
    if (entity == null)
      return null;
    return PromotionCommissionVm.builder()
        .id(entity.getId())
        .programId(entity.getProgram().getId())
        .programName(entity.getProgram().getName())
        .vendorId(entity.getVendorId())
        .orderId(entity.getOrderId())
        .customerId(entity.getCustomerId())
        .orderAmount(entity.getOrderAmount())
        .discountAmount(entity.getDiscountAmount())
        .vendorContribution(entity.getVendorContribution())
        .platformContribution(entity.getPlatformContribution())
        .commissionAmount(entity.getCommissionAmount())
        .commissionRate(entity.getCommissionRate())
        .paymentStatus(entity.getPaymentStatus())
        .paymentDate(entity.getPaymentDate())
        .transactionId(entity.getTransactionId())
        .createdAt(entity.getCreatedAt())
        .processedAt(entity.getProcessedAt())
        .build();
  }

  @Override
  @Transactional
  public void createCommission(UUID programId, UUID vendorId, UUID orderId, UUID customerId, Double orderAmount,
      Double discountAmount) {
    double vendorContribution = discountAmount * 0.5; // simplistic split
    double platformContribution = discountAmount - vendorContribution;
    double commissionRate = 0.05; // fixed example
    double commissionAmount = orderAmount * commissionRate;

    EPromotionCommission commission = EPromotionCommission.builder()
        .program(com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.builder().id(programId).build())
        .vendorId(vendorId)
        .orderId(orderId)
        .customerId(customerId)
        .orderAmount(orderAmount)
        .discountAmount(discountAmount)
        .vendorContribution(vendorContribution)
        .platformContribution(platformContribution)
        .commissionAmount(commissionAmount)
        .commissionRate(commissionRate)
        .paymentStatus(PaymentStatus.PENDING)
        .build();
    commissionRepository.save(commission);
  }

  @Override
  public PromotionCommissionVm getCommissionById(TAccountRequest accountRequest, UUID id) {
    return commissionRepository.findById(id).map(this::toVm)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission not found"));
  }

  @Override
  public PagedResponse<PromotionCommissionVm> getVendorCommissions(TAccountRequest accountRequest, UUID vendorId,
      Pageable pageable) {
    List<EPromotionCommission> list = commissionRepository.findByVendorId(vendorId);
    return mapList(list, pageable);
  }

  @Override
  public PagedResponse<PromotionCommissionVm> getProgramCommissions(TAccountRequest accountRequest, UUID programId,
      Pageable pageable) {
    List<EPromotionCommission> list = commissionRepository.findByProgramId(programId);
    return mapList(list, pageable);
  }

  @Override
  public PagedResponse<PromotionCommissionVm> getCommissionsByStatus(TAccountRequest accountRequest,
      PaymentStatus status,
      Pageable pageable) {
    List<EPromotionCommission> list = commissionRepository.findByPaymentStatus(status);
    return mapList(list, pageable);
  }

  @Override
  @Transactional
  public void updatePaymentStatus(TAccountRequest accountRequest, UUID id, PaymentStatus status, String transactionId) {
    EPromotionCommission commission = commissionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commission not found"));
    commission.setPaymentStatus(status);
    commission.setTransactionId(transactionId);
    commission.setPaymentDate(Instant.now());
    commissionRepository.save(commission);
  }

  @Override
  @Transactional
  public void processCommissionPayment(TAccountRequest accountRequest, UUID id) {
    updatePaymentStatus(accountRequest, id, PaymentStatus.PAID, UUID.randomUUID().toString());
  }

  @Override
  @Transactional
  public void processVendorCommissionPayments(UUID vendorId) {
    commissionRepository.findByVendorId(vendorId).forEach(c -> {
      if (c.getPaymentStatus() == PaymentStatus.PENDING) {
        c.setPaymentStatus(PaymentStatus.PAID);
        c.setPaymentDate(Instant.now());
        commissionRepository.save(c);
      }
    });
  }

  @Override
  @Transactional
  public void processPendingCommissions() {
    commissionRepository.findPendingCommissionsOlderThan(Instant.now().minusSeconds(86400))
        .forEach(c -> {
          c.setPaymentStatus(PaymentStatus.PAID);
          c.setPaymentDate(Instant.now());
          commissionRepository.save(c);
        });
  }

  @Override
  public Double getTotalVendorCommission(UUID vendorId, PaymentStatus status) {
    return commissionRepository.getTotalCommissionByVendorAndStatus(vendorId, status);
  }

  @Override
  public Double getTotalProgramCommission(UUID programId) {
    return commissionRepository.getTotalVendorContributionByProgram(programId);
  }

  private PagedResponse<PromotionCommissionVm> mapList(List<EPromotionCommission> list, Pageable pageable) {
    int page = pageable.getPageNumber();
    int size = pageable.getPageSize();
    int fromIndex = Math.min(page * size, list.size());
    int toIndex = Math.min(fromIndex + size, list.size());
    List<PromotionCommissionVm> slice = list.subList(fromIndex, toIndex).stream().map(this::toVm)
        .collect(Collectors.toList());

    long total = list.size();
    long totalPages = size == 0 ? 1 : (long) Math.ceil((double) total / size);

    return PagedResponse.<PromotionCommissionVm>builder()
        .results(slice)
        .page(page)
        .size(size)
        .totalElements(total)
        .totalPages(totalPages)
        .maxPageItems(size)
        .build();
  }
}
