package com.winnguyen1905.promotion.persistance.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "shop_promotion_participations", schema = "public")
public class EShopPromotionParticipation {

  @Version
  private long version;

  @Id
  private UUID id;

  @Column(name = "shop_id", nullable = false)
  private UUID shopId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "program_id", nullable = false)
  private EPromotionProgram program;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Column(name = "discount_value", nullable = false)
  private Double discountValue;

  @Column(name = "accepted_terms", nullable = false)
  private Boolean acceptedTerms;

  @Default
  @Column(name = "joined_at", nullable = false, updatable = false)
  private LocalDateTime joinedAt = LocalDateTime.now();

  public enum Status {
    PENDING, APPROVED, REJECTED, WITHDRAWN
  }

  @PrePersist
  protected void onCreate() {
    if (this.status == null) {
      this.status = Status.PENDING;
    }
    if (this.acceptedTerms == null) {
      this.acceptedTerms = false;
    }
  }
}
