package com.winnguyen1905.promotion.persistance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EPromotionAnalytics;

@Repository
public interface PromotionAnalyticsRepository extends JpaRepository<EPromotionAnalytics, UUID>, JpaSpecificationExecutor<EPromotionAnalytics> {
    
    List<EPromotionAnalytics> findByProgramId(UUID programId);
    
    Optional<EPromotionAnalytics> findByProgramIdAndDate(UUID programId, LocalDate date);
    
    List<EPromotionAnalytics> findByProgramIdAndDateBetween(UUID programId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT pa FROM EPromotionAnalytics pa WHERE pa.program.id = :programId ORDER BY pa.date DESC")
    List<EPromotionAnalytics> findByProgramIdOrderByDateDesc(@Param("programId") UUID programId);
    
    @Query("SELECT SUM(pa.totalRevenue) FROM EPromotionAnalytics pa WHERE pa.program.id = :programId AND pa.date BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByProgramAndDateRange(@Param("programId") UUID programId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(pa.totalDiscountGiven) FROM EPromotionAnalytics pa WHERE pa.program.id = :programId AND pa.date BETWEEN :startDate AND :endDate")
    Double getTotalDiscountGivenByProgramAndDateRange(@Param("programId") UUID programId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(pa.conversionRate) FROM EPromotionAnalytics pa WHERE pa.program.id = :programId AND pa.date BETWEEN :startDate AND :endDate")
    Double getAverageConversionRateByProgramAndDateRange(@Param("programId") UUID programId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(pa.roi) FROM EPromotionAnalytics pa WHERE pa.program.id = :programId AND pa.date BETWEEN :startDate AND :endDate")
    Double getAverageROIByProgramAndDateRange(@Param("programId") UUID programId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    void deleteByProgramId(UUID programId);
} 
