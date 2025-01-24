package com.winnguyen1905.promotion.core.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Getter;
import lombok.Setter;
public record PagedResponse<T>(
    int maxPageItems,
    int page,
    int size,
    @JsonProperty("results") List<T> results,
    int totalElements,
    int totalPages
) implements AbstractModel {
}
