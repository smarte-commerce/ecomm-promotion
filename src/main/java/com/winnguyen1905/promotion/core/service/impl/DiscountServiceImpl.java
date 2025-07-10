package com.winnguyen1905.promotion.core.service.impl;

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
import com.winnguyen1905.promotion.exception.BadRequestException;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.model.AbstractModel;
import com.winnguyen1905.promotion.model.request.AddDiscountRequest;
import com.winnguyen1905.promotion.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.model.request.AssignCategoriesRequest;
import com.winnguyen1905.promotion.model.request.AssignProductsRequest;
import com.winnguyen1905.promotion.model.request.CheckoutRequest;
import com.winnguyen1905.promotion.model.request.ComprehensiveDiscountRequest;
import com.winnguyen1905.promotion.model.request.CustomerCart;
import com.winnguyen1905.promotion.model.request.SearchDiscountRequest;
import com.winnguyen1905.promotion.model.request.UpdateDiscountRequest;
import com.winnguyen1905.promotion.model.request.UpdateDiscountStatusRequest;
import com.winnguyen1905.promotion.model.request.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.model.response.ApplyDiscountResponse;
import com.winnguyen1905.promotion.model.response.ComprehensiveDiscountResponse;
import com.winnguyen1905.promotion.model.response.DiscountValidityResponse;
import com.winnguyen1905.promotion.model.response.DiscountVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscount.CreatorType;
import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ProductDiscountRepository;

import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.specification.DiscountSpecification;
import com.winnguyen1905.promotion.secure.TAccountRequest;
import com.winnguyen1905.promotion.core.service.DiscountService;
import com.winnguyen1905.promotion.core.service.DistributedLockService;
import com.winnguyen1905.promotion.core.service.OptimisticLockingService;
import com.winnguyen1905.promotion.core.service.OptimisticLockingService.OptimisticLockingException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

  // Optimistic locking services
  private final OptimisticLockingService optimisticLockingService;
  private final DistributedLockService distributedLockService;

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
    CustomerCartWithShop cart = validateAndGetCart(applyDiscountRequest, discount);
    validateDiscountUsableShopCondition(discount, applyDiscountRequest.shopId());

    double promotionalTotal = calculateAndValidatePromotionalTotal(discount, cart);
    double amountReduced = calculateDiscountAmount(discount, promotionalTotal);

    double amountShipReduced = 0.0;
    double amountProductReduced = 0.0;

    if (discount.getDiscountCategory().equals(DiscountCategory.SHIPPING)) {
      amountShipReduced = amountReduced;
    } else { // Handles PRODUCT and other categories if any
      amountProductReduced = amountReduced;
    }

    return PriceStatisticsResponse.builder()
        .totalProductPrice(cart.priceStatistic().totalProductPrice())
        .totalShipFee(cart.priceStatistic().totalShipFee())
        .amountProductReduced(amountProductReduced)
        .amountShipReduced(amountShipReduced)
        .finalPrice(cart.priceStatistic().finalPrice() - amountProductReduced - amountShipReduced)
        .build();
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
    double discountValue = discount.getValue();
    double discountAmount;

    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
      discountAmount = (baseAmount * discountValue) / 100;

      if (discount.getMaxDiscountAmount() != null && discount.getMaxDiscountAmount() > 0) {
        discountAmount = Math.min(discountAmount, discount.getMaxDiscountAmount());
      }
    } else {
      discountAmount = Math.min(discountValue, baseAmount);
    }
    return discountAmount;
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
        .maxReducedValue(discount.getMaxDiscountAmount())
        .expiryDate(discount.getEndDate())
        .build();
  }

  // -----------------------------------------------------------------
  @Override
  public ApplyDiscountResponse applyDiscountToCart(TAccountRequest accountRequest, ApplyDiscountRequest request) {
    EDiscount discount = findAndValidateDiscount(accountRequest.id(), request.discountId());

    return ApplyDiscountResponse.builder()
        .shopId(discount.getVendorId())
        .discountId(discount.getId())
        .priceStatisticsResponse(this.calculatePriceAfterApplyDiscountForCart(discount, request))
        .build();
  }

  // OUTDATED
  @Override
  public ApplyDiscountResponse applyDiscountToOrder(TAccountRequest accountRequest, ApplyDiscountRequest request) {
    UUID customerId = accountRequest.id();

    // Apply single discount with optimistic locking
    PriceStatisticsResponse finalPriceStats = applySingleDiscountWithOptimisticLocking(customerId, request);

    return ApplyDiscountResponse.builder()
        .shopId(request.shopId())
        .discountId(request.discountId())
        .priceStatisticsResponse(finalPriceStats).build();
  }

  // check the correctness of the logic here
  @Override
  @Transactional
  public ComprehensiveDiscountResponse applyComprehensiveDiscounts(TAccountRequest accountRequest,
      ComprehensiveDiscountRequest request) {
    if (request == null || request.getCheckoutItems() == null || request.getCheckoutItems().isEmpty()) {
      throw new BadRequestException("Invalid comprehensive discount request");
    }

    UUID customerId = accountRequest.id();
    List<ComprehensiveDiscountResponse.DrawOrder> processedOrders = new ArrayList<>();

    // Step 1: Apply shop-specific discounts for each order
    for (ComprehensiveDiscountRequest.DrawOrder order : request.getCheckoutItems()) {
      try {
        ComprehensiveDiscountResponse.DrawOrder processedOrder = applyShopDiscountToOrder(customerId, order, request);
        processedOrders.add(processedOrder);
      } catch (Exception e) {
        // Create a failed order response with zero discounts
        ComprehensiveDiscountResponse.DrawOrder failedOrder = createFailedOrderResponse(order, request);
        processedOrders.add(failedOrder);
      }
    }

    // Step 2: Apply global product discount across all orders
    processedOrders = applyGlobalProductDiscountToOrders(customerId, processedOrders, request);

    // Step 3: Apply global shipping discount across all orders
    processedOrders = applyGlobalShippingDiscountToOrders(customerId, processedOrders, request);

    return ComprehensiveDiscountResponse.builder()
        .sagaId(request.getSagaId())
        .customerId(request.getCustomerId())
        .eventType(request.getEventType())
        .globalProductDiscountId(request.getGlobalProductDiscountId())
        .globalShippingDiscountId(request.getGlobalShippingDiscountId())
        .checkoutItems(processedOrders)
        .build();
  }

  private ComprehensiveDiscountResponse.DrawOrder createFailedOrderResponse(
      ComprehensiveDiscountRequest.DrawOrder order, ComprehensiveDiscountRequest request) {
    return ComprehensiveDiscountResponse.DrawOrder.builder()
        .shopId(order.getShopId())
        .orderId(order.getOrderId())
        .customerId(request.getCustomerId())
        .items(convertToResponseItems(order.getItems(), false))
        .shopProductDiscountId(order.getShopProductDiscountId())
        .totalOrderBeforeDiscounts(calculateOrderTotal(order.getItems()))
        .totalShopProductDiscount(0.0)
        .totalGlobalProductDiscount(0.0)
        .totalGlobalShippingDiscount(0.0)
        .totalOrderAfterDiscounts(calculateOrderTotal(order.getItems()))
        .build();
  }

  private ComprehensiveDiscountResponse.DrawOrder applyShopDiscountToOrder(UUID customerId,
      ComprehensiveDiscountRequest.DrawOrder order, ComprehensiveDiscountRequest request) {
    // Convert items to CustomerCartWithShop format for existing discount logic
    List<CustomerCart.CartItem> cartItems = order.getItems().stream()
        .map(item -> CustomerCart.CartItem.builder()
            .productVariantId(item.getVariantId())
            .quantity(item.getQuantity())
            .price(item.getUnitPrice())
            .isSelected(true)
            .build())
        .collect(Collectors.toList());

    double orderTotal = calculateOrderTotal(order.getItems());

    PriceStatisticsResponse priceStats = PriceStatisticsResponse.builder()
        .totalProductPrice(orderTotal)
        .totalShipFee(0.0)
        .amountProductReduced(0.0)
        .amountShipReduced(0.0)
        .finalPrice(orderTotal)
        .build();

    CustomerCartWithShop cart = new CustomerCartWithShop(order.getShopId(), cartItems, priceStats);

    // Apply shop product discount if available, otherwise use original price
    double shopDiscountAmount = 0.0;
    double finalTotal = orderTotal; // Default to original total if no discount

    if (order.getShopProductDiscountId() != null) {
      try {
        ApplyDiscountRequest shopDiscountRequest = ApplyDiscountRequest.builder()
            .shopId(order.getShopId())
            .customerId(customerId)
            .discountId(order.getShopProductDiscountId())
            .customerCartWithShop(cart)
            .build();

        PriceStatisticsResponse shopResult = applySingleDiscountWithOptimisticLocking(customerId, shopDiscountRequest);
        shopDiscountAmount = (orderTotal - shopResult.finalPrice());
        cart = updateCartWithDiscount(cart, shopResult);
        finalTotal = shopResult.finalPrice();
      } catch (Exception e) {
        // Continue with zero shop discount and original price
        shopDiscountAmount = 0.0;
        finalTotal = orderTotal;
      }
    }
    // If no shop discount ID provided, explicitly use original order total (no
    // discount applied)

    return ComprehensiveDiscountResponse.DrawOrder.builder()
        .shopId(order.getShopId())
        .orderId(order.getOrderId())
        .customerId(request.getCustomerId())
        .items(convertToResponseItems(order.getItems(), true))
        .shopProductDiscountId(order.getShopProductDiscountId())
        .totalOrderBeforeDiscounts(orderTotal)
        .totalShopProductDiscount(shopDiscountAmount)
        .totalGlobalProductDiscount(0.0) // Will be set in global discount phase
        .totalGlobalShippingDiscount(0.0) // Will be set in global shipping discount phase
        .totalOrderAfterDiscounts(finalTotal)
        .build();
  }

  private List<ComprehensiveDiscountResponse.DrawOrder> applyGlobalProductDiscountToOrders(
      UUID customerId,
      List<ComprehensiveDiscountResponse.DrawOrder> orders,
      ComprehensiveDiscountRequest request) {

    // If no global product discount ID provided, return orders as-is
    if (request.getGlobalProductDiscountId() == null) {
      return orders;
    }

    List<ComprehensiveDiscountResponse.DrawOrder> updatedOrders = new ArrayList<>();

    for (ComprehensiveDiscountResponse.DrawOrder order : orders) {
      try {
        // Convert order to cart format for discount calculation
        List<CustomerCart.CartItem> cartItems = order.getItems().stream()
            .map(item -> CustomerCart.CartItem.builder()
                .productVariantId(item.getVariantId())
                .quantity(item.getQuantity())
                .price(item.getUnitPrice())
                .isSelected(true)
                .build())
            .collect(Collectors.toList());

        PriceStatisticsResponse currentPriceStats = PriceStatisticsResponse.builder()
            .totalProductPrice(order.getTotalOrderAfterDiscounts())
            .totalShipFee(0.0)
            .amountProductReduced(0.0)
            .amountShipReduced(0.0)
            .finalPrice(order.getTotalOrderAfterDiscounts())
            .build();

        CustomerCartWithShop cart = new CustomerCartWithShop(order.getShopId(), cartItems, currentPriceStats);

        ApplyDiscountRequest globalDiscountRequest = ApplyDiscountRequest.builder()
            .shopId(order.getShopId())
            .customerId(customerId)
            .discountId(request.getGlobalProductDiscountId())
            .customerCartWithShop(cart)
            .build();

        PriceStatisticsResponse globalResult = applySingleDiscountWithOptimisticLocking(customerId,
            globalDiscountRequest);
        double globalDiscountAmount = (order.getTotalOrderAfterDiscounts() - globalResult.finalPrice());

        // Create updated order with global product discount applied
        ComprehensiveDiscountResponse.DrawOrder updatedOrder = ComprehensiveDiscountResponse.DrawOrder.builder()
            .shopId(order.getShopId())
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .items(order.getItems())
            .shopProductDiscountId(order.getShopProductDiscountId())
            .totalOrderBeforeDiscounts(order.getTotalOrderBeforeDiscounts())
            .totalShopProductDiscount(order.getTotalShopProductDiscount())
            .totalGlobalProductDiscount(globalDiscountAmount)
            .totalGlobalShippingDiscount(order.getTotalGlobalShippingDiscount())
            .totalOrderAfterDiscounts(globalResult.finalPrice())
            .build();

        updatedOrders.add(updatedOrder);
      } catch (Exception e) {
        // Keep original order if global discount fails (no discount applied)
        updatedOrders.add(order);
      }
    }

    return updatedOrders;
  }

  private List<ComprehensiveDiscountResponse.DrawOrder> applyGlobalShippingDiscountToOrders(
      UUID customerId,
      List<ComprehensiveDiscountResponse.DrawOrder> orders,
      ComprehensiveDiscountRequest request) {

    // If no global shipping discount ID provided, return orders as-is
    if (request.getGlobalShippingDiscountId() == null) {
      return orders;
    }

    List<ComprehensiveDiscountResponse.DrawOrder> updatedOrders = new ArrayList<>();

    for (ComprehensiveDiscountResponse.DrawOrder order : orders) {
      try {
        // Convert order to cart format for discount calculation
        List<CustomerCart.CartItem> cartItems = order.getItems().stream()
            .map(item -> CustomerCart.CartItem.builder()
                .productVariantId(item.getVariantId())
                .quantity(item.getQuantity())
                .price(item.getUnitPrice())
                .isSelected(true)
                .build())
            .collect(Collectors.toList());

        PriceStatisticsResponse currentPriceStats = PriceStatisticsResponse.builder()
            .totalProductPrice(order.getTotalOrderAfterDiscounts())
            .totalShipFee(0.0) // Assuming shipping fee calculation logic would be here
            .amountProductReduced(0.0)
            .amountShipReduced(0.0)
            .finalPrice(order.getTotalOrderAfterDiscounts())
            .build();

        CustomerCartWithShop cart = new CustomerCartWithShop(order.getShopId(), cartItems, currentPriceStats);

        ApplyDiscountRequest shippingDiscountRequest = ApplyDiscountRequest.builder()
            .shopId(order.getShopId())
            .customerId(customerId)
            .discountId(request.getGlobalShippingDiscountId())
            .customerCartWithShop(cart)
            .build();

        PriceStatisticsResponse shippingResult = applySingleDiscountWithOptimisticLocking(customerId,
            shippingDiscountRequest);
        double shippingDiscountAmount = (order.getTotalOrderAfterDiscounts() - shippingResult.finalPrice());

        // Create updated order with global shipping discount applied
        ComprehensiveDiscountResponse.DrawOrder updatedOrder = ComprehensiveDiscountResponse.DrawOrder.builder()
            .shopId(order.getShopId())
            .orderId(order.getOrderId())
            .customerId(order.getCustomerId())
            .items(order.getItems())
            .shopProductDiscountId(order.getShopProductDiscountId())
            .totalOrderBeforeDiscounts(order.getTotalOrderBeforeDiscounts())
            .totalShopProductDiscount(order.getTotalShopProductDiscount())
            .totalGlobalProductDiscount(order.getTotalGlobalProductDiscount())
            .totalGlobalShippingDiscount(shippingDiscountAmount)
            .totalOrderAfterDiscounts(shippingResult.finalPrice())
            .build();

        updatedOrders.add(updatedOrder);
      } catch (Exception e) {
        // Keep original order if shipping discount fails (no discount applied)
        updatedOrders.add(order);
      }
    }

    return updatedOrders;
  }

  /**
   * @deprecated Replaced by applyShopDiscountToOrder in the new structured flow
   */
  @Deprecated
  private ComprehensiveDiscountResponse.DrawOrder processShopOrder(UUID customerId,
      ComprehensiveDiscountRequest.DrawOrder order, ComprehensiveDiscountRequest request) {
    // This method is deprecated - use the new structured flow instead
    return applyShopDiscountToOrder(customerId, order, request);
  }

  private List<ComprehensiveDiscountResponse.DrawOrderItem> convertToResponseItems(
      List<ComprehensiveDiscountRequest.DrawOrderItem> items, boolean eligible) {
    return items.stream()
        .map(item -> ComprehensiveDiscountResponse.DrawOrderItem.builder()
            .productId(item.getProductId())
            .variantId(item.getVariantId())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .productSku(item.getProductSku())
            .isEligibleForDiscount(eligible)
            .build())
        .collect(Collectors.toList());
  }

  private double calculateOrderTotal(List<ComprehensiveDiscountRequest.DrawOrderItem> items) {
    return items.stream()
        .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
        .sum();
  }

  /**
   * Applies a single discount with optimistic locking for each discount.
   */
  private PriceStatisticsResponse applySingleDiscountWithOptimisticLocking(UUID customerId,
      ApplyDiscountRequest request) {
    if (request.discountId() == null) {
      // Return original cart values if no discount provided
      return PriceStatisticsResponse.builder()
          .totalProductPrice(request.customerCartWithShop().priceStatistic().totalProductPrice())
          .totalShipFee(request.customerCartWithShop().priceStatistic().totalShipFee())
          .totalPrice(request.customerCartWithShop().priceStatistic().finalPrice())
          .amountProductReduced(0.0)
          .amountShipReduced(0.0)
          .finalPrice(request.customerCartWithShop().priceStatistic().finalPrice())
          .build();
    }

    // Validate version information if provided
    if (request.discountVersion() != null) {
      optimisticLockingService.validateVersion(request.discountId(), request.discountVersion());
    }

    // Get and validate the discount
    EDiscount discount = findAndValidateDiscount(customerId, request.discountId());
    validateDiscountEligibility(customerId, discount);

    // Apply discount with optimistic locking
    return applyDiscountWithLocking(
        customerId,
        request.shopId(),
        discount,
        request.customerCartWithShop(),
        request.discountVersion());
  }

  /**
   * Applies multiple discounts with optimistic locking for each discount.
   * 
   * @deprecated Use applySingleDiscountWithOptimisticLocking for better control
   */
  private PriceStatisticsResponse applyDiscountsWithOptimisticLocking(UUID customerId, ApplyDiscountRequest request) {
    // Validate version information if provided
    validateDiscountVersions(request);

    // Get and validate all discount types
    var discounts = getValidatedDiscounts(customerId, request);

    // Apply discounts sequentially with optimistic locking
    return applyDiscountsInOrderWithLocking(customerId, request, discounts);
  }

  /**
   * Validates discount versions for optimistic locking if provided in the
   * request.
   */
  private void validateDiscountVersions(ApplyDiscountRequest request) {
    if (request.discountVersion() != null && request.discountId() != null) {
      optimisticLockingService.validateVersion(request.discountId(), request.discountVersion());
    }
    if (request.shopDiscountVersion() != null && request.shopDiscountId() != null) {
      optimisticLockingService.validateVersion(request.shopDiscountId(), request.shopDiscountVersion());
    }
    if (request.shippingDiscountVersion() != null && request.shippingDiscountId() != null) {
      optimisticLockingService.validateVersion(request.shippingDiscountId(), request.shippingDiscountVersion());
    }
    if (request.globallyDiscountVersion() != null && request.globallyDiscountId() != null) {
      optimisticLockingService.validateVersion(request.globallyDiscountId(), request.globallyDiscountVersion());
    }
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

  /**
   * Applies discounts in order with optimistic locking for each discount.
   */
  private PriceStatisticsResponse applyDiscountsInOrderWithLocking(
      UUID customerId,
      ApplyDiscountRequest originalRequest,
      OrderDiscounts discounts) {
    CustomerCartWithShop currentCart = originalRequest.customerCartWithShop();

    // Step 1: Apply shop discount first with optimistic locking
    PriceStatisticsResponse shopPriceStats = applyDiscountWithLocking(
        customerId,
        originalRequest.shopId(),
        discounts.shopDiscount(),
        currentCart,
        originalRequest.shopDiscountVersion());

    // Step 2: Apply global discount with optimistic locking
    CustomerCartWithShop cartAfterShopDiscount = updateCartWithDiscount(currentCart, shopPriceStats);
    PriceStatisticsResponse globalPriceStats = applyDiscountWithLocking(
        customerId,
        originalRequest.shopId(),
        discounts.globalDiscount(),
        cartAfterShopDiscount,
        originalRequest.globallyDiscountVersion());

    // Step 3: Finally apply shipping discount with optimistic locking
    CustomerCartWithShop cartAfterGlobalDiscount = updateCartWithDiscount(cartAfterShopDiscount, globalPriceStats);

    return applyDiscountWithLocking(
        customerId,
        originalRequest.shopId(),
        discounts.shippingDiscount(),
        cartAfterGlobalDiscount,
        originalRequest.shippingDiscountVersion());
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

  /**
   * Applies a single discount with optimistic locking.
   */
  private PriceStatisticsResponse applyDiscountWithLocking(
      UUID customerId,
      UUID shopId,
      EDiscount discount,
      CustomerCartWithShop cart,
      Long expectedVersion) {

    return optimisticLockingService.executeWithOptimisticLocking(discount.getId(), lockedDiscount -> {
      if (expectedVersion != null && !expectedVersion.equals(lockedDiscount.getVersion())) {
        throw new OptimisticLockingService.OptimisticLockingException(
            String.format("Version mismatch for discount %s. Expected: %d, Actual: %d",
                lockedDiscount.getId(), expectedVersion, lockedDiscount.getVersion()));
      }

      validateDiscountEligibility(customerId, lockedDiscount);
      validateUsageLimitsForApplication(customerId, lockedDiscount);

      ApplyDiscountRequest request = ApplyDiscountRequest.builder()
          .shopId(shopId)
          .customerId(customerId)
          .discountId(lockedDiscount.getId())
          .customerCartWithShop(cart)
          .build();

      PriceStatisticsResponse priceStats = calculatePriceAfterApplyDiscountForOrder(lockedDiscount, request);

      lockedDiscount.setUsageCount(lockedDiscount.getUsageCount() + 1);
      discountRepository.save(lockedDiscount);

      double totalOriginalPrice = priceStats.totalProductPrice() + priceStats.totalShipFee();
      double discountAmount = totalOriginalPrice - priceStats.finalPrice();

      EDiscountUsage discountUsage = createDiscountUsageRecord(lockedDiscount, customerId, UUID.randomUUID(),
          discountAmount);
      discountUsageRepository.save(discountUsage);

      return priceStats;
    });
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

  /**
   * Validates usage limits with current discount state (optimistic locking safe).
   * This method should be called within an optimistic locking context.
   */
  private void validateUsageLimits(UUID customerId, EDiscount discount) {
    // Check global usage limit with current state
    if (discount.getUsageLimitTotal() != null && discount.getUsageCount() >= discount.getUsageLimitTotal()) {
      throw new ResourceNotFoundException("Discount has reached its global usage limit");
    }

    // Check per-customer usage limit
    int customerUsageCount = discountUsageRepository.countByDiscountIdAndCustomerId(discount.getId(), customerId);
    if (discount.getUsageLimitPerCustomer() != null && customerUsageCount >= discount.getUsageLimitPerCustomer()) {
      throw new ResourceNotFoundException(
          String.format("Customer %s has reached their usage limit for this discount", customerId));
    }
  }

  /**
   * Enhanced usage validation that considers the upcoming usage increment.
   * This prevents overselling by checking if the discount would exceed limits
   * after application.
   */
  private void validateUsageLimitsForApplication(UUID customerId, EDiscount discount) {
    // Check if applying this discount would exceed global usage limit
    if (discount.getUsageLimitTotal() != null && (discount.getUsageCount() + 1) > discount.getUsageLimitTotal()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Applying this discount would exceed the global usage limit");
    }

    // Check per-customer usage limit
    int customerUsageCount = discountUsageRepository.countByDiscountIdAndCustomerId(discount.getId(), customerId);
    if (discount.getUsageLimitPerCustomer() != null && (customerUsageCount + 1) > discount.getUsageLimitPerCustomer()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Applying this discount would exceed your personal usage limit");
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
        .creatorType(request.creatorType() == EDiscount.CreatorType.VENDOR ? EDiscount.CreatorType.VENDOR
            : EDiscount.CreatorType.ADMIN)
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
        .usageLimit(discount.getUsageLimitTotal())
        .usesCount(discount.getUsageCount())
        .limitUsagePerCutomer(discount.getUsageLimitPerCustomer())
        .maxReducedValue(discount.getMaxDiscountAmount())
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
            .usageLimit(discount.getUsageLimitTotal())
            .usesCount(discount.getUsageCount())
            .limitUsagePerCutomer(discount.getUsageLimitPerCustomer())
            .maxReducedValue(discount.getMaxDiscountAmount())
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
    UUID discountId = request.getGlobalProductDiscountId();
    UUID customerId = accountRequest.id();

    // Use optimistic locking to safely apply the discount
    return (PriceStatisticsResponse) optimisticLockingService.executeWithOptimisticLocking(discountId, discount -> {
      // Validate discount eligibility
      validateDiscountEligibility(customerId, discount);

      // Get the total product price from the request
      double totalProductPrice = request.getTotal();
      double discountAmount = calculateDiscountAmount(discount, totalProductPrice);

      // Validate usage limits with current state
      validateUsageLimitsForApplication(customerId, discount);

      // Calculate final price after discount
      double finalPrice = Math.max(0, totalProductPrice - discountAmount);

      // Atomically update discount usage count
      discount.setUsageCount(discount.getUsageCount() + 1);
      EDiscount updatedDiscount = discountRepository.save(discount);

      // Create and save discount usage record
      EDiscountUsage discountUsage = createDiscountUsageRecord(
          updatedDiscount, customerId, UUID.randomUUID(), discountAmount);
      discountUsageRepository.save(discountUsage);

      // Since totalProductPrice already comes from request.getTotal(), we'll use it
      // as the totalPrice
      // as shipping fee is not part of the discount calculation in this method
      return PriceStatisticsResponse.builder()
          .totalPrice(totalProductPrice) // Total price before any discounts
          .totalProductPrice(totalProductPrice) // Original product price
          .totalShipFee(0.0) // No shipping fee in this calculation
          .amountShipReduced(0.0) // No shipping discount applied in this method
          .amountProductReduced(discountAmount) // Amount reduced from product price
          .finalPrice(finalPrice) // Final price after applying discount
          .build();
    });
  }

  /**
   * Creates a discount usage record for tracking.
   */
  private EDiscountUsage createDiscountUsageRecord(EDiscount discount, UUID customerId,
      UUID orderId, double discountAmount) {
    EDiscountUsage.EDiscountUsageBuilder builder = EDiscountUsage.builder()
        .discount(discount)
        .customerId(customerId)
        .orderId(orderId)
        .discountAmount(discountAmount)
        .usageStatus(DiscountUsageStatus.SUCCESS);

    // If there's a userDiscount associated with this usage, set it
    if (discount.getUserDiscounts() != null && !discount.getUserDiscounts().isEmpty()) {
      builder.userDiscount(discount.getUserDiscounts().get(0));
    }

    return builder.build();
  }
}
