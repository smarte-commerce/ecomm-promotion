package com.winnguyen1905.promotion.core.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.core.feign.CartServiceClient;
import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.core.service.DiscountServiceImpl;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;

import lombok.RequiredArgsConstructor;

@Service
public final class ApplyDiscountService extends DiscountServiceImpl {

  public ApplyDiscountService(CartServiceClient cartServiceClient, DiscountRepository discountRepository,
      UserDiscountRepository userDiscountRepository, ShopPromotionRepository shopPromotionRepository,
      DiscountUsageRepository discountUsageRepository) {
    super(cartServiceClient, discountRepository, userDiscountRepository, shopPromotionRepository,
        discountUsageRepository);

  }

  @Override
  protected PriceStatisticsResponse calculatePriceAfterApplyDiscountForCart(EDiscount discount, ApplyDiscountRequest applyDiscountRequest) {
    CustomerCartWithShop cart = validateAndGetCart(applyDiscountRequest, discount);
    validateDiscountUsableShopCondition(discount, applyDiscountRequest.shopId());

    double promotionalTotal = calculateAndValidatePromotionalTotal(discount, cart);
    double amountShipReduced = calculateAmoundShipReduced(discount, promotionalTotal);
    double amountProductReduced = calculateAmoundProductReduced(discount, promotionalTotal);

    return PriceStatisticsResponse.builder()
        .totalProductPrice(cart.priceStatistic().totalProductPrice())
        .totalShipPrice(cart.priceStatistic().totalShipPrice())
        .amountProductReduced(amountProductReduced)
        .amountShipReduced(amountShipReduced)
        .finalPrice(cart.priceStatistic().finalPrice() - amountProductReduced - amountShipReduced)
        .build();
  }

  @Override
  protected PriceStatisticsResponse calculatePriceAfterApplyDiscountForOrder(EDiscount discount,
      ApplyDiscountRequest applyDiscountRequest) {
    return null;
  }

  private double calculateAmoundProductReduced(EDiscount discount, double promotionalTotal) {
    if (discount.getDiscountCategory().equals(DiscountCategory.SHIPPING))
      return 0.0;

    return calculateDiscountAmount(discount, promotionalTotal);
  }

  private final double calculateAmoundShipReduced(EDiscount discount, double promotionalTotal) {
    if (discount.getDiscountCategory().equals(DiscountCategory.PRODUCT))
      return 0.0;
    return calculateDiscountAmount(discount, promotionalTotal);
  }

  private void validateDiscountUsableShopCondition(EDiscount discount, UUID shopId) {
    switch (discount.getScope()) {
      case GLOBAL -> {
        if (this.shopPromotionRepository.findByShopIdAndPromotionId(shopId, discount.getId()).isEmpty())
          throw new ResourceNotFoundException("Discount is not for this shop");
      }
      case SHOP -> {
        if (!discount.getShopId().equals(shopId))
          throw new ResourceNotFoundException("Discount is not for this shop");
        if (!discount.getScope().equals(EDiscount.Scope.SHOP))
          throw new ResourceNotFoundException("Discount is not for this shop");
      }
      default -> throw new ResourceNotFoundException("Invalid discount scope");
    }
  }

  private CustomerCartWithShop validateAndGetCart(ApplyDiscountRequest applyDiscountRequest, EDiscount discount) {
    CustomerCartWithShop cart = Optional.ofNullable(applyDiscountRequest.customerCartWithShop())
        .orElseThrow(() -> new ResourceNotFoundException("Not found shop of discount  in customer cart"));
    validateDiscountUsableShopCondition(discount, cart.shopId());
    return cart;
  }

  private double calculateAndValidatePromotionalTotal(EDiscount discount, CustomerCartWithShop cart) {
    double total = calculateSpecificProductsDiscount(discount, cart);
    if (total < discount.getMinOrderValue()) {
      throw new ResourceNotFoundException("Not enough order value");
    }
    return total;
  }

  private double calculateDiscountAmount(EDiscount discount, double baseAmount) {
    return discount.getDiscountType().equals(DiscountType.PERCENTAGE)
        ? baseAmount * discount.getValue() / 100
        : discount.getValue();
  }

  private double calculateSpecificProductsDiscount(EDiscount discount, CustomerCartWithShop cartWithShop) {
    if (discount.getAppliesTo().equals(ApplyDiscountType.ALL) || discount.getScope().equals(EDiscount.Scope.GLOBAL))
      return cartWithShop.priceStatistic().finalPrice();

    List<UUID> productIds = Optional
        .ofNullable(discount.getProductDiscounts().stream()
            .map(EProductDiscount::getProductId)
            .collect(Collectors.toList()))
        .orElseThrow(() -> new ResourceNotFoundException("No products found for specific discount"));

    double totalPromotionPrice = cartWithShop.cartItems().stream()
        .filter(cartItem -> productIds.contains(cartItem.productVariantId()))
        .mapToDouble(cartItem -> cartItem.price())
        .sum();

    return totalPromotionPrice;
  }
}
