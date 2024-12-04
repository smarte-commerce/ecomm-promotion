package com.winnguyen1905.promotion.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EDiscountUser;
import com.winnguyen1905.promotion.persistance.repository.custom.SoftDeleteRepository;

@Repository
public interface UserDiscountRepository extends JpaRepository<EDiscountUser, UUID>, SoftDeleteRepository<EDiscountUser, UUID> {
  Optional<EDiscountUser> findByUserIdAndDiscountId(UUID userId, UUID discountId);
}
