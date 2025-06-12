package com.winnguyen1905.promotion.persistance.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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
@Table(name = "promotions", schema = "public")
public class EPromotion {
  @Version
  private long version;

  @Id
  // @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "promotion_name")
  private String promotionName;

  @Column(name = "base_commission_rate")
  private String baseCommissionRate;
}
