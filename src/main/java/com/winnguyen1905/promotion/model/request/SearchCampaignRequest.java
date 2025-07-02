package com.winnguyen1905.promotion.model.request;

import java.time.Instant;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignStatus;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignType;

import lombok.Builder;

@Builder
public record SearchCampaignRequest(
    String name,
    CampaignStatus status,
    CampaignType campaignType,
    Instant startDate,
    Instant endDate,
    UUID createdBy,
    Boolean includeExpired) implements AbstractModel {
} 
