package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;

@Repository
public interface ProductDiscountRepository extends JpaRepository<EProductDiscount, UUID> {
  List<EProductDiscount> findByProductId(UUID productId);
  List<EProductDiscount> findByDiscountId(UUID discountId);
  boolean existsByProductIdAndDiscountId(UUID productId, UUID discountId);  
  @Query("select id from EProductDiscount pd where pd.productId in ?1 and pd.discount.id = ?2")
  List<UUID> findAllByProductIdInAAndDiscountId(List<UUID> productId, UUID discountId);
}
