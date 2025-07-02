package com.winnguyen1905.promotion.persistance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;

@Repository
public interface DiscountUsageRepository extends JpaRepository<EDiscountUsage, UUID> {
    
    @Query("SELECT COUNT(du) FROM EDiscountUsage du WHERE du.customerId = :customerId AND du.discount.id = :discountId")
    int countByDiscountIdAndCustomerId(@Param("discountId") UUID discountId, @Param("customerId") UUID customerId);
    
    @Query("SELECT COUNT(du) FROM EDiscountUsage du WHERE du.customerId = :customerId AND du.program.id = :programId")
    int countByProgramIdAndCustomerId(@Param("programId") UUID programId, @Param("customerId") UUID customerId);
}
