package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.ParticipationType;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.ProductSelection;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.Status;

import lombok.Builder;

@Builder
public record VendorParticipationVm(
    UUID id,
    UUID vendorId,
    String vendorName,
    UUID programId,
    String programName,
    ParticipationType participationType,
    Status status,
    Double vendorContributionRate,
    Double expectedDiscountRate,
    Double minDiscountAmount,
    Double maxDiscountAmount,
    ProductSelection productSelection,
    Boolean acceptedTerms,
    Instant joinedAt,
    Instant approvedAt,
    UUID approvedBy,
    String withdrawalReason,
    JsonNode performanceMetrics
) implements AbstractModel {
} 
