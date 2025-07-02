package com.winnguyen1905.promotion.core.service;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for handling optimistic locking operations with retry logic.
 * Provides thread-safe operations for discount modifications with proper
 * version checking and conflict resolution.
 */
@Service
@RequiredArgsConstructor
public class OptimisticLockingService {

    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockingService.class);
    
    private final DiscountRepository discountRepository;
    private final DistributedLockService distributedLockService;
    
    // Maximum number of retry attempts for optimistic locking conflicts
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Executes a discount modification operation with optimistic locking and retry logic.
     * Combines distributed locking with JPA optimistic locking for maximum safety.
     * 
     * @param discountId The ID of the discount to modify
     * @param operation The operation to perform on the discount
     * @param <T> The return type of the operation
     * @return The result of the operation
     * @throws OptimisticLockingException if all retry attempts fail
     */
    public <T> T executeWithOptimisticLocking(UUID discountId, Function<EDiscount, T> operation) {
        return distributedLockService.executeWithLock(discountId, () -> {
            return executeWithRetry(discountId, operation);
        });
    }

    /**
     * Executes an operation with retry logic for optimistic locking conflicts.
     */
    private <T> T executeWithRetry(UUID discountId, Function<EDiscount, T> operation) {
        OptimisticLockingFailureException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                // Fetch the latest version of the discount
                EDiscount discount = discountRepository.findById(discountId)
                    .orElseThrow(() -> new IllegalArgumentException("Discount not found: " + discountId));
                
                logger.debug("Attempting operation on discount {} (version: {}, attempt: {})", 
                    discountId, discount.getVersion(), attempt);
                
                // Execute the operation
                T result = operation.apply(discount);
                
                logger.debug("Successfully completed operation on discount {} after {} attempt(s)", 
                    discountId, attempt);
                
                return result;
                
            } catch (OptimisticLockingFailureException e) {
                lastException = e;
                logger.warn("Optimistic locking conflict on discount {} (attempt {}/{}): {}", 
                    discountId, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    // Wait a bit before retrying with exponential backoff
                    try {
                        Thread.sleep(50 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new OptimisticLockingException(
                            "Operation interrupted while retrying optimistic lock for discount: " + discountId, ie);
                    }
                }
            }
        }
        
        // All retry attempts failed
        throw new OptimisticLockingException(
            "Failed to complete operation on discount " + discountId + " after " + MAX_RETRY_ATTEMPTS + " attempts", 
            lastException);
    }

    /**
     * Validates that the provided version matches the current entity version.
     * Used for explicit version checking before operations.
     * 
     * @param discountId The discount ID to check
     * @param expectedVersion The expected version
     * @throws OptimisticLockingException if versions don't match
     */
    public void validateVersion(UUID discountId, Long expectedVersion) {
        if (expectedVersion == null) {
            logger.debug("No version provided for discount {}, skipping version validation", discountId);
            return;
        }
        
        EDiscount discount = discountRepository.findById(discountId)
            .orElseThrow(() -> new IllegalArgumentException("Discount not found: " + discountId));
        
        if (!expectedVersion.equals(discount.getVersion())) {
            throw new OptimisticLockingException(
                String.format("Version mismatch for discount %s. Expected: %d, Actual: %d", 
                    discountId, expectedVersion, discount.getVersion()));
        }
    }

    /**
     * Safely increments the usage count of a discount with optimistic locking.
     * 
     * @param discountId The discount to update
     * @param expectedVersion The expected version (optional)
     * @return The updated discount
     */
    public EDiscount incrementUsageCount(UUID discountId, Long expectedVersion) {
        return executeWithOptimisticLocking(discountId, discount -> {
            // Validate version if provided
            if (expectedVersion != null && !expectedVersion.equals(discount.getVersion())) {
                throw new OptimisticLockingException(
                    String.format("Version mismatch for discount %s. Expected: %d, Actual: %d", 
                        discountId, expectedVersion, discount.getVersion()));
            }
            
            // Increment usage count
            discount.setUsageCount(discount.getUsageCount() + 1);
            
            // Save and return the updated discount
            return discountRepository.save(discount);
        });
    }

    /**
     * Exception thrown when optimistic locking operations fail.
     */
    public static class OptimisticLockingException extends RuntimeException {
        public OptimisticLockingException(String message) {
            super(message);
        }
        
        public OptimisticLockingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


