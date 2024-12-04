package com.winnguyen1905.promotion.core.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.core.model.AbstractModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplyDiscountRequest extends AbstractModel {
  private UUID cartId;
  private UUID discountId;
}
