package com.winnguyen1905.promotion.core.model.response;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record ApplyDiscountResponse(
    UUID cartId,
    UUID shopId,
    UUID discountId,
    Boolean isOrder,
    PriceStatisticsResponse priceStatisticsResponse) implements AbstractModel {
}
