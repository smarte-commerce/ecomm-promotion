package com.winnguyen1905.promotion.persistance.entity;

import java.util.UUID;

import com.winnguyen1905.promotion.common.ApplyDiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promotion_details")
public class EPromotionDetail extends EBaseAudit {
    @ManyToOne
    @JoinColumn(name = "discount_id")
    private EDiscount discount;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "shop_id")
    private UUID shopId;
}
