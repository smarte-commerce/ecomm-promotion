package com.winnguyen1905.promotion.persistance.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shop_promotion")
public class EShopPromotion extends EBaseAudit {
  private double totalCommissionRate;
  private double conditionJoin;
  private UUID shopId;
  private UUID promotionId;
  private Boolean isVerified;
}
