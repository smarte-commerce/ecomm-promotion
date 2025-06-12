package com.winnguyen1905.promotion.core.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;

@Builder
public record PagedResponse<T>(
    long maxPageItems,
    long page,
    long size,
    @JsonProperty("results") List<T> results,
    long totalElements,
    long totalPages) implements AbstractModel {
  @Builder
  public PagedResponse(
      long maxPageItems,
      long page,
      long size,
      @JsonProperty("results") List<T> results,
      long totalElements,
      long totalPages) {
    this.maxPageItems = maxPageItems;
    this.page = page;
    this.size = size;
    this.results = results;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
  }
}
