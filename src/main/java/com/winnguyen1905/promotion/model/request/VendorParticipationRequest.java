package com.winnguyen1905.promotion.model.request;

import java.util.UUID;

import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.ParticipationType;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.ProductSelection;

import jakarta.validation.constraints.NotNull;

public record VendorParticipationRequest(
    @NotNull UUID vendorId,
    @NotNull UUID programId,
    ParticipationType participationType,
    @NotNull Double vendorContributionRate,
    @NotNull Double expectedDiscountRate,
    Double minDiscountAmount,
    Double maxDiscountAmount,
    ProductSelection productSelection,
    @NotNull Boolean acceptedTerms
) implements AbstractModel {
} 
