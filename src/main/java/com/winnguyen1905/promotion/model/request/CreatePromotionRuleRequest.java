package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionRule.Operator;
import com.winnguyen1905.promotion.persistance.entity.EPromotionRule.RuleType;

import jakarta.validation.constraints.NotNull;

public record CreatePromotionRuleRequest(
    @NotNull UUID programId,
    @NotNull RuleType ruleType,
    @NotNull Operator operator,
    @NotNull JsonNode value,
    Boolean isRequired
) implements AbstractModel {
} 
