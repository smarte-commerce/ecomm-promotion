package com.winnguyen1905.promotion.model.request;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateFlashSaleRequest(
    @NotNull UUID programId,
    @NotNull Instant countdownStart,
    @NotNull Instant countdownEnd,
    @Positive Integer maxQuantity,
    JsonNode priceTiers,
    Boolean isNotifyEnabled
) implements AbstractModel {
} 
