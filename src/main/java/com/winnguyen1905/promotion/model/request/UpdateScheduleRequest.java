package com.winnguyen1905.promotion.model.request;

import java.time.Instant;

import com.winnguyen1905.promotion.model.AbstractModel;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateScheduleRequest(
    @NotNull Instant startDate,
    @NotNull Instant endDate,
    @NotNull Long version) implements AbstractModel {
} 
