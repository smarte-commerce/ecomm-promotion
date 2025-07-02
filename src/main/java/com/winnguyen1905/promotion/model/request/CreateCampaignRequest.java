package com.winnguyen1905.promotion.model.request;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateCampaignRequest(
    @NotBlank String name,
    String description,
    @NotNull CampaignType campaignType,
    @NotNull Instant startDate,
    @NotNull Instant endDate,
    Double budget,
    JsonNode targetAudience) implements AbstractModel {
} 
