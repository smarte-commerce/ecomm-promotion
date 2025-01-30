package com.winnguyen1905.promotion.core.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.core.feign.CartServiceClient;
import com.winnguyen1905.promotion.core.model.CustomerCart;
import com.winnguyen1905.promotion.core.model.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscount.CreatorType;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public abstract class DiscountServiceImpl implements DiscountService {

  protected record OrderDiscounts(
      EDiscount shopDiscount,
      EDiscount shippingDiscount,
      EDiscount globalDiscount) {
  }

  protected final CartServiceClient cartServiceClient;
  protected final DiscountRepository discountRepository;
  protected final UserDiscountRepository userDiscountRepository;
  protected final ShopPromotionRepository shopPromotionRepository;
  protected final DiscountUsageRepository discountUsageRepository;

  @Override
  public void updateDiscount(UUID userId, UpdateDiscountRequest updateDiscountRequest) {
    throw new UnsupportedOperationException("Unimplemented method 'updateDiscount'");
  }

  @Override
  public void addDiscount(UUID userId, AddDiscountRequest request) {
    validateDiscountRequest(request);
    EDiscount discount = buildDiscount(userId, request);
    this.discountRepository.save(discount);
  }

  protected abstract PriceStatisticsResponse calculatePriceAfterApplyDiscountForCart(
      EDiscount discount,
      ApplyDiscountRequest request);

  protected abstract PriceStatisticsResponse calculatePriceAfterApplyDiscountForOrder(
      EDiscount discount,
      ApplyDiscountRequest request);

  @Override
  public ApplyDiscountResponse applyDiscountForCart(UUID customerId, ApplyDiscountRequest request) {
    EDiscount discount = findAndValidateDiscount(customerId, request.discountId());

    return ApplyDiscountResponse.builder()
        .shopId(discount.getShopId())
        .discountId(discount.getId())
        .priceStatisticsResponse(this.calculatePriceAfterApplyDiscountForCart(discount, request))
        .build();
  }

  @Override
  public ApplyDiscountResponse applyDiscountForOrder(UUID customerId, ApplyDiscountRequest request) {
    // Get and validate all discount types
    var discounts = getValidatedDiscounts(customerId, request);

    // Apply discounts sequentially
    PriceStatisticsResponse finalPriceStats = applyDiscountsInOrder(customerId, request, discounts);

    return ApplyDiscountResponse.builder()
        .shopId(request.shopId())
        .discountId(request.discountId())
        .priceStatisticsResponse(finalPriceStats).build();
  }

  private OrderDiscounts getValidatedDiscounts(UUID customerId, ApplyDiscountRequest request) {
    // Get and validate shop discount
    EDiscount shopDiscount = findAndValidateDiscount(customerId, request.shopDiscountId());
    validateDiscountScope(shopDiscount, EDiscount.Scope.SHOP, "Shop discount must have SHOP scope");
    validateDiscountCategory(shopDiscount, DiscountCategory.PRODUCT, "Shop discount must be PRODUCT category");
    validateDiscountEligibility(customerId, shopDiscount);

    // Get and validate shipping discount
    EDiscount shippingDiscount = findAndValidateDiscount(customerId, request.shippingDiscountId());
    validateDiscountScope(shippingDiscount, EDiscount.Scope.SHOP, "Shipping discount must have SHOP scope");
    validateDiscountCategory(shippingDiscount, DiscountCategory.SHIPPING,
        "Shipping discount must be SHIPPING category");
    validateDiscountEligibility(customerId, shippingDiscount);

    // Get and validate global discount
    EDiscount globalDiscount = findAndValidateDiscount(customerId, request.globallyDiscountId());
    validateDiscountScope(globalDiscount, EDiscount.Scope.GLOBAL, "Global discount must have GLOBAL scope");
    validateDiscountCategory(globalDiscount, DiscountCategory.PRODUCT, "Global discount must be PRODUCT category");
    validateDiscountEligibility(customerId, globalDiscount);

    return new OrderDiscounts(shopDiscount, shippingDiscount, globalDiscount);
  }

  private void validateDiscountScope(EDiscount discount, EDiscount.Scope expectedScope, String errorMessage) {
    if (!discount.getScope().equals(expectedScope)) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private void validateDiscountCategory(EDiscount discount, DiscountCategory expectedCategory, String errorMessage) {
    if (!discount.getDiscountCategory().equals(expectedCategory)) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private PriceStatisticsResponse applyDiscountsInOrder(
      UUID customerId,
      ApplyDiscountRequest originalRequest,
      OrderDiscounts discounts) {
    CustomerCartWithShop currentCart = originalRequest.customerCartWithShop();

    // Step 1: Apply shop discount first
    PriceStatisticsResponse shopPriceStats = applyDiscount(
        customerId,
        originalRequest.shopId(),
        discounts.shopDiscount(),
        currentCart);

    // Step 2: Apply global discount
    CustomerCartWithShop cartAfterShopDiscount = updateCartWithDiscount(currentCart, shopPriceStats);
    PriceStatisticsResponse globalPriceStats = applyDiscount(
        customerId,
        originalRequest.shopId(),
        discounts.globalDiscount(),
        cartAfterShopDiscount);

    // Step 3: Finally apply shipping discount
    CustomerCartWithShop cartAfterGlobalDiscount = updateCartWithDiscount(currentCart, globalPriceStats);
    return applyDiscount(
        customerId,
        originalRequest.shopId(),
        discounts.shippingDiscount(),
        cartAfterGlobalDiscount);
  }

  private PriceStatisticsResponse applyDiscount(
      UUID customerId,
      UUID shopId,
      EDiscount discount,
      CustomerCartWithShop cart) {
    ApplyDiscountRequest request = ApplyDiscountRequest.builder()
        .shopId(shopId)
        .customerId(customerId)
        .discountId(discount.getId())
        .customerCartWithShop(cart)
        .build();

    return calculatePriceAfterApplyDiscountForOrder(discount, request);
  }

  // private ApplyDiscountResponse buildOrderDiscountResponse(
  // ApplyDiscountRequest request,
  // PriceStatisticsResponse finalPriceStats) {
  // return ApplyDiscountResponse.builder()
  // .shopId(request.shopId())
  // .discountId(request.discountId())
  // .priceStatisticsResponse(finalPriceStats)
  // .build();
  // }

  private CustomerCartWithShop updateCartWithDiscount(
      CustomerCartWithShop originalCart,
      PriceStatisticsResponse priceStats) {
    return new CustomerCartWithShop(
        originalCart.shopId(),
        originalCart.cartItems(),
        priceStats);
  }

  private EDiscount findAndValidateDiscount(UUID customerId, UUID discountId) {
    EUserDiscount userDiscount = userDiscountRepository
        .findByCustomerIdAndDiscountId(customerId, discountId)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("No discount found for customer %s and discount %s", customerId, discountId)));

    return Optional.ofNullable(userDiscount.getDiscount())
        .orElseThrow(() -> new EntityNotFoundException("Invalid discount reference in user discount"));
  }

  private void validateDiscountEligibility(UUID customerId, EDiscount discount) {
    validateUsageLimits(customerId, discount);
    validateTimeConstraints(discount);
    validateDiscountType(discount);
  }

  private void validateUsageLimits(UUID customerId, EDiscount discount) {
    if (discount.getUsageCount() >= discount.getUsageLimit()) {
      throw new ResourceNotFoundException("Discount has reached its global usage limit");
    }

    int customerUsageCount = discountUsageRepository.countByDiscountIdAndCustomerId(customerId, discount.getId());
    if (customerUsageCount >= discount.getLimitUsagePerCutomer()) {
      throw new ResourceNotFoundException(
          String.format("Customer %s has reached their usage limit for this discount", customerId));
    }
  }

  private void validateTimeConstraints(EDiscount discount) {
    Instant now = Instant.now();

    if (discount.getStartDate().isAfter(now)) {
      throw new ResourceNotFoundException("Discount period has not started yet");
    }

    if (discount.getEndDate().isBefore(now)) {
      throw new ResourceNotFoundException("Discount period has already ended");
    }
  }

  private void validateDiscountType(EDiscount discount) {
    if (discount.getAppliesTo().equals(ApplyDiscountType.SPECIFIC)) {
      // throw new ResourceNotFoundException("This discount requires specific product
      // selection");
    }
  }

  private void validateDiscountRequest(AddDiscountRequest request) {
    Instant now = Instant.now();
    if (request.startDate().isBefore(now)) {
      throw new IllegalArgumentException("Start date must be in the future");
    }
    if (request.endDate().isBefore(request.startDate())) {
      throw new IllegalArgumentException("End date must be after start date");
    }

    if (request.discountType() == DiscountType.PERCENTAGE) {
      if (request.value() <= 0 || request.value() > 100) {
        throw new IllegalArgumentException("Percentage discount must be between 0 and 100");
      }
      if (request.maxReducedValue() <= 0) {
        throw new IllegalArgumentException("Maximum reduced value must be greater than 0");
      }
    } else {
      if (request.value() <= 0) {
        throw new IllegalArgumentException("Fixed amount discount must be greater than 0");
      }
    }

    if (request.usageLimit() <= 0 || request.limitUsagePerCutomer() <= 0) {
      throw new IllegalArgumentException("Usage limits must be greater than 0");
    }
    if (request.limitUsagePerCutomer() > request.usageLimit()) {
      throw new IllegalArgumentException("Per-customer usage limit cannot exceed total usage limit");
    }

    if (request.code() == null || request.code().trim().isEmpty() || !request.code().matches("^[a-zA-Z0-9-_]+$")) {
      throw new IllegalArgumentException("Invalid discount code format");
    }
    // if (discountRepository.existsByCode(request.code())) {
    // throw new IllegalArgumentException("Discount code already exists");
    // }
  }

  private EDiscount buildDiscount(UUID userId, AddDiscountRequest request) {
    return EDiscount.builder()
        .scope(request.scope())
        .creatorType(request.scope() == EDiscount.Scope.SHOP ? CreatorType.SHOP : CreatorType.ADMIN)
        .discountType(request.discountType())
        .appliesTo(request.appliesTo())
        .discountCategory(request.discountCategory())
        .name(request.name())
        .description(request.description())
        .value(request.value())
        .maxReducedValue(request.maxReducedValue())
        .code(request.code().toUpperCase())
        .startDate(request.startDate())
        .endDate(request.endDate())
        .usageLimit(request.usageLimit())
        .usageCount(0)
        .limitUsagePerCutomer(request.limitUsagePerCutomer())
        .minOrderValue(request.minOrderValue())
        .isActive(true)
        .shopId(request.scope() == EDiscount.Scope.SHOP ? userId : null)
        .build();
  }
}
