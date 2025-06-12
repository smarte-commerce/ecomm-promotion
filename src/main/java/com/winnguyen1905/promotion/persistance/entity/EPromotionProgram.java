package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "promotion_programs", schema = "public")
public class EPromotionProgram {
  @Version
  private long version;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Column(name = "platform_commission_rate", nullable = false)
  private Double platformCommissionRate;

  @Column(name = "required_discount", nullable = false)
  private Double requiredDiscount;

  @Enumerated(EnumType.STRING)
  @Column(name = "visibility", nullable = false)
  private Visibility visibility;

  @Column(name = "terms_url")
  private String termsUrl;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @JsonIgnore
  @Column(name = "updated_by", nullable = true)
  private String updatedBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", updatable = true)
  private Instant updatedDate;

  @Default
  @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
  private List<ECommissionPayout> commissionPayouts = new ArrayList<>();

  @Default
  @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
  private List<EShopPromotionParticipation> shopPromotionParticipations = new ArrayList<>();

  @Default
  @OneToMany(mappedBy = "program", fetch = FetchType.LAZY)
  private List<EDiscount> discounts = new ArrayList<>();

  public enum Visibility {
    PUBLIC, INVITE_ONLY
  }
}
