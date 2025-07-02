package com.winnguyen1905.promotion.model.request;

import java.util.List;
import java.util.UUID;

public record AssignCategoriesRequest(
    UUID discountId,
    List<String> categories) {
}
