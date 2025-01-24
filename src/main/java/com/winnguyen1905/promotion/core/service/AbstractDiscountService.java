package com.winnguyen1905.promotion.core.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.promotion.common.ApplyDiscountStatus;
import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.core.feign.CartServiceClient;
import com.winnguyen1905.promotion.core.model.CustomerCart;
import com.winnguyen1905.promotion.core.model.Discount;
import com.winnguyen1905.promotion.core.model.Product;
import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest.ApplyGlobalDiscount;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest.ApplyShopDiscount;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.core.service.impl.ApplyGlobalDiscountService;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EShopPromotion;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public abstract class AbstractDiscountService<T extends ApplyDiscountRequest> implements DiscountService {

  protected final CartServiceClient cartServiceClient;
  protected final DiscountRepository discountRepository;
  protected final UserDiscountRepository userDiscountRepository;
  protected final ShopPromotionRepository shopPromotionRepository;
  protected final DiscountUsageRepository discountUsageRepository;

  @Override
  public void addDiscount(UUID userId, AddDiscountRequest addDiscountRequest) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'addDiscount'");
  }

  @Override
  public void updateDiscount(UUID userId, UpdateDiscountRequest updateDiscountRequest) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'updateDiscount'");
  }

  protected abstract void validateDiscountUsableShopCondition(EDiscount discount, UUID shopId);
  
  protected abstract ApplyDiscountResponse calculatePriceAfterApplyDiscount(EDiscount discount, T applyDiscountRequest);

  @Override
  public ApplyDiscountResponse applyDiscountForCart(UUID customerId, ApplyDiscountRequest applyDiscountRequest) {
    EDiscount discount = getValidatedDiscount(customerId, applyDiscountRequest.discountId());
    validateDiscountUsableCustomerCondition(customerId, discount);
    return caculatePriceFactory(discount, applyDiscountRequest);
  }

  public ApplyDiscountResponse applyDiscountForOrder(UUID customerId, ApplyDiscountRequest applyDiscountRequest) {
    // EDiscount discount = getValidatedDiscount(customerId,
    // applyDiscountRequest.discountId());
    // CustomerCart customerCart = getValidatedCustomerCart(customerId, discount);
    // validateDiscountUsableCustomerCondition(customerId, discount);
    // return calculatePriceAfterApplyDiscount(customerCart, discount);
    return null;
  }

  private ApplyDiscountResponse caculatePriceFactory(EDiscount discount, ApplyDiscountRequest applyDiscountRequest) {
    if (applyDiscountRequest instanceof ApplyShopDiscount applyShopDiscount)
      return calculatePriceAfterApplyDiscount(discount, (T) applyShopDiscount);
    return calculatePriceAfterApplyDiscount(discount, (T) applyDiscountRequest);
  }

  private EDiscount getValidatedDiscount(UUID customerId, UUID discountId) {
    EUserDiscount userDiscount = userDiscountRepository
        .findByCustomerIdAndDiscountId(customerId, discountId)
        .orElseThrow(() -> new EntityNotFoundException("User discount not found"));

    return Optional.ofNullable(userDiscount.getDiscount())
        .orElseThrow(() -> new EntityNotFoundException("Discount is not found"));
  }

  // private CustomerCart getValidatedCustomerCart(UUID customerId, EDiscount discount) {
  //   return Optional.ofNullable(cartServiceClient.getCustomerCartDetail(customerId).getBody())
  //       .orElseThrow(() -> new ResourceNotFoundException("Not found cart"));
  // }
  private void validateDiscountUsableCustomerCondition(UUID customerId, EDiscount discount) {
    if (discount.getUsageCount() >= discount.getUsageLimit()) {
      throw new ResourceNotFoundException("Discount is expired");
    }
    if (discount.getStartDate().isAfter(Instant.now()) || discount.getEndDate().isBefore(Instant.now())) {
      throw new ResourceNotFoundException("Discount is expired");
    }
    if (discount.getAppliesTo().equals(ApplyDiscountType.SPECIFIC)) {
      throw new ResourceNotFoundException("Products are not found");
    }
    int totalUsage = this.discountUsageRepository.countByDiscountIdAndCustomerId(customerId, discount.getId());
    if (totalUsage >= discount.getLimitUsagePerCutomer()) {
      throw new ResourceNotFoundException("Discount is expired");
    }
  }
}
