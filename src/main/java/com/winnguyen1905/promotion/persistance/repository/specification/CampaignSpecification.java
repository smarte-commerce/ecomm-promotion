package com.winnguyen1905.promotion.persistance.repository.specification;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;

import com.winnguyen1905.promotion.model.request.SearchCampaignRequest;
import com.winnguyen1905.promotion.persistance.entity.ECampaign;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class CampaignSpecification {

  public static Specification<ECampaign> withFilter(SearchCampaignRequest request) {
    return (Root<ECampaign> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      Predicate predicate = cb.conjunction();

      if (request == null) {
        return predicate;
      }

      if (request.name() != null && !request.name().isBlank()) {
        predicate = cb.and(predicate,
            cb.like(cb.lower(root.get("name")), "%" + request.name().toLowerCase() + "%"));
      }

      if (request.status() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("status"), request.status()));
      }

      if (request.campaignType() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("campaignType"), request.campaignType()));
      }

      // Date range filtering
      Instant start = request.startDate();
      Instant end = request.endDate();
      if (start != null && end != null) {
        predicate = cb.and(predicate,
            cb.between(root.get("startDate"), start, end));
      } else if (start != null) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startDate"), start));
      } else if (end != null) {
        predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("endDate"), end));
      }

      if (request.createdBy() != null) {
        predicate = cb.and(predicate, cb.equal(root.get("createdBy"), request.createdBy()));
      }

      if (request.includeExpired() != null && !request.includeExpired()) {
        predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("endDate"), cb.currentTimestamp()));
      }

      return predicate;
    };
  }
} 
