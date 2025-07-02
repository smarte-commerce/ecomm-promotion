package com.winnguyen1905.promotion.core.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.OptimisticLockingFailureException;

import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.core.service.OptimisticLockingService.OptimisticLockingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Unit tests for OptimisticLockingService.
 * Tests the core optimistic locking logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
public class OptimisticLockingServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private OptimisticLockingService optimisticLockingService;

    private EDiscount testDiscount;
    private UUID discountId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        discountId = UUID.randomUUID();
        testDiscount = EDiscount.builder()
            .id(discountId)
            .name("Test Discount")
            .usageCount(0)
            .version(1L)
            .build();
    }

    @Test
    public void testExecuteWithOptimisticLocking_Success() {
        // Given
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(testDiscount));
        when(distributedLockService.executeWithLock(eq(discountId), any())).thenAnswer(invocation -> {
            return invocation.getArgument(1, java.util.function.Supplier.class).get();
        });

        // When
        String result = optimisticLockingService.executeWithOptimisticLocking(discountId, discount -> {
            return "success";
        });

        // Then
        assert "success".equals(result);
        verify(discountRepository).findById(discountId);
        verify(distributedLockService).executeWithLock(eq(discountId), any());
    }

    @Test
    public void testExecuteWithOptimisticLocking_RetryOnOptimisticLockingFailure() {
        // Given
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(testDiscount));
        when(distributedLockService.executeWithLock(eq(discountId), any())).thenAnswer(invocation -> {
            return invocation.getArgument(1, java.util.function.Supplier.class).get();
        });

        // Mock the function to throw OptimisticLockingFailureException on first call, succeed on second
        java.util.function.Function<EDiscount, String> mockFunction = mock(java.util.function.Function.class);
        try {
            when(mockFunction.apply(any(EDiscount.class)))
                .thenThrow(new OptimisticLockingFailureException("Version conflict"))
                .thenReturn("success after retry");
        } catch (Exception e) {
            // Handle checked exception from mock setup
        }

        // When
        String result = optimisticLockingService.executeWithOptimisticLocking(discountId, mockFunction);

        // Then
        assert "success after retry".equals(result);
        verify(discountRepository, times(2)).findById(discountId);
    }

    @Test
    public void testIncrementUsageCount_Success() {
        // Given
        Long expectedVersion = 1L;
        EDiscount updatedDiscount = EDiscount.builder()
            .id(discountId)
            .name("Test Discount")
            .usageCount(1)
            .version(2L)
            .build();

        when(discountRepository.findById(discountId)).thenReturn(Optional.of(testDiscount));
        when(discountRepository.save(any(EDiscount.class))).thenReturn(updatedDiscount);
        when(distributedLockService.executeWithLock(eq(discountId), any())).thenAnswer(invocation -> {
            return invocation.getArgument(1, java.util.function.Supplier.class).get();
        });

        // When
        EDiscount result = optimisticLockingService.incrementUsageCount(discountId, expectedVersion);

        // Then
        assert result != null;
        assert result.getUsageCount() == 1;
        assert result.getVersion() == 2L;
        verify(discountRepository).save(any(EDiscount.class));
    }

    @Test
    public void testIncrementUsageCount_VersionMismatch() {
        // Given
        Long wrongVersion = 999L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(testDiscount));
        when(distributedLockService.executeWithLock(eq(discountId), any())).thenAnswer(invocation -> {
            return invocation.getArgument(1, java.util.function.Supplier.class).get();
        });

        // When & Then
        try {
            optimisticLockingService.incrementUsageCount(discountId, wrongVersion);
            assert false : "Should have thrown OptimisticLockingException";
        } catch (OptimisticLockingException e) {
            assert e.getMessage().contains("Version mismatch");
        }
    }

    @Test
    public void testValidateVersion_Success() {
        // Given
        Long correctVersion = 1L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(testDiscount));

        // When & Then - should not throw exception
        try {
            optimisticLockingService.validateVersion(discountId, correctVersion);
        } catch (Exception e) {
            assert false : "Should not throw exception with correct version";
        }
    }

    @Test
    public void testValidateVersion_Failure() {
        // Given
        Long wrongVersion = 999L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(testDiscount));

        // When & Then
        try {
            optimisticLockingService.validateVersion(discountId, wrongVersion);
            assert false : "Should have thrown OptimisticLockingException";
        } catch (OptimisticLockingException e) {
            assert e.getMessage().contains("Version mismatch");
        }
    }

    @Test
    public void testValidateVersion_DiscountNotFound() {
        // Given
        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        // When & Then
        try {
            optimisticLockingService.validateVersion(discountId, 1L);
            assert false : "Should have thrown exception for non-existent discount";
        } catch (RuntimeException e) {
            assert e.getMessage().contains("not found");
        }
    }
}
