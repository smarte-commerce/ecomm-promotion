package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EPromotionProduct;

@Repository
public interface PromotionProductRepository extends JpaRepository<EPromotionProduct, UUID> {
  List<EPromotionProduct> findAllByProgramId(UUID programId);

  long countByProgramIdAndProductId(UUID programId, UUID productId);
} 
