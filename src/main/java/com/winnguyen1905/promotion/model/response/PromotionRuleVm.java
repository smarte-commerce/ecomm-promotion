package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EPromotionRule.Operator;
import com.winnguyen1905.promotion.persistance.entity.EPromotionRule.RuleType;

import lombok.Builder;

@Builder
public record PromotionRuleVm(
    UUID id,
    UUID programId,
    String programName,
    RuleType ruleType,
    Operator operator,
    JsonNode value,
    Boolean isRequired,
    Instant createdAt
) implements AbstractModel {
} 
