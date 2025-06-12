package com.winnguyen1905.promotion.persistance.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "commission_payouts", schema = "public")
public class ECommissionPayout {
  @Version
  private long version;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "program_id", nullable = false)
  private EPromotionProgram program;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "amount", nullable = false)
  private Double amount;

  @Default
  @Column(name = "paid", nullable = false)
  private Boolean paid = false;

  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  @Default
  @Column(name = "processed_at")
  private LocalDateTime processedAt = LocalDateTime.now();

  @PrePersist
  protected void onPersist() {
    if (this.processedAt == null) {
      this.processedAt = LocalDateTime.now();
    }
    if (this.paid == null) {
      this.paid = false;
    }
  }
}
