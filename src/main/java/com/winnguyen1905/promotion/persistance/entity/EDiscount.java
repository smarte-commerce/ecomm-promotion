package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder.Default;

@Entity
@Getter
@Setter
@Builder
@Table(name = "discounts")
public class EDiscount extends EBaseAudit {

  public static enum Scope {
    SHOP, GLOBAL
  }

  public static enum CreatorType {
    ADMIN, SHOP
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_scope")
  private Scope scope;

  @Enumerated(EnumType.STRING)
  @Column(name = "creator_type", nullable = false)
  private CreatorType creatorType;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type")
  private DiscountType discountType;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_applies_to")
  private ApplyDiscountType appliesTo;

  @Column(name = "discount_name")
  private String name;

  @Column(name = "discount_description", columnDefinition = "MEDIUMTEXT")
  private String description;

  @Min(value = 0)
  @Column(name = "discount_value")
  private Double value;

  @Column(name = "discount_code")
  private String code;

  @Column(name = "discount_start_date")
  private Instant startDate;

  @Column(name = "discount_end_date")
  private Instant endDate;

  @Min(value = 1)
  @Column(name = "discount_usage_limit")
  private int usageLimit;

  @Min(value = 0)
  @Column(name = "discount_usage_count")
  private int usageCount;

  @Min(value = 1)
  @Column(name = "discount_limit_usage_per_customer")
  private int limitUsagePerCutomer;

  @Min(value = 0)
  @Column(name = "discount_min_order_value")
  private Double minOrderValue;

  @Column(name = "discount_is_active")
  private Boolean isActive;

  @Column(name = "shop_id", nullable = true)
  private UUID shopId;

  @Default
  @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
  private List<EDiscountUsage> discountUsages = new ArrayList<>();

  @Default
  @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
  private List<EUserDiscount> userDiscounts = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "promotion_id", nullable = true)
  private EPromotion promotion;

  @OneToMany(mappedBy = "discount")
  private List<EProductDiscount> productDiscounts;


  @PrePersist
  protected void prePersist() {
    // this.setDiscountType(this.discountType == null ? DiscountType.FIXED_AMOUNT :
    // this.discountType);
    // this.setAppliesTo(this.appliesTo == null ? ApplyDiscountType.ALL :
    // this.appliesTo);
    // this.setIsActive(this.isActive == null ? false : this.isActive);
    // super.prePersist();
  }
}
