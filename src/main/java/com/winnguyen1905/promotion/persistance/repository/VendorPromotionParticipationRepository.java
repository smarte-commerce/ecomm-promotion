package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation;

@Repository
public interface VendorPromotionParticipationRepository extends JpaRepository<EVendorPromotionParticipation, UUID> {
  List<EVendorPromotionParticipation> findAllByVendorId(UUID vendorId);

  long countByProgramIdAndVendorId(UUID programId, UUID vendorId);
} 
