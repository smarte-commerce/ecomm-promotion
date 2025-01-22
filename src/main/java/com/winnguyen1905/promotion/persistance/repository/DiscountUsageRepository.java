package com.winnguyen1905.promotion.persistance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;

@Repository
public interface DiscountUsageRepository extends JpaRepository<EDiscountUsage, UUID> {
    int countByDiscountIdAndCustomerId(UUID customerId, UUID discountId);
}