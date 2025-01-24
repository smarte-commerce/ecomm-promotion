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
@Table(name = "promotion_details")
public class EUserDiscount extends EBaseAudit {
  @ManyToOne
  @JoinColumn(name = "discount_id")
  private EDiscount discount;

  @Column(name = "customer_id")
  private UUID customerId;
}