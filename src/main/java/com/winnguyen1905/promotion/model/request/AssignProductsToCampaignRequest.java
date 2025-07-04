package com.winnguyen1905.promotion.model.request;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.Builder;

@Builder
public record AssignProductsToCampaignRequest(
    UUID campaignId,
    List<UUID> productIds,
    Long version) implements AbstractModel {
} 
