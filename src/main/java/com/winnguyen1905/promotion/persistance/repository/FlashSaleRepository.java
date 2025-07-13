package com.winnguyen1905.promotion.persistance.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EFlashSale;
import com.winnguyen1905.promotion.persistance.entity.EFlashSale.Status;

@Repository
public interface FlashSaleRepository extends JpaRepository<EFlashSale, UUID>, JpaSpecificationExecutor<EFlashSale> {
    
    Optional<EFlashSale> findByProgramId(UUID programId);
    
    List<EFlashSale> findByStatus(Status status);
    
    List<EFlashSale> findByCountdownStartBeforeAndCountdownEndAfter(Instant startTime, Instant endTime);
    
    @Query("SELECT f FROM EFlashSale f WHERE f.countdownStart <= :now AND f.countdownEnd >= :now AND f.status = :status")
    List<EFlashSale> findActiveFlashSales(@Param("now") Instant now, @Param("status") Status status);
    
    @Query("SELECT f FROM EFlashSale f WHERE f.countdownStart <= :now AND f.status = 'UPCOMING'")
    List<EFlashSale> findFlashSalesToStart(@Param("now") Instant now);
    
    @Query("SELECT f FROM EFlashSale f WHERE f.countdownEnd <= :now AND f.status = 'LIVE'")
    List<EFlashSale> findFlashSalesToEnd(@Param("now") Instant now);
    
    @Modifying
    @Query("UPDATE EFlashSale f SET f.soldQuantity = f.soldQuantity + :quantity WHERE f.id = :id")
    int updateSoldQuantity(@Param("id") UUID id, @Param("quantity") Integer quantity);
    
    @Query("SELECT f FROM EFlashSale f WHERE f.isNotifyEnabled = true AND f.notificationSent = false AND f.countdownStart <= :notifyTime")
    List<EFlashSale> findFlashSalesForNotification(@Param("notifyTime") Instant notifyTime);
} 
