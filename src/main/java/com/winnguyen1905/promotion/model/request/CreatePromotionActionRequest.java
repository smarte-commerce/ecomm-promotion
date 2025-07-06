package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction.ActionType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction.Target;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePromotionActionRequest(
    @NotNull UUID programId,
    @NotNull ActionType actionType,
    @NotNull Target target,
    @Positive Double value,
    Double maxDiscountAmount,
    JsonNode appliesTo
) implements AbstractModel {
} 
