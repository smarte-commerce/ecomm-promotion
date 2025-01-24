package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EShopPromotion;

@Repository
public interface ShopPromotionRepository extends JpaRepository<EShopPromotion, UUID> {
  Optional<EShopPromotion> findByShopIdAndPromotionId(UUID shopId, UUID promotionId);

  @Query("select id from shop_promotion where shop_id in ?1 and promotion_id = ?2 and is_verified = true")
  List<UUID> findAllByShopIdsAndPromotionIdAndIsVerifiedTrue(List<UUID> ids, UUID promotionId);
}
