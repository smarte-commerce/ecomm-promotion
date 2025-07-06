package com.winnguyen1905.promotion.model.request;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.Visibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePromotionProgramRequest(
    @NotBlank String name,
    String description,
    @NotNull ProgramType programType,
    @NotNull Instant startDate,
    @NotNull Instant endDate,
    Integer priority,
    Boolean isStackable,
    Double platformCommissionRate,
    Double requiredVendorContribution,
    Visibility visibility,
    Integer usageLimitGlobal,
    String termsConditions,
    String termsUrl,
    Boolean autoApply,
    UUID campaignId
) implements AbstractModel {
} 
