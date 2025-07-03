package com.winnguyen1905.promotion.model.response;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EFlashSale.Status;

import lombok.Builder;

@Builder
public record FlashSaleVm(
    UUID id,
    UUID programId,
    String programName,
    Instant countdownStart,
    Instant countdownEnd,
    Integer maxQuantity,
    Integer soldQuantity,
    JsonNode priceTiers,
    Boolean notificationSent,
    Boolean isNotifyEnabled,
    Status status
) implements AbstractModel {
} 
