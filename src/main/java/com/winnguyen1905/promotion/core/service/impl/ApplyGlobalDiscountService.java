package com.winnguyen1905.promotion.core.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.winnguyen1905.promotion.core.feign.CartServiceClient;
import com.winnguyen1905.promotion.core.model.CustomerCart;
import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest.ApplyGlobalDiscount;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest.ApplyShopDiscount;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.core.service.AbstractDiscountService;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EShopPromotion;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;

@Service
public class ApplyGlobalDiscountService extends AbstractDiscountService<ApplyGlobalDiscount> {

  public ApplyGlobalDiscountService(CartServiceClient cartServiceClient, DiscountRepository discountRepository,
      UserDiscountRepository userDiscountRepository, ShopPromotionRepository shopPromotionRepository,
      DiscountUsageRepository discountUsageRepository) {
    super(cartServiceClient, discountRepository, userDiscountRepository, shopPromotionRepository,
        discountUsageRepository);
  }

  @Override
  protected ApplyDiscountResponse calculatePriceAfterApplyDiscount(EDiscount discount, ApplyGlobalDiscount customerCart) {
    validateDiscountUsableShopCondition(customerCart.s, discount);
    List<CustomerCartWithShop> shopPromotions = getValidatedCustomerCartWithShopPromotion(customerCart, discount);
    
  }

  private List<CustomerCartWithShop> getValidatedCustomerCartWithShopPromotion(CustomerCart customerCart, EDiscount discount) {
    List<UUID> shopPromotionIdsTemp = customerCart.cartByShops().stream().map(CustomerCartWithShop::shopId).collect(Collectors.toList());
    List<UUID> shopPromotionIds = this.shopPromotionRepository.findAllByShopIdsAndPromotionIdAndIsVerifiedTrue(shopPromotionIdsTemp, discount.getPromotion().getId());
    List<CustomerCartWithShop> customerCartWithShopList = customerCart.cartByShops().stream()
        .filter(item -> shopPromotionIds.contains(item.shopId()))
        .collect(Collectors.toList());

    if (customerCartWithShopList.isEmpty()) {
      throw new ResourceNotFoundException("Shop is not in promotion of ecommerce");
    }

    return customerCartWithShopList;
  }

  @Override
  protected void validateDiscountUsableShopCondition(UUID shopId, EDiscount discount) {
    if (!discount.getShopId().equals(shopId)) {
      throw new ResourceNotFoundException("Discount is not for this shop");
    }
    if (!discount.getScope().equals(EDiscount.Scope.SHOP)) {
      throw new ResourceNotFoundException("Discount is not for this shop");
    }
  }

  // private void validateDiscountUsableShopCondition(UUID shopId, EDiscount discount) {
  //   if (discount.getScope().equals(EDiscount.Scope.SHOP) || discount.getShopId() != null) {
  //     throw new ResourceNotFoundException("Discount is not for this shop");
  //   }
    
  //   if (discount.getScope().equals(EDiscount.Scope.GLOBAL)) {
  //     EShopPromotion shopPromotion = this.shopPromotionRepository
  //         .findByShopIdAndPromotionId(shopId, discount.getPromotion().getId())
  //         .orElseThrow(() -> new ResourceNotFoundException("Shop is not in promotion of ecommerce"));
  //     if (!shopPromotion.getIsVerified()) {
  //       throw new ResourceNotFoundException("Shop is not in promotion of ecommerce");
  //     }
  //   }
  // }
}
