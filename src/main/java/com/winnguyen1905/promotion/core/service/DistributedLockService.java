package com.winnguyen1905.promotion.core.service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Redis-based distributed lock service for ensuring thread-safe operations
 * across multiple service instances in a microservice environment.
 */
@Service
@RequiredArgsConstructor
public class DistributedLockService {

  private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);

  private final RedisTemplate<String, String> redisTemplate;

  // Default lock timeout to prevent deadlocks
  private static final Duration DEFAULT_LOCK_TIMEOUT = Duration.ofSeconds(30);
  private static final Duration DEFAULT_WAIT_TIMEOUT = Duration.ofSeconds(5);

  // Lua script for atomic lock release
  private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
      "    return redis.call('del', KEYS[1]) " +
      "else " +
      "    return 0 " +
      "end";

  /**
   * Executes a function with a distributed lock for discount operations.
   * 
   * @param discountId The discount ID to lock on
   * @param operation  The operation to execute while holding the lock
   * @param <T>        The return type of the operation
   * @return The result of the operation
   * @throws DistributedLockException if unable to acquire lock
   */
  public <T> T executeWithLock(UUID discountId, Supplier<T> operation) {
    return executeWithLock(discountId, operation, DEFAULT_LOCK_TIMEOUT, DEFAULT_WAIT_TIMEOUT);
  }

  /**
   * Executes a function with a distributed lock with custom timeouts.
   * 
   * @param discountId  The discount ID to lock on
   * @param operation   The operation to execute while holding the lock
   * @param lockTimeout How long to hold the lock before auto-release
   * @param waitTimeout How long to wait to acquire the lock
   * @param <T>         The return type of the operation
   * @return The result of the operation
   * @throws DistributedLockException if unable to acquire lock
   */
  public <T> T executeWithLock(UUID discountId, Supplier<T> operation,
      Duration lockTimeout, Duration waitTimeout) {
    String lockKey = generateLockKey(discountId);
    String lockValue = generateLockValue();

    try {
      if (acquireLock(lockKey, lockValue, lockTimeout, waitTimeout)) {
        logger.debug("Acquired lock for discount: {}", discountId);
        return operation.get();
      } else {
        throw new DistributedLockException(
            "Failed to acquire lock for discount: " + discountId + " within " + waitTimeout);
      }
    } finally {
      releaseLock(lockKey, lockValue);
      logger.debug("Released lock for discount: {}", discountId);
    }
  }

  /**
   * Attempts to acquire a distributed lock.
   */
  private boolean acquireLock(String lockKey, String lockValue, Duration lockTimeout, Duration waitTimeout) {
    long waitTimeoutMs = waitTimeout.toMillis();
    long lockTimeoutSeconds = lockTimeout.getSeconds();
    long startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < waitTimeoutMs) {
      Boolean acquired = redisTemplate.opsForValue()
          .setIfAbsent(lockKey, lockValue, lockTimeoutSeconds, TimeUnit.SECONDS);

      if (Boolean.TRUE.equals(acquired)) {
        return true;
      }

      // Wait a bit before retrying
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
      }
    }

    return false;
  }

  /**
   * Releases a distributed lock using Lua script for atomicity.
   */
  private void releaseLock(String lockKey, String lockValue) {
    try {
      DefaultRedisScript<Long> script = new DefaultRedisScript<>();
      script.setScriptText(UNLOCK_SCRIPT);
      script.setResultType(Long.class);

      redisTemplate.execute(script, java.util.List.of(lockKey), lockValue);
    } catch (Exception e) {
      logger.warn("Failed to release lock: {}", lockKey, e);
    }
  }

  /**
   * Generates a unique lock key for a discount.
   */
  private String generateLockKey(UUID discountId) {
    return "discount:lock:" + discountId.toString();
  }

  /**
   * Generates a unique lock value to ensure only the lock holder can release it.
   */
  private String generateLockValue() {
    return Thread.currentThread().getName() + ":" + System.currentTimeMillis() + ":" + UUID.randomUUID();
  }

  /**
   * Exception thrown when unable to acquire a distributed lock.
   */
  public static class DistributedLockException extends RuntimeException {
    public DistributedLockException(String message) {
      super(message);
    }

    public DistributedLockException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
