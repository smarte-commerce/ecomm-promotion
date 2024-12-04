package com.winnguyen1905.promotion.core.model.request;

import com.winnguyen1905.promotion.core.model.Product;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateProductRequest extends Product {
  private Boolean isDraft;
  private Boolean isPublished;
}
