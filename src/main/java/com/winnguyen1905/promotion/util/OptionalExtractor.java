package com.winnguyen1905.promotion.util;

import java.util.Optional;
import java.util.UUID;

import com.winnguyen1905.promotion.config.SecurityUtils;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;

public class OptionalExtractor {
  public static <T> T extractFromResource(Optional<T> optional) {
    if (optional.isPresent() && optional.get() instanceof T t) return t;
    else throw new ResourceNotFoundException("Resource not found for optional extract !");
  }

  public static UUID extractUserId() {
    if(SecurityUtils.getCurrentUserId().isEmpty()) throw new ResourceNotFoundException("");
    return SecurityUtils.getCurrentUserId().get();
  }

}
