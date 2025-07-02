package com.winnguyen1905.promotion.model.request;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignStatus;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignType;

import lombok.Builder;

@Builder
public record UpdateCampaignRequest(
    String name,
    String description,
    CampaignType campaignType,
    Instant startDate,
    Instant endDate,
    CampaignStatus status,
    Double budget,
    JsonNode targetAudience,
    Long version) implements AbstractModel {
} 
