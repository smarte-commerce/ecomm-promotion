package com.winnguyen1905.promotion.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.winnguyen1905.promotion.core.service.DistributedLockService.DistributedLockException;
import com.winnguyen1905.promotion.core.service.OptimisticLockingService.OptimisticLockingException;

/**
 * Global exception handler for optimistic locking and concurrency-related exceptions.
 * Provides user-friendly error messages and appropriate HTTP status codes.
 */
@RestControllerAdvice
public class OptimisticLockingExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockingExceptionHandler.class);

    /**
     * Handles OptimisticLockingFailureException from JPA.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        logger.warn("Optimistic locking failure: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .error("OPTIMISTIC_LOCK_FAILURE")
            .message("The discount information has been updated by another user. Please try again.")
            .details("This usually happens when multiple users try to apply the same discount simultaneously.")
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles custom OptimisticLockingException from our service layer.
     */
    @ExceptionHandler(OptimisticLockingException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingException(OptimisticLockingException ex) {
        logger.warn("Optimistic locking exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .error("DISCOUNT_CONCURRENCY_ERROR")
            .message("Unable to apply discount due to concurrent modifications. Please try again.")
            .details(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles DistributedLockException when unable to acquire distributed locks.
     */
    @ExceptionHandler(DistributedLockException.class)
    public ResponseEntity<ErrorResponse> handleDistributedLockException(DistributedLockException ex) {
        logger.warn("Distributed lock exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .error("DISCOUNT_BUSY")
            .message("The discount is currently being processed by another request. Please try again in a moment.")
            .details("This discount is temporarily locked due to high concurrent usage.")
            .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    /**
     * Error response structure for consistent API responses.
     */
    public static class ErrorResponse {
        private String error;
        private String message;
        private String details;
        private long timestamp;

        public ErrorResponse() {
            this.timestamp = System.currentTimeMillis();
        }

        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters and setters
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public static class ErrorResponseBuilder {
            private String error;
            private String message;
            private String details;

            public ErrorResponseBuilder error(String error) {
                this.error = error;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ErrorResponseBuilder details(String details) {
                this.details = details;
                return this;
            }

            public ErrorResponse build() {
                ErrorResponse response = new ErrorResponse();
                response.setError(this.error);
                response.setMessage(this.message);
                response.setDetails(this.details);
                return response;
            }
        }
    }
}
