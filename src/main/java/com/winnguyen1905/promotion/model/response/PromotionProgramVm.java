package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramStatus;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.Visibility;

import lombok.Builder;

@Builder
public record PromotionProgramVm(
    UUID id,
    String name,
    String description,
    ProgramType programType,
    Instant startDate,
    Instant endDate,
    Integer priority,
    Boolean isStackable,
    Double platformCommissionRate,
    Double requiredVendorContribution,
    Visibility visibility,
    Integer usageLimitGlobal,
    Integer usageCountGlobal,
    String termsConditions,
    String termsUrl,
    Boolean autoApply,
    ProgramStatus status,
    UUID createdBy,
    UUID updatedBy,
    Instant createdAt,
    Instant updatedAt,
    String campaignName,
    UUID campaignId
) implements AbstractModel {
} 
