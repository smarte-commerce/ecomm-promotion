package com.winnguyen1905.promotion.persistance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;

@Repository
public interface PromotionProgramRepository extends JpaRepository<EPromotionProgram, UUID> {
    // Custom query methods can be added here if needed
}
