package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;

import jakarta.validation.constraints.NotNull;

public record CreateUserDiscountRequest(
    @NotNull UUID discountId,
    @NotNull UUID customerId
) implements AbstractModel {
} 
