package com.winnguyen1905.promotion.core.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.rsocket.RSocketProperties.Server.Spec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.common.DiscountUsageStatus;
import com.winnguyen1905.promotion.core.feign.CartServiceClient;
import com.winnguyen1905.promotion.core.model.AbstractModel;
import com.winnguyen1905.promotion.core.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.AssignCategoriesRequest;
import com.winnguyen1905.promotion.core.model.request.AssignProductsRequest;
import com.winnguyen1905.promotion.core.model.request.CheckoutRequest;
import com.winnguyen1905.promotion.core.model.request.CustomerCart;
import com.winnguyen1905.promotion.core.model.request.SearchDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.UpdateDiscountStatusRequest;
import com.winnguyen1905.promotion.core.model.request.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.core.model.response.DiscountValidityResponse;
import com.winnguyen1905.promotion.core.model.response.DiscountVm;
import com.winnguyen1905.promotion.core.model.response.PagedResponse;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.exception.BadRequestException;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscount.CreatorType;
import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ProductDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.specification.DiscountSpecification;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

  protected record OrderDiscounts(
      EDiscount shopDiscount,
      EDiscount shippingDiscount,
      EDiscount globalDiscount) {
  }

  // protected final CartServiceClient cartServiceClient;
  protected final DiscountRepository discountRepository;
  protected final UserDiscountRepository userDiscountRepository;
  protected final DiscountUsageRepository discountUsageRepository;
  protected final ProductDiscountRepository productDiscountRepository;

  @Override
  public void createDiscount(TAccountRequest accountRequest, AddDiscountRequest request) {
    validateDiscountRequest(request);
    EDiscount discount = buildDiscount(accountRequest.id(), request);
    List<EProductDiscount> productDiscounts = request.productIds().stream()
        .map(productId -> EProductDiscount.builder().productId(productId).discount(discount).build())
        .collect(Collectors.toList());
    discount.setProductDiscounts(productDiscounts);
    this.discountRepository.save(discount);
  }

  // -----------------------------------------------------------------
  protected PriceStatisticsResponse calculatePriceAfterApplyDiscountForCart(EDiscount discount,
      ApplyDiscountRequest applyDiscountRequest) {
    CustomerCartWithShop cart = validateAndGetCart(applyDiscountRequest, discount);
    validateDiscountUsableShopCondition(discount, applyDiscountRequest.shopId());

    double promotionalTotal = calculateAndValidatePromotionalTotal(discount, cart);
    double amountShipReduced = calculateAmoundShipReduced(discount, promotionalTotal);
    double amountProductReduced = calculateAmoundProductReduced(discount, promotionalTotal);

    return PriceStatisticsResponse.builder()
        .totalProductPrice(cart.priceStatistic().totalProductPrice())
        .totalShipFee(cart.priceStatistic().totalShipFee())
        .amountProductReduced(amountProductReduced)
        .amountShipReduced(amountShipReduced)
        .finalPrice(cart.priceStatistic().finalPrice() - amountProductReduced - amountShipReduced)
        .build();
  }

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
    switch (discount.getCreatorType()) {
      case ADMIN -> {
        // if (this.shopPromotionRepository.findByShopIdAndPromotionId(shopId,
        // discount.getId()).isEmpty())
        // throw new ResourceNotFoundException("Discount is not for this shop");
      }
      case VENDOR -> {
        if (!discount.getVendorId().equals(shopId))
          throw new ResourceNotFoundException("Discount is not for this shop");
        if (!discount.getCreatorType().equals(EDiscount.CreatorType.VENDOR))
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
    if (discount.getAppliesTo().equals(ApplyDiscountType.ALL)
        || discount.getCreatorType().equals(EDiscount.CreatorType.ADMIN))
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

  @Override
  public DiscountValidityResponse checkDiscountValidity(TAccountRequest accountRequest, UUID discountId) {
    EDiscount discount = findAndValidateDiscount(accountRequest.id(), discountId);
    return DiscountValidityResponse.builder()
        .isValid(true)
        .code(discount.getCode())
        .minOrderValue(discount.getMinOrderValue())
        .maxReducedValue(discount.getMaxReducedValue())
        .expiryDate(discount.getEndDate())
        .build();
  }

  // -----------------------------------------------------------------
  @Override
  public ApplyDiscountResponse applyDiscountToCart(TAccountRequest accountRequest, ApplyDiscountRequest request) {
    EDiscount discount = findAndValidateDiscount(accountRequest.id(), request.discountId());

    return ApplyDiscountResponse.builder()
        .shopId(discount.getShopId())
        .discountId(discount.getId())
        .priceStatisticsResponse(this.calculatePriceAfterApplyDiscountForCart(discount, request))
        .build();
  }

  @Override
  public ApplyDiscountResponse applyDiscountToOrder(TAccountRequest accountRequest, ApplyDiscountRequest request) {
    UUID customerId = accountRequest.id();
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
    validateDiscountScope(shopDiscount, EDiscount.CreatorType.VENDOR, "Shop discount must have VENDOR scope");
    validateDiscountCategory(shopDiscount, DiscountCategory.PRODUCT, "Shop discount must be PRODUCT category");
    validateDiscountEligibility(customerId, shopDiscount);

    // Get and validate shipping discount
    EDiscount shippingDiscount = findAndValidateDiscount(customerId, request.shippingDiscountId());
    validateDiscountScope(shippingDiscount, EDiscount.CreatorType.VENDOR, "Shipping discount must have VENDOR scope");
    validateDiscountCategory(shippingDiscount, DiscountCategory.SHIPPING,
        "Shipping discount must be SHIPPING category");
    validateDiscountEligibility(customerId, shippingDiscount);

    // Get and validate global discount
    EDiscount globalDiscount = findAndValidateDiscount(customerId, request.globallyDiscountId());
    validateDiscountScope(globalDiscount, CreatorType.ADMIN, "Global discount must have GLOBAL scope");
    validateDiscountCategory(globalDiscount, DiscountCategory.PRODUCT, "Global discount must be PRODUCT category");
    validateDiscountEligibility(customerId, globalDiscount);

    return new OrderDiscounts(shopDiscount, shippingDiscount, globalDiscount);
  }

  private void validateDiscountScope(EDiscount discount, EDiscount.CreatorType expectedScope, String errorMessage) {
    if (!discount.getCreatorType().equals(expectedScope)) {
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

  protected EDiscount findAndValidateDiscount(UUID customerId, UUID discountId) {
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
    if (discount.getUsageLimitTotal() != null && discount.getUsageCount() >= discount.getUsageLimitTotal()) {
      throw new ResourceNotFoundException("Discount has reached its global usage limit");
    }

    int customerUsageCount = discountUsageRepository.countByDiscountIdAndCustomerId(discount.getId(), customerId);
    if (discount.getUsageLimitPerCustomer() != null && customerUsageCount >= discount.getUsageLimitPerCustomer()) {
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
        .creatorType(request.creatorType() == EDiscount.CreatorType.VENDOR ? 
            EDiscount.CreatorType.VENDOR : EDiscount.CreatorType.ADMIN)
        .creatorId(userId)
        .discountType(request.discountType())
        .appliesTo(request.appliesTo())
        .discountCategory(request.discountCategory())
        .name(request.name())
        .description(request.description())
        .value(request.value())
        .maxDiscountAmount(request.maxReducedValue())
        .code(request.code().toUpperCase())
        .startDate(request.startDate())
        .endDate(request.endDate())
        .usageLimitTotal(request.usageLimit())
        .usageCount(0)
        .usageLimitPerCustomer(request.limitUsagePerCutomer())
        .minOrderValue(request.minOrderValue())
        .isActive(true)
        .vendorId(request.creatorType() == EDiscount.CreatorType.VENDOR ? userId : null)
        .build();
  }

  @Override
  public DiscountVm getDiscountById(TAccountRequest accountRequest, UUID id) {
    EDiscount discount = discountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    return DiscountVm.builder()
        .name(discount.getName())
        .discountCategory(discount.getDiscountCategory())
        .description(discount.getDescription())
        .creatorType(discount.getCreatorType())
        .discountType(discount.getDiscountType())
        .value(discount.getValue())
        .code(discount.getCode())
        .startDate(discount.getStartDate())
        .endDate(discount.getEndDate())
        .usageLimit(discount.getUsageLimit())
        .usesCount(discount.getUsageCount())
        .limitUsagePerCutomer(discount.getLimitUsagePerCutomer())
        .maxReducedValue(discount.getMaxReducedValue())
        .minOrderValue(discount.getMinOrderValue())
        .isActive(discount.getIsActive())
        .appliesTo(discount.getAppliesTo())
        // .categories(discount.getCategoryNames())
        .productIds(
            discount.getProductDiscounts().stream().map(EProductDiscount::getProductId).collect(Collectors.toSet()))
        .build();

  }

  @Override
  public void updateDiscountStatus(TAccountRequest accountRequest, UpdateDiscountStatusRequest request) {
    EDiscount discount = discountRepository.findById(request.discountId())
        .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    discount.setIsActive(request.isActive());
    discountRepository.save(discount);
  }

  @Override
  public void assignProducts(TAccountRequest accountRequest, AssignProductsRequest request) {
    EDiscount discount = discountRepository.findById(request.discountId())
        .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    request.productIds().forEach(productId -> {
      Boolean isExisting = productDiscountRepository.existsByProductIdAndDiscountId(productId, request.discountId());
      if (!isExisting) {
        EProductDiscount productDiscount = EProductDiscount.builder()
            .productId(productId)
            .discount(discount)
            .build();
        productDiscountRepository.save(productDiscount);
      }
    });
  }

  @Override
  public PagedResponse<DiscountVm> getDiscounts(TAccountRequest accountRequest, SearchDiscountRequest request,
      Pageable pageable) {
    Specification<EDiscount> spec = DiscountSpecification.withFilter(request);
    var discounts = discountRepository.findAll(spec, pageable);
    List<DiscountVm> discountVms = discounts.getContent().stream()
        .map(discount -> DiscountVm.builder()
            .name(discount.getName())
            .discountCategory(discount.getDiscountCategory())
            .description(discount.getDescription())
            .creatorType(discount.getCreatorType())
            .discountType(discount.getDiscountType())
            .value(discount.getValue())
            .code(discount.getCode())
            .startDate(discount.getStartDate())
            .endDate(discount.getEndDate())
            .usageLimit(discount.getUsageLimit())
            .usesCount(discount.getUsageCount())
            .limitUsagePerCutomer(discount.getLimitUsagePerCutomer())
            .maxReducedValue(discount.getMaxReducedValue())
            .minOrderValue(discount.getMinOrderValue())
            .isActive(discount.getIsActive())
            .appliesTo(discount.getAppliesTo())
            // .categories(discount.getCategoryNames())
            .productIds(
                discount.getProductDiscounts().stream().map(EProductDiscount::getProductId).collect(Collectors.toSet()))
            .build())
        .collect(Collectors.toList());
    return PagedResponse.<DiscountVm>builder()
        .results(discountVms)
        .page(discounts.getNumber())
        .size(discounts.getSize())
        .totalElements(discounts.getTotalElements())
        .totalPages(discounts.getTotalPages())
        .build();
  }

  @Override
  public void claimDiscount(TAccountRequest accountRequest, UUID discountId) {
    EDiscount discount = discountRepository.findById(discountId)
        .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    // validate
    Boolean isClaimed = userDiscountRepository.existsByCustomerIdAndDiscountId(accountRequest.id(), discountId);

    if (isClaimed) {
      throw new ResourceNotFoundException("Discount already claimed");
    }

    EUserDiscount userDiscount = EUserDiscount.builder()
        .customerId(accountRequest.id())
        .discount(discount)
        .build();

    userDiscountRepository.save(userDiscount);
  }

  @Override
  @Transactional
  public void unassignProducts(TAccountRequest accountRequest, AssignProductsRequest request) {
    List<UUID> productIds = productDiscountRepository
        .findAllByProductIdInAAndDiscountId(request.productIds(), request.discountId())
        .stream()
        .filter(id -> !request.productIds().contains(id))
        .collect(Collectors.toList());
    EDiscount discount = discountRepository.findById(request.discountId())
        .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    List<EProductDiscount> productDiscounts = productIds.stream()
        .map(productId -> EProductDiscount.builder().productId(productId).discount(discount).build())
        .collect(Collectors.toList());
    productDiscountRepository.saveAll(productDiscounts);
  }

  @Override
  public void assignCategories(TAccountRequest accountRequest, AssignCategoriesRequest request) {
    // EDiscount discount = discountRepository.findById(request.discountId())
    // .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    // request.categories().stream().forEach(category -> {
    // Boolean isExisting = discount.getCategoryNames().contains(category);
    // if (!isExisting) {
    // discount.getCategoryNames().add(category);
    // }
    // });
    // discountRepository.save(discount);
  }

  @Override
  public void unassignCategories(TAccountRequest accountRequest, AssignCategoriesRequest request) {
    // EDiscount discount = discountRepository.findById(request.discountId())
    // .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));
    // request.categories().stream().forEach(category -> {
    // Boolean isExisting = discount.getCategoryNames().contains(category);
    // if (isExisting) {
    // discount.getCategoryNames().remove(category);
    // }
    // });
    // discountRepository.save(discount);
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public PriceStatisticsResponse applyDiscountToShop(TAccountRequest accountRequest, CheckoutRequest request) {
    // Find and validate the discount
    EDiscount discount = findAndValidateDiscount(accountRequest.id(), request.getGlobalProductDiscountId());

    // Get the total product price from the request
    double totalProductPrice = request.getTotal();
    double discountValue = discount.getValue();
    double discountAmount = 0.0;

    // Calculate discount amount based on discount type
    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
      discountAmount = (totalProductPrice * discountValue) / 100;

      // Apply maximum reduction if specified
      if (discount.getMaxReducedValue() != null && discount.getMaxReducedValue() > 0) {
        discountAmount = Math.min(discountAmount, discount.getMaxReducedValue());
      }
    } else {
      // Fixed amount discount
      discountAmount = Math.min(discountValue, totalProductPrice);
    }

    // Check if discount has usage limits
    if (discount.getUsageLimit() > 0 && discount.getUsageCount() >= discount.getUsageLimit()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount has reached its maximum usage limit");
    }

    // Check per-user usage limit
    if (discount.getLimitUsagePerCutomer() > 0) {
      int userUsageCount = discountUsageRepository.countByDiscountIdAndCustomerId(
          discount.getId(), accountRequest.id());
      if (userUsageCount >= discount.getLimitUsagePerCutomer()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "You have reached the maximum usage limit for this discount");
      }
    }

    // Calculate final price after discount
    double finalPrice = Math.max(0, totalProductPrice - discountAmount);

    try {
      // Update discount usage count
      discount.setUsageCount(discount.getUsageCount() + 1);
      discountRepository.save(discount);

      // Create and save discount usage record
      EDiscountUsage discountUsage = EDiscountUsage.builder()
          .discount(discount)
          .customerId(accountRequest.id())
          .orderId(UUID.randomUUID()) // This should be replaced with actual order ID when available
          .usageStatus(DiscountUsageStatus.SUCCESS)
          .build();

      // If there's a userDiscount associated with this usage, set it
      if (discount.getUserDiscounts() != null && !discount.getUserDiscounts().isEmpty()) {
        discountUsage.setUserDiscount(discount.getUserDiscounts().get(0));
      }

      discountUsageRepository.save(discountUsage);

      // Since totalProductPrice already comes from request.getTotal(), we'll use it as the totalPrice
      // as shipping fee is not part of the discount calculation in this method
      return PriceStatisticsResponse.builder()
          .totalPrice(totalProductPrice) // Total price before any discounts
          .totalProductPrice(totalProductPrice) // Original product price
          .totalShipFee(0.0) // No shipping fee in this calculation
          .amountShipReduced(0.0) // No shipping discount applied in this method
          .totalDiscountVoucher(discountAmount) // Total discount from voucher
          .amountProductReduced(discountAmount) // Amount reduced from product price
          .finalPrice(finalPrice) // Final price after applying discount
          .build();

    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to apply discount: " + e.getMessage(), e);
    }
  }
}
