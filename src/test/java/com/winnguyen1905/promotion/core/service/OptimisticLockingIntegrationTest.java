package com.winnguyen1905.promotion.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.core.service.DistributedLockService.DistributedLockException;
import com.winnguyen1905.promotion.core.service.OptimisticLockingService.OptimisticLockingException;
import com.winnguyen1905.promotion.model.request.CheckoutRequest;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

/**
 * Integration tests for optimistic locking functionality in discount application.
 * Tests concurrent access scenarios and verifies proper handling of race conditions.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OptimisticLockingIntegrationTest {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private OptimisticLockingService optimisticLockingService;

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountUsageRepository discountUsageRepository;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    private EDiscount testDiscount;
    private TAccountRequest testAccount;
    private CheckoutRequest testCheckoutRequest;

    @BeforeEach
    void setUp() {
        // Create test discount with limited usage
        testDiscount = EDiscount.builder()
            .id(UUID.randomUUID())
            .name("Test Discount")
            .discountCategory(DiscountCategory.PRODUCT)
            .discountType(DiscountType.PERCENTAGE)
            .value(20.0)
            .usageLimitTotal(5) // Limited to 5 uses
            .usageLimitPerCustomer(1) // One per customer
            .usageCount(0)
            .startDate(Instant.now())
            .endDate(Instant.now().plus(30, ChronoUnit.DAYS))
            .isActive(true)
            .creatorType(EDiscount.CreatorType.ADMIN)
            .creatorId(UUID.randomUUID())
            .build();

        testDiscount = discountRepository.save(testDiscount);

        testAccount = new TAccountRequest(UUID.randomUUID(), "test@example.com", null, null);

        testCheckoutRequest = CheckoutRequest.builder()
            .globalProductDiscountId(testDiscount.getId())
            .total(100.0)
            .build();

        // Mock Redis operations for distributed locking
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
            .thenReturn(true);
    }

    @Test
    void testConcurrentDiscountApplication_ShouldPreventOverselling() throws InterruptedException {
        // Given: A discount with limited usage (5 total uses)
        int numberOfThreads = 10;
        int expectedSuccessfulApplications = 5;
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When: Multiple threads try to apply the discount concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            UUID customerId = UUID.randomUUID(); // Different customer for each thread
            TAccountRequest account = new TAccountRequest(customerId, "customer" + i + "@example.com", null, null);
            
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    discountService.applyDiscountToShop(account, testCheckoutRequest);
                    return true;
                } catch (Exception e) {
                    // Expected for some threads due to usage limits
                    return false;
                } finally {
                    completionLatch.countDown();
                }
            }, executor);
            
            futures.add(future);
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all threads to complete
        assertTrue(completionLatch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");

        // Then: Only the expected number of applications should succeed
        long successfulApplications = futures.stream()
            .mapToLong(future -> {
                try {
                    return future.get() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();

        assertEquals(expectedSuccessfulApplications, successfulApplications, 
            "Should have exactly " + expectedSuccessfulApplications + " successful applications");

        // Verify final usage count
        EDiscount updatedDiscount = discountRepository.findById(testDiscount.getId()).orElseThrow();
        assertEquals(expectedSuccessfulApplications, updatedDiscount.getUsageCount(), 
            "Final usage count should match successful applications");

        executor.shutdown();
    }

    @Test
    void testOptimisticLockingWithVersionMismatch_ShouldThrowException() {
        // Given: A discount with a specific version
        Long originalVersion = testDiscount.getVersion();

        // When: We try to increment usage with an outdated version
        // First, simulate another process updating the discount
        testDiscount.setUsageCount(testDiscount.getUsageCount() + 1);
        discountRepository.save(testDiscount);

        // Then: Attempting to use the original version should fail
        assertThrows(OptimisticLockingException.class, () -> {
            optimisticLockingService.incrementUsageCount(testDiscount.getId(), originalVersion);
        }, "Should throw OptimisticLockingException for version mismatch");
    }

    @Test
    void testDistributedLockTimeout_ShouldThrowException() {
        // Given: A mock scenario where Redis lock acquisition fails
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
            .thenReturn(false); // Simulate lock acquisition failure

        // When & Then: Should throw DistributedLockException
        assertThrows(DistributedLockException.class, () -> {
            distributedLockService.executeWithLock(testDiscount.getId(), () -> {
                return "should not reach here";
            });
        }, "Should throw DistributedLockException when unable to acquire lock");
    }

    @Test
    void testSuccessfulOptimisticLockingRetry() {
        // Given: A discount that will be updated during the operation
        UUID discountId = testDiscount.getId();

        // When: We execute an operation that might face optimistic locking conflicts
        EDiscount result = optimisticLockingService.executeWithOptimisticLocking(discountId, discount -> {
            // Simulate some processing time
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Update usage count
            discount.setUsageCount(discount.getUsageCount() + 1);
            return discountRepository.save(discount);
        });

        // Then: Operation should succeed
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getUsageCount(), "Usage count should be incremented");
    }

    @Test
    void testUsageLimitValidation_ShouldPreventExceedingLimits() {
        // Given: A discount at its usage limit
        testDiscount.setUsageCount(testDiscount.getUsageLimitTotal());
        discountRepository.save(testDiscount);

        // When & Then: Attempting to apply should fail
        assertThrows(Exception.class, () -> {
            discountService.applyDiscountToShop(testAccount, testCheckoutRequest);
        }, "Should throw exception when usage limit is exceeded");
    }

    @Test
    void testPerCustomerUsageLimit_ShouldPreventMultipleUsage() {
        // Given: A customer who has already used the discount
        discountService.applyDiscountToShop(testAccount, testCheckoutRequest);

        // When & Then: Second application by same customer should fail
        assertThrows(Exception.class, () -> {
            discountService.applyDiscountToShop(testAccount, testCheckoutRequest);
        }, "Should throw exception when customer usage limit is exceeded");
    }

    @Test
    void testVersionValidation_ShouldSucceedWithCorrectVersion() {
        // Given: A discount with known version
        Long currentVersion = testDiscount.getVersion();

        // When & Then: Validation with correct version should succeed
        assertDoesNotThrow(() -> {
            optimisticLockingService.validateVersion(testDiscount.getId(), currentVersion);
        }, "Should not throw exception with correct version");
    }

    @Test
    void testVersionValidation_ShouldFailWithIncorrectVersion() {
        // Given: A discount with known version
        Long incorrectVersion = testDiscount.getVersion() + 1;

        // When & Then: Validation with incorrect version should fail
        assertThrows(OptimisticLockingException.class, () -> {
            optimisticLockingService.validateVersion(testDiscount.getId(), incorrectVersion);
        }, "Should throw exception with incorrect version");
    }
}
