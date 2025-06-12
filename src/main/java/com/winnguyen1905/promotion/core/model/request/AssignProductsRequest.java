package com.winnguyen1905.promotion.core.model.request;

import java.util.List;
import java.util.UUID;

public record AssignProductsRequest(
    UUID discountId,
    List<UUID> productIds) {
}
