package com.winnguyen1905.promotion.model.request;

import java.time.Instant;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramStatus;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.Visibility;

public record UpdatePromotionProgramRequest(
    String name,
    String description,
    Instant startDate,
    Instant endDate,
    Integer priority,
    Boolean isStackable,
    Double platformCommissionRate,
    Double requiredVendorContribution,
    Visibility visibility,
    Integer usageLimitGlobal,
    String termsConditions,
    String termsUrl,
    Boolean autoApply,
    ProgramStatus status
) implements AbstractModel {
} 
