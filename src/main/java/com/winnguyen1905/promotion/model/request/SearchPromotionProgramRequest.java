package com.winnguyen1905.promotion.model.request;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramStatus;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.Visibility;

public record SearchPromotionProgramRequest(
    String name,
    ProgramType programType,
    ProgramStatus status,
    Visibility visibility,
    Instant startDateFrom,
    Instant startDateTo,
    Instant endDateFrom,
    Instant endDateTo,
    UUID campaignId,
    Boolean isActive,
    Boolean isStackable,
    UUID createdBy
) implements AbstractModel {
} 
