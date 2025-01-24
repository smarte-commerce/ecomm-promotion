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
@Table(name = "product_discount")
public class EProductDiscount extends EBaseAudit {
    @ManyToOne
    @JoinColumn(name = "discount_id")
    EDiscount discount;

    @Column(name = "product_id")
    private UUID productId;
}
