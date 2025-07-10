package com.winnguyen1905.promotion.model.response;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveDiscountResponse implements AbstractModel {
  private UUID sagaId;
  private UUID customerId;
  private String eventType;
  private UUID globalProductDiscountId;
  private UUID globalShippingDiscountId;
  private String globalShippingDiscountType; // FIXED or PERCENTAGE
  private List<DrawOrder> checkoutItems;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DrawOrder {
    private UUID shopId;
    private UUID orderId;
    private UUID customerId;
    private List<DrawOrderItem> items;
    private UUID shopProductDiscountId;
    private Double totalOrderBeforeDiscounts;
    private Double totalShopProductDiscount;
    private Double totalGlobalProductDiscount;
    private Double totalGlobalShippingDiscount;
    private Double totalOrderAfterDiscounts;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DrawOrderItem {
    private UUID productId;
    private UUID variantId;
    private Integer quantity;
    private Double unitPrice;
    private String productSku;
    private boolean isEligibleForDiscount;
  }
}
