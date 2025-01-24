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
@Table(name = "discount_users")
public class EDiscountUsage extends EBaseAudit {
  @Column(name = "remaining_usage")
  private UUID orderId; 

  @Column(name = "customer_id")
  private UUID customerId; // optional field for checking user used discount

  @Column(name = "usage_status")
  private Boolean usageStatus;

  @ManyToOne
  @JoinColumn(name = "user_discount_id")
  private EUserDiscount userDiscount;
}
