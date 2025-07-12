package com.winnguyen1905.promotion.persistance.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramStatus;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.Visibility;

@Repository
public interface PromotionProgramRepository extends JpaRepository<EPromotionProgram, UUID>, JpaSpecificationExecutor<EPromotionProgram> {
    
    List<EPromotionProgram> findByStatus(ProgramStatus status);
    
    List<EPromotionProgram> findByProgramType(ProgramType programType);
    
    List<EPromotionProgram> findByVisibility(Visibility visibility);
    
    Page<EPromotionProgram> findByCreatedBy(UUID createdBy, Pageable pageable);
    
    List<EPromotionProgram> findByStartDateBeforeAndEndDateAfter(Instant startDate, Instant endDate);
    
    @Query("SELECT pp FROM EPromotionProgram pp WHERE pp.startDate <= :now AND pp.endDate >= :now AND pp.status = 'ACTIVE'")
    List<EPromotionProgram> findActivePrograms(@Param("now") Instant now);
    
    @Query("SELECT pp FROM EPromotionProgram pp WHERE pp.endDate <= :now AND pp.status = 'ACTIVE'")
    List<EPromotionProgram> findExpiredActivePrograms(@Param("now") Instant now);
    
    @Query("SELECT pp FROM EPromotionProgram pp WHERE pp.startDate <= :now AND pp.status = 'DRAFT'")
    List<EPromotionProgram> findProgramsToActivate(@Param("now") Instant now);
    
    @Query("SELECT pp FROM EPromotionProgram pp WHERE pp.campaign.id = :campaignId")
    List<EPromotionProgram> findByCampaignId(@Param("campaignId") UUID campaignId);
    
    @Query("SELECT pp FROM EPromotionProgram pp WHERE pp.isStackable = true AND pp.status = 'ACTIVE'")
    List<EPromotionProgram> findStackableActivePrograms();
    
    @Query("SELECT COUNT(pp) FROM EPromotionProgram pp WHERE pp.status = :status")
    long countByStatus(@Param("status") ProgramStatus status);
}
