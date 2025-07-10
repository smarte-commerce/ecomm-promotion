package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.model.request.CustomerCart.CustomerCartWithShop;

import lombok.Builder;

@Builder
public record ApplyCampaignRequest(
    UUID campaignId,
    UUID customerId,
    CustomerCartWithShop customerCartWithShop,
    Long campaignVersion) implements AbstractModel {
} 
