package com.winnguyen1905.promotion.persistance.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.ECampaign;
import com.winnguyen1905.promotion.persistance.entity.ECampaign.CampaignStatus;

@Repository
public interface CampaignRepository extends JpaRepository<ECampaign, UUID>, JpaSpecificationExecutor<ECampaign> {
  // Find all campaigns by status
  Page<ECampaign> findAllByStatus(CampaignStatus status, Pageable pageable);

  // Get campaigns created by specific user
  Page<ECampaign> findAllByCreatedBy(UUID createdBy, Pageable pageable);

  // Search campaigns in date range
  List<ECampaign> findByStartDateBetween(Instant startDate, Instant endDate);

  // Delete by ids
  void deleteByIdIn(List<UUID> ids);
} 
