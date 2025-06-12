package com.winnguyen1905.promotion.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.custom.SoftDeleteRepository;

@Repository
public interface UserDiscountRepository extends JpaRepository<EUserDiscount, UUID> {
  Boolean existsByCustomerIdAndDiscountId(UUID customerId, UUID discountId);
  Optional<EUserDiscount> findByCustomerIdAndDiscountId(UUID customerId, UUID discountId);
}
