package com.winnguyen1905.promotion.persistance.entity;

import java.util.UUID;

import com.winnguyen1905.promotion.common.DiscountUsageStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.FetchType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "discount_users", schema = "public")
public class EDiscountUsage {
  @Id
  // @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "remaining_usage")
  private UUID orderId;

  @Column(name = "customer_id")
  private UUID customerId; 

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "discount_id", nullable = false)
  private EDiscount discount;

  @Enumerated(EnumType.STRING)
  @Column(name = "usage_status")
  private DiscountUsageStatus usageStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_discount_id", nullable = false)
  private EUserDiscount userDiscount;

  @PrePersist
  public void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
