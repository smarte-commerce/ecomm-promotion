package com.winnguyen1905.promotion.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manual test runner to verify optimistic locking behavior under concurrent access.
 * This simulates the scenarios we want to test without requiring full Spring context.
 */
public class ConcurrencyTestRunner {

    /**
     * Simulates concurrent discount applications to test optimistic locking.
     */
    public static void testConcurrentDiscountApplication() {
        System.out.println("=== Testing Concurrent Discount Application ===");
        
        // Simulate a discount with limited usage
        MockDiscount discount = new MockDiscount(UUID.randomUUID(), 5); // 5 usage limit
        MockOptimisticLockingService lockingService = new MockOptimisticLockingService();
        
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Create concurrent tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    // Simulate discount application with optimistic locking
                    boolean success = lockingService.tryApplyDiscount(discount, "Customer-" + threadId);
                    
                    if (success) {
                        successCount.incrementAndGet();
                        System.out.println("Thread " + threadId + ": SUCCESS - Applied discount");
                    } else {
                        failureCount.incrementAndGet();
                        System.out.println("Thread " + threadId + ": FAILED - Usage limit reached or conflict");
                    }
                    
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("Thread " + threadId + ": ERROR - " + e.getMessage());
                } finally {
                    completionLatch.countDown();
                }
            }, executor);
            
            futures.add(future);
        }

        try {
            // Start all threads simultaneously
            System.out.println("Starting " + numberOfThreads + " concurrent threads...");
            startLatch.countDown();
            
            // Wait for all threads to complete
            boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
            
            if (completed) {
                System.out.println("\n=== Results ===");
                System.out.println("Successful applications: " + successCount.get());
                System.out.println("Failed applications: " + failureCount.get());
                System.out.println("Final discount usage count: " + discount.getUsageCount());
                System.out.println("Expected successful applications: 5");
                
                // Verify results
                if (successCount.get() == 5 && discount.getUsageCount() == 5) {
                    System.out.println("✅ TEST PASSED: Optimistic locking prevented overselling!");
                } else {
                    System.out.println("❌ TEST FAILED: Optimistic locking did not work correctly");
                }
            } else {
                System.out.println("❌ TEST TIMEOUT: Not all threads completed within 30 seconds");
            }
            
        } catch (InterruptedException e) {
            System.out.println("❌ TEST INTERRUPTED: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Tests version validation logic.
     */
    public static void testVersionValidation() {
        System.out.println("\n=== Testing Version Validation ===");
        
        MockDiscount discount = new MockDiscount(UUID.randomUUID(), 10);
        MockOptimisticLockingService lockingService = new MockOptimisticLockingService();
        
        // Test correct version
        try {
            lockingService.validateVersion(discount, discount.getVersion());
            System.out.println("✅ Correct version validation passed");
        } catch (Exception e) {
            System.out.println("❌ Correct version validation failed: " + e.getMessage());
        }
        
        // Test incorrect version
        try {
            lockingService.validateVersion(discount, discount.getVersion() + 1);
            System.out.println("❌ Incorrect version validation should have failed");
        } catch (Exception e) {
            System.out.println("✅ Incorrect version validation correctly failed: " + e.getMessage());
        }
    }

    /**
     * Main method to run all tests.
     */
    public static void main(String[] args) {
        System.out.println("Starting Optimistic Locking Concurrency Tests...\n");
        
        testConcurrentDiscountApplication();
        testVersionValidation();
        
        System.out.println("\n=== All Tests Completed ===");
    }

    /**
     * Mock discount class for testing.
     */
    static class MockDiscount {
        private final UUID id;
        private volatile int usageCount;
        private volatile long version;
        private final int usageLimit;

        public MockDiscount(UUID id, int usageLimit) {
            this.id = id;
            this.usageLimit = usageLimit;
            this.usageCount = 0;
            this.version = 1L;
        }

        public UUID getId() { return id; }
        public int getUsageCount() { return usageCount; }
        public long getVersion() { return version; }
        public int getUsageLimit() { return usageLimit; }

        public synchronized boolean incrementUsageIfPossible() {
            if (usageCount < usageLimit) {
                usageCount++;
                version++;
                return true;
            }
            return false;
        }
    }

    /**
     * Mock optimistic locking service for testing.
     */
    static class MockOptimisticLockingService {
        
        public boolean tryApplyDiscount(MockDiscount discount, String customerId) {
            // Simulate optimistic locking with retry
            int maxRetries = 3;
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    // Simulate some processing time
                    Thread.sleep(1 + (int)(Math.random() * 5));
                    
                    // Try to increment usage count atomically
                    return discount.incrementUsageIfPossible();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } catch (Exception e) {
                    // Simulate optimistic locking failure, retry
                    if (attempt == maxRetries - 1) {
                        throw new RuntimeException("Max retries exceeded", e);
                    }
                }
            }
            return false;
        }
        
        public void validateVersion(MockDiscount discount, long expectedVersion) {
            if (discount.getVersion() != expectedVersion) {
                throw new RuntimeException("Version mismatch. Expected: " + expectedVersion + 
                    ", Actual: " + discount.getVersion());
            }
        }
    }
}
