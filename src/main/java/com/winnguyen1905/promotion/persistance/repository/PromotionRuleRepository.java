package com.winnguyen1905.promotion.persistance.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EPromotionRule;
import com.winnguyen1905.promotion.persistance.entity.EPromotionRule.RuleType;

@Repository
public interface PromotionRuleRepository extends JpaRepository<EPromotionRule, UUID>, JpaSpecificationExecutor<EPromotionRule> {
    
    List<EPromotionRule> findByProgramId(UUID programId);
    
    List<EPromotionRule> findByProgramIdAndRuleType(UUID programId, RuleType ruleType);
    
    List<EPromotionRule> findByProgramIdAndIsRequired(UUID programId, Boolean isRequired);
    
    @Query("SELECT pr FROM EPromotionRule pr WHERE pr.program.id = :programId ORDER BY pr.createdAt ASC")
    List<EPromotionRule> findByProgramIdOrderByCreatedAt(@Param("programId") UUID programId);
    
    @Query("SELECT pr FROM EPromotionRule pr WHERE pr.program.id = :programId AND pr.isRequired = true")
    List<EPromotionRule> findRequiredRulesByProgramId(@Param("programId") UUID programId);
    
    void deleteByProgramId(UUID programId);
    
    @Query("SELECT COUNT(pr) FROM EPromotionRule pr WHERE pr.program.id = :programId")
    long countByProgramId(@Param("programId") UUID programId);
    
    @Query("SELECT COUNT(pr) FROM EPromotionRule pr WHERE pr.program.id = :programId AND pr.isRequired = true")
    long countRequiredRulesByProgramId(@Param("programId") UUID programId);
} 
