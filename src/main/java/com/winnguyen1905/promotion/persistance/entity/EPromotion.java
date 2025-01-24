package com.winnguyen1905.promotion.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promotion")
public class EPromotion extends EBaseAudit {
    @Column(name = "promotion_name")
    private String promotionName;

    @Column(name = "base_commission_rate")
    private String baseCommissionRate;
}