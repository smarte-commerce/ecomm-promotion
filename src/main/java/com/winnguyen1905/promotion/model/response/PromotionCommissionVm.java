package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission.PaymentStatus;

import lombok.Builder;

@Builder
public record PromotionCommissionVm(
    UUID id,
    UUID programId,
    String programName,
    UUID vendorId,
    String vendorName,
    UUID orderId,
    UUID customerId,
    Double orderAmount,
    Double discountAmount,
    Double vendorContribution,
    Double platformContribution,
    Double commissionAmount,
    Double commissionRate,
    PaymentStatus paymentStatus,
    Instant paymentDate,
    String transactionId,
    Instant createdAt,
    Instant processedAt
) implements AbstractModel {
} 
