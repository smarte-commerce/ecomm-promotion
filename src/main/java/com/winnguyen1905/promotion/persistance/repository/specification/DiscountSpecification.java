package com.winnguyen1905.promotion.persistance.repository.specification;

import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.model.request.SearchDiscountRequest;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class DiscountSpecification {

  public static Specification<EDiscount> withFilter(SearchDiscountRequest request) {
    return (Root<EDiscount> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      Predicate predicate = cb.conjunction();

      if (request.code() != null) {
        predicate = cb.and(predicate, cb.like(cb.lower(root.get("code")), "%" + request.code().toLowerCase() + "%"));
      }

      if (request.isActive() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("isActive"), request.isActive()));
      }

      if (request.minDiscountValue() != null) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("value"), request.minDiscountValue()));
      }

      if (request.maxDiscountValue() != null) {
        predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("value"), request.maxDiscountValue()));
      }

      if (request.minOrderValue() != null) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("minOrderValue"), request.minOrderValue()));
      }

      if (request.maxOrderValue() != null) {
        predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("minOrderValue"), request.maxOrderValue()));
      }

      if (request.startDate() != null) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), request.startDate()));
      }

      if (request.endDate() != null) {
        predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("endDate"), request.endDate()));
      }

      if (request.shopId() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("shopId"), request.shopId()));
      }

      if (request.discountType() != null) {
        try {
          DiscountType type = DiscountType.valueOf(request.discountType());
          predicate = cb.and(predicate, cb.equal(root.get("discountType"), type));
        } catch (IllegalArgumentException ignored) {
        }
      }

      if (request.minUsageLimit() != null) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("usageLimit"), request.minUsageLimit()));
      }

      if (request.maxUsageLimit() != null) {
        predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("usageLimit"), request.maxUsageLimit()));
      }

      if (request.includeExpired() != null && !request.includeExpired()) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("endDate"), cb.currentTimestamp()));
      }

      if (request.productIds() != null && !request.productIds().isEmpty()) {
        Join<EDiscount, EProductDiscount> productJoin = root.join("productDiscounts", JoinType.LEFT);
        predicate = cb.and(predicate, productJoin.get("productId").in(request.productIds()));
        query.distinct(true);
      }

      if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
        Join<EDiscount, String> categoryJoin = root.join("categoryNames", JoinType.LEFT);
        // assuming your category UUIDs are stored as String in categoryNames
        predicate = cb.and(predicate, categoryJoin.in(request.categoryIds().stream().map(UUID::toString).toList()));
        query.distinct(true);
      }

      return predicate;
    };
  }
}
