package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignStatus;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignType;

import lombok.Builder;

@Builder
public record CampaignVm(
    UUID id,
    String name,
    String description,
    CampaignType campaignType,
    Instant startDate,
    Instant endDate,
    CampaignStatus status,
    Double budget,
    Double spentBudget,
    JsonNode targetAudience,
    UUID createdBy) implements AbstractModel {
} 
