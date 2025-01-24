package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.core.feign.CartServiceClient;
import com.winnguyen1905.promotion.core.model.CustomerCart;
import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest.ApplyShopDiscount;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.core.service.AbstractDiscountService;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;

@Service
public class ApplyShopDiscountService extends AbstractDiscountService<ApplyShopDiscount> {

  public ApplyShopDiscountService(CartServiceClient cartServiceClient, DiscountRepository discountRepository,
      UserDiscountRepository userDiscountRepository, ShopPromotionRepository shopPromotionRepository,
      DiscountUsageRepository discountUsageRepository) {
    super(cartServiceClient, discountRepository, userDiscountRepository, shopPromotionRepository,
        discountUsageRepository);
  }

  @Override
  protected void validateDiscountUsableShopCondition(EDiscount discount, UUID shopId) {
    if (!discount.getShopId().equals(shopId)) {
      throw new ResourceNotFoundException("Discount is not for this shop");
    }
    if (!discount.getScope().equals(EDiscount.Scope.SHOP)) {
      throw new ResourceNotFoundException("Discount is not for this shop");
    }
  }

  @Override
  public ApplyDiscountResponse calculatePriceAfterApplyDiscount(EDiscount discount, ApplyShopDiscount applyShopDiscount) {
    CustomerCart.CustomerCartWithShop customerCartWithShop = Optional.ofNullable(applyShopDiscount.customerCart())
        .orElseThrow(() -> new ResourceNotFoundException("Not found shop of discount in customer cart"));

    validateDiscountUsableShopCondition(discount, customerCartWithShop.shopId());
    double totalAmountReduced = calculateDiscountAmount(discount, customerCartWithShop);

    PriceStatisticsResponse updatedPriceStatistic = PriceStatisticsResponse.builder()
        .totalPrice(customerCartWithShop.priceStatistic().totalPrice())
        .totalShipPrice(customerCartWithShop.priceStatistic().totalShipPrice())
        .totalDiscountVoucher(customerCartWithShop.priceStatistic().totalDiscountVoucher())
        .amountShipReduced(customerCartWithShop.priceStatistic().amountShipReduced())
        .amountProductReduced(totalAmountReduced)
        .finalPrice(customerCartWithShop.priceStatistic().finalPrice() - totalAmountReduced)
        .build();

    return ApplyDiscountResponse.builder()
        .shopId(discount.getShopId())
        .discountId(discount.getId())
        .priceStatisticsResponse(updatedPriceStatistic).build();
  }

  private double calculateDiscountAmount(EDiscount discount, CustomerCartWithShop cartWithShop) {
    if (discount.getScope().equals(EDiscount.Scope.SHOP) &&
        discount.getAppliesTo().equals(ApplyDiscountType.SPECIFIC)) {
      return calculateSpecificProductsDiscount(discount, cartWithShop);
    }
    return calculateGlobalDiscount(discount, cartWithShop);
  }

  private double calculateSpecificProductsDiscount(EDiscount discount, CustomerCartWithShop cartWithShop) {
    List<UUID> productIds = Optional
        .ofNullable(discount.getProductDiscounts().stream()
            .map(EProductDiscount::getProductId)
            .collect(Collectors.toList()))
        .orElseThrow(() -> new ResourceNotFoundException("No products found for specific discount"));

    double totalPromotionPrice = cartWithShop.cartItems().stream()
        .filter(cartItem -> productIds.contains(cartItem.productVariantId()))
        .mapToDouble(cartItem -> cartItem.price())
        .sum();

    validateMinimumOrderValue(totalPromotionPrice, discount.getMinOrderValue());
    return calculateFinalDiscount(totalPromotionPrice, discount);
  }

  private double calculateGlobalDiscount(EDiscount discount, CustomerCartWithShop cartWithShop) {
    double cartTotal = cartWithShop.priceStatistic().finalPrice();
    validateMinimumOrderValue(cartTotal, discount.getMinOrderValue());
    return calculateFinalDiscount(cartTotal, discount);
  }

  private void validateMinimumOrderValue(double total, double minRequired) {
    if (total < minRequired) {
      throw new ResourceNotFoundException("Total price of products insufficient");
    }
  }

  private double calculateFinalDiscount(double amount, EDiscount discount) {
    return discount.getDiscountType().equals(DiscountType.PERCENTAGE)
        ? amount * discount.getValue() / 100
        : discount.getValue();
  }
}
