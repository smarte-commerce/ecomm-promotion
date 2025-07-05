package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "campaigns", schema = "public")
public class ECampaign {

  @Version
  private long version;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "campaign_type", nullable = false)
  private CampaignType campaignType;

  @Column(name = "start_date", nullable = false)
  private Instant startDate;

  @Column(name = "end_date", nullable = false)
  private Instant endDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private CampaignStatus status = CampaignStatus.DRAFT;

  @Column(name = "budget")
  private Double budget;

  @Column(name = "spent_budget")
  private Double spentBudget = 0.0;

  @Column(name = "target_audience", columnDefinition = "jsonb")
  private JsonNode targetAudience;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "approved_by")
  private UUID approvedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;

  @JsonIgnore
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @JsonIgnore
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Default
  @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY)
  private List<EPromotionProgram> promotionPrograms = new ArrayList<>();

  @PrePersist
  protected void prePersist() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
    if (this.status == null) {
      this.status = CampaignStatus.DRAFT;
    }
    if (this.spentBudget == null) {
      this.spentBudget = 0.0;
    }
  }

  public enum CampaignType {
    FLASH_SALE, SEASONAL, CLEARANCE, NEW_PRODUCT, LOYALTY, AFFILIATE
  }

  public enum CampaignStatus {
    DRAFT, SCHEDULED, ACTIVE, PAUSED, COMPLETED, CANCELLED
  }
}
