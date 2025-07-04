package com.winnguyen1905.promotion.core.service;

import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.request.CreatePromotionActionRequest;
import com.winnguyen1905.promotion.model.response.PromotionActionVm;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface PromotionActionService {
    
    void createPromotionAction(TAccountRequest accountRequest, CreatePromotionActionRequest request);
    
    PromotionActionVm getPromotionActionById(TAccountRequest accountRequest, UUID id);
    
    List<PromotionActionVm> getPromotionActionsByProgramId(TAccountRequest accountRequest, UUID programId);
    
    void updatePromotionAction(TAccountRequest accountRequest, UUID id, CreatePromotionActionRequest request);
    
    void deletePromotionAction(TAccountRequest accountRequest, UUID id);
    
    void deletePromotionActionsByProgramId(TAccountRequest accountRequest, UUID programId);
    
    Double calculateDiscountAmount(UUID actionId, Double orderAmount, List<UUID> productIds);
} 
