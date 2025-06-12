package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EShopPromotionParticipation;

@Repository
public interface ShopPromotionParticipationRepository extends JpaRepository<EShopPromotionParticipation, UUID> {
    List<EShopPromotionParticipation> findByShopId(UUID shopId);
    List<EShopPromotionParticipation> findByProgramId(UUID programId);
    boolean existsByShopIdAndProgramId(UUID shopId, UUID programId);
}
