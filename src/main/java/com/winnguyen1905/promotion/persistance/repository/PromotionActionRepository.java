package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EPromotionAction;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction.ActionType;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction.Target;

@Repository
public interface PromotionActionRepository extends JpaRepository<EPromotionAction, UUID>, JpaSpecificationExecutor<EPromotionAction> {
    
    List<EPromotionAction> findByProgramId(UUID programId);
    
    List<EPromotionAction> findByProgramIdAndActionType(UUID programId, ActionType actionType);
    
    List<EPromotionAction> findByProgramIdAndTarget(UUID programId, Target target);
    
    @Query("SELECT pa FROM EPromotionAction pa WHERE pa.program.id = :programId ORDER BY pa.createdAt ASC")
    List<EPromotionAction> findByProgramIdOrderByCreatedAt(@Param("programId") UUID programId);
    
    void deleteByProgramId(UUID programId);
    
    @Query("SELECT COUNT(pa) FROM EPromotionAction pa WHERE pa.program.id = :programId")
    long countByProgramId(@Param("programId") UUID programId);
} 
