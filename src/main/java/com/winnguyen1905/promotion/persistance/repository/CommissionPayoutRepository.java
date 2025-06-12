package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.ECommissionPayout;

@Repository
public interface CommissionPayoutRepository extends JpaRepository<ECommissionPayout, UUID> {
  List<ECommissionPayout> findByShopId(UUID shopId);

  List<ECommissionPayout> findByProgramId(UUID programId);

  List<ECommissionPayout> findByOrderId(UUID orderId);

  List<ECommissionPayout> findByPaid(boolean paid);
}
