package com.winnguyen1905.promotion.core.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.request.CreatePromotionRuleRequest;
import com.winnguyen1905.promotion.model.response.PromotionRuleVm;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface PromotionRuleService {

  void createPromotionRule(TAccountRequest accountRequest, CreatePromotionRuleRequest request);

  PromotionRuleVm getPromotionRuleById(TAccountRequest accountRequest, UUID id);

  List<PromotionRuleVm> getPromotionRulesByProgramId(TAccountRequest accountRequest, UUID programId);

  void updatePromotionRule(TAccountRequest accountRequest, UUID id, CreatePromotionRuleRequest request);

  void deletePromotionRule(TAccountRequest accountRequest, UUID id);

  void deletePromotionRulesByProgramId(TAccountRequest accountRequest, UUID programId);

  boolean evaluateRules(UUID programId, TAccountRequest accountRequest, Double orderAmount, List<UUID> productIds);

  List<PromotionRuleVm> getRequiredRules(UUID programId);
}
