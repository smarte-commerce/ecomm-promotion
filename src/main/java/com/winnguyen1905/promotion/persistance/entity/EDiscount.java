package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder.Default;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discounts", schema = "public")
public class EDiscount {

  public static enum Scope {
    SHOP, GLOBAL
  }

  public static enum CreatorType {
    ADMIN, SHOP
  }

  @Version
  private long version;

  @Id
  // @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "creator_type", nullable = false)
  private CreatorType creatorType;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private DiscountType discountType;

  @Enumerated(EnumType.STRING)
  @Column(name = "applies_to")
  private ApplyDiscountType appliesTo;

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  private DiscountCategory discountCategory;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Min(value = 0)
  @Column(name = "value")
  private Double value;

  @Min(value = 0)
  @Column(name = "max_reduced_value")
  private Double maxReducedValue;

  @Column(name = "code")
  private String code;

  @Column(name = "start_date")
  private Instant startDate;

  @Column(name = "end_date")
  private Instant endDate;

  @Min(value = 1)
  @Column(name = "usage_limit")
  private int usageLimit;

  @Min(value = 0)
  @Column(name = "usage_count")
  private int usageCount;

  @Min(value = 1)
  @Column(name = "limit_usage_per_customer")
  private int limitUsagePerCutomer;

  @Min(value = 0)
  @Column(name = "min_order_value")
  private Double minOrderValue;

  @Column(name = "is_active")
  private Boolean isActive;

  @Column(name = "shop_id", nullable = true)
  private UUID shopId;

  @ManyToOne
  @JoinColumn(name = "program_id", nullable = true)
  private EPromotionProgram program;

  @Default
  @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EDiscountUsage> discountUsages = new ArrayList<>();

  @Default
  @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EUserDiscount> userDiscounts = new ArrayList<>();

  @Default
  @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
      CascadeType.REMOVE })
  private List<EProductDiscount> productDiscounts = new ArrayList<>();

  @PrePersist
  public void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
