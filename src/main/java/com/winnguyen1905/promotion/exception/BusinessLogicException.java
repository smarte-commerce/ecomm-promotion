package com.winnguyen1905.promotion.exception;

public class BusinessLogicException extends RuntimeException {
  public BusinessLogicException(String message) {
    super(message);
  }

  public BusinessLogicException(String message, Throwable cause) {
    super(message, cause);
  }
}
