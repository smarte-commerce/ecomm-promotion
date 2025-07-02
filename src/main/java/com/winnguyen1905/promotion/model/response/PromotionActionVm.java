package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction.ActionType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction.Target;

import lombok.Builder;

@Builder
public record PromotionActionVm(
    UUID id,
    UUID programId,
    String programName,
    ActionType actionType,
    Target target,
    Double value,
    Double maxDiscountAmount,
    JsonNode appliesTo,
    Instant createdAt
) implements AbstractModel {
} 
