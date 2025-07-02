package com.winnguyen1905.promotion.persistance.entity;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
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
@Table(name = "flash_sales", schema = "public")
public class EFlashSale {
    
    @Version
    private long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false, unique = true)
    private EPromotionProgram program;

    @Column(name = "countdown_start", nullable = false)
    private Instant countdownStart;

    @Column(name = "countdown_end", nullable = false)
    private Instant countdownEnd;

    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity;

    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0;

    @Column(name = "price_tiers", columnDefinition = "jsonb")
    private JsonNode priceTiers;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "is_notify_enabled")
    private Boolean isNotifyEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.UPCOMING;

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.soldQuantity == null) {
            this.soldQuantity = 0;
        }
        if (this.notificationSent == null) {
            this.notificationSent = false;
        }
        if (this.isNotifyEnabled == null) {
            this.isNotifyEnabled = true;
        }
        if (this.status == null) {
            this.status = Status.UPCOMING;
        }
    }

    public enum Status {
        UPCOMING, LIVE, SOLD_OUT, ENDED
    }
} 
