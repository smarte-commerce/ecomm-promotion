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

import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission.PaymentStatus;

@Repository
public interface PromotionCommissionRepository extends JpaRepository<EPromotionCommission, UUID>, JpaSpecificationExecutor<EPromotionCommission> {
    
    List<EPromotionCommission> findByProgramId(UUID programId);
    
    List<EPromotionCommission> findByVendorId(UUID vendorId);
    
    Page<EPromotionCommission> findByVendorIdAndPaymentStatus(UUID vendorId, PaymentStatus paymentStatus, Pageable pageable);
    
    List<EPromotionCommission> findByOrderId(UUID orderId);
    
    List<EPromotionCommission> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT pc FROM EPromotionCommission pc WHERE pc.vendor.id = :vendorId AND pc.createdAt BETWEEN :startDate AND :endDate")
    List<EPromotionCommission> findByVendorIdAndDateRange(@Param("vendorId") UUID vendorId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
    
    @Query("SELECT SUM(pc.commissionAmount) FROM EPromotionCommission pc WHERE pc.vendorId = :vendorId AND pc.paymentStatus = :paymentStatus")
    Double getTotalCommissionByVendorAndStatus(@Param("vendorId") UUID vendorId, @Param("paymentStatus") PaymentStatus paymentStatus);
    
    @Query("SELECT SUM(pc.vendorContribution) FROM EPromotionCommission pc WHERE pc.program.id = :programId")
    Double getTotalVendorContributionByProgram(@Param("programId") UUID programId);
    
    @Query("SELECT SUM(pc.platformContribution) FROM EPromotionCommission pc WHERE pc.program.id = :programId")
    Double getTotalPlatformContributionByProgram(@Param("programId") UUID programId);
    
    @Query("SELECT pc FROM EPromotionCommission pc WHERE pc.paymentStatus = 'PENDING' AND pc.createdAt <= :cutoffDate")
    List<EPromotionCommission> findPendingCommissionsOlderThan(@Param("cutoffDate") Instant cutoffDate);
} 
