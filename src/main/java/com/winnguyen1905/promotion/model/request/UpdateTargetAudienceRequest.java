package com.winnguyen1905.promotion.model.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateTargetAudienceRequest(
    @NotNull JsonNode targetAudience,
    @NotNull Long version) implements AbstractModel {
} 
