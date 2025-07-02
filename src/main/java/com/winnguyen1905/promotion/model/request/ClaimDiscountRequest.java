
package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

public record ClaimDiscountRequest(
    String code,
    UUID userId
) {}
