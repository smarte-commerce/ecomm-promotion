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
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;
import com.winnguyen1905.promotion.util.DiscountUtils;
import com.winnguyen1905.promotion.util.OptionalExtractor;
import com.winnguyen1905.promotion.util.PaginationUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

  private final CartServiceClient cartServiceClient;
  private final DiscountRepository discountRepository;
  private final UserDiscountRepository userDiscountRepository;
  private final DiscountUsageRepository discountUsageRepository;

  @Override
  public PriceStatisticsResponse applyDiscountFactory(UUID customerId, ApplyDiscountRequest applyDiscountRequest) {
    EDiscount discount = getValidatedDiscount(customerId, applyDiscountRequest.discountId());
    CustomerCartWithShop cartWithShop = getValidatedCart(customerId, discount);
    validateDiscountUsableShopCondition(cartWithShop.shopId(), discount);
    validateDiscountUsableCustomerCondition(customerId, discount);
    double totalAmountReduced = calculateDiscountAmount(discount, cartWithShop);
    return buildPriceResponse(cartWithShop, totalAmountReduced);
  }

  private EDiscount getValidatedDiscount(UUID customerId, UUID discountId) {
    EUserDiscount userDiscount = userDiscountRepository
        .findByCustomerIdAndDiscountId(customerId, discountId)
        .orElseThrow(() -> new EntityNotFoundException("User discount not found"));

    return Optional.ofNullable(userDiscount.getDiscount())
        .orElseThrow(() -> new EntityNotFoundException("Discount is not found"));
  }

  private CustomerCartWithShop getValidatedCart(UUID customerId, EDiscount discount) {
    CustomerCart customerCart = Optional
        .ofNullable(cartServiceClient.getCustomerCartDetail(customerId).getBody())
        .orElseThrow(() -> new ResourceNotFoundException("Not found cart"));

    return customerCart.cartByShops().stream()
        .filter(item -> item.shopId().equals(discount.getShopId()))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Not found shop of discount in customer cart"));
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

  private PriceStatisticsResponse buildPriceResponse(CustomerCartWithShop cartWithShop, double amountReduced) {
    return PriceStatisticsResponse.builder()
        .totalPrice(cartWithShop.priceStatistic().totalPrice())
        .amountProductReduced(amountReduced)
        .finalPrice(cartWithShop.priceStatistic().finalPrice() - amountReduced)
        .build();
  }

  private void validateDiscountUsableShopCondition(UUID shopId, EDiscount discount) {
    if (discount.getScope().equals(EDiscount.Scope.SHOP) && !discount.getShopId().equals(shopId)) {
      throw new ResourceNotFoundException("Discount is not for this shop");
    }

    if (discount.getScope().equals(EDiscount.Scope.GLOBAL)) {
      EShopPromotionParticipant shopPromotionParticipant = this.shopPromotionParticipantRepository
          .findByShopIdAndPromotionId(shopId, discount.getPromotionId())
          .orElseThrow(() -> new ResourceNotFoundException("Shop is not in promotion of ecommerce"));
      if (!shopPromotionParticipant.getIsVerified()) {
        throw new ResourceNotFoundException("Shop is not in promotion of ecommerce");
      }
    }
  }

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

  // // global discount here
  // public PriceStatisticsResponse applyProductDiscount(UUID customerId,
  // EDiscount discount) {
  // cartServiceClient.getTotalPriceOfCartWithShop()
  // return null;
  // }

  // private PriceStatisticsResponse applyShippingDiscount(UUID customerId,
  // EDiscount discount) {
  // cartServiceClient.getTotalPriceOffEachShop()
  // return null;
  // }

  // private final ModelMapper modelMapper;

  // @Override
  // public Discount handleCreateDiscountCode(Discount discount, UUID shopId) {
  // List<UUID> productIds = Optional.ofNullable(discount.getProducts())
  // .map(products -> products.stream().map(item ->
  // item.getId()).collect(Collectors.toList()))
  // .orElse(new ArrayList<>());

  // discount.setProducts(null);
  // EDiscount eDiscount = this.modelMapper.map(discount, EDiscount.class);

  // if (discount.getAppliesTo().equals(ApplyDiscountType.SPECIFIC)) {
  // List<EProduct> eProducts =
  // this.productRepository.findByIdInAndShopId(productIds, shopId);
  // if (eProducts.size() != productIds.size()) {
  // throw new ResourceNotFoundException("Just found " + eProducts.size() + " / "
  // + productIds.size() + " product");
  // }
  // for (EProduct eProduct : eProducts) {
  // eProduct.getDiscounts().add(eDiscount);
  // eDiscount.getProducts().add(eProduct);
  // }
  // }

  // eDiscount.setShopId(shopId);
  // eDiscount = this.discountRepository.save(eDiscount);
  // return this.modelMapper.map(discount, Discount.class);
  // }

  // @Override
  // public Discount handleGetDiscount(UUID id) {
  // // TODO Auto-generated method stub
  // throw new UnsupportedOperationException("Unimplemented method
  // 'handleGetDiscount'");
  // }

  // @Override
  // public Discount handleGetAllDiscountCodesByShop(UUID shopId, Pageable
  // pageable) {
  // Page<EDiscount> discount =
  // this.discountRepository.findAllByShopIdAndIsActiveTrue(shopId, pageable);
  // discount.getContent().forEach(item -> item.setProducts(null));
  // return this.modelMapper.map(discount, Discount.class);
  // }

  // @Override
  // public Boolean handleVerifyDiscountCode(UUID id) {
  // // TODO Auto-generated method stub
  // throw new UnsupportedOperationException("Unimplemented method
  // 'handleVerifyDiscountCode'");
  // }

  // @Override
  // public void handleDeleteDiscountCode(UUID id) {
  // // TODO Auto-generated method stub
  // throw new UnsupportedOperationException("Unimplemented method
  // 'handleDeleteDiscountCode'");
  // }

  // @Override
  // public void handleCancelDiscountCode(UUID id, String username) {
  // // TODO Auto-generated method stub
  // throw new UnsupportedOperationException("Unimplemented method
  // 'handleCancelDiscountCode'");
  // }

  // @Override
  // public Discount handleGetAllProductsRelateDiscountCode(Discount discount,
  // Pageable pageable) {
  // EDiscount eDiscount =
  // this.discountRepository.findByIdAndIsActiveTrue(discount.getId())
  // .orElseThrow(() -> new ResourceNotFoundException("Not found discount id "));

  // Product product;

  // if (discount.getAppliesTo().equals(ApplyDiscountType.ALL)) {
  // Page<EProduct> productPages =
  // this.productRepository.findAllByShopIdAndIsPublishedTrue(eDiscount.getShopId(),
  // pageable);
  // product = this.modelMapper.map(productPages, Product.class);
  // } else {
  // Page<EProduct> productPages = PaginationUtils
  // .createPaginationFromDataListAndPageable(List.copyOf(eDiscount.getProducts()),
  // pageable);
  // product = this.modelMapper.map(productPages, Product.class);
  // }

  // eDiscount.setProducts(null);
  // discount = this.modelMapper.map(discount, Discount.class);
  // discount.setProductList(product);

  // return discount;
  // }

  // @Override
  // @Transactional
  // public PriceStatisticsResponse handleApplyDiscountCodeForCart(UUID
  // customerId, ApplyDiscountRequest applyDiscountRequest, ApplyDiscountStatus
  // applyDiscountStatus) {
  // EDiscount eDiscount =
  // OptionalExtractor.extractFromResource(this.discountRepository.findByIdAndIsActiveTrue(applyDiscountRequest.getDiscountId()));
  // EUserDiscount eUserDiscount =
  // OptionalExtractor.extractFromResource(this.userDiscountRepository.findByUserIdAndDiscountId(customerId,
  // discount.getId()));

  // Boolean isUsable = DiscountUtils.isUsable(eDiscount, eUserDiscount);
  // if (!isUsable) throw new ResourceNotFoundException("Cannot use discount, some
  // error happen");

  // CartEntity cart =
  // this.cartRepository.findById(applyDiscountRequest.getCartId())
  // .orElseThrow(() -> new ResourceNotFoundException("Not found cart"));
  // if (!cart.getCustomer().getId().equals(customerId)) {
  // throw new ResourceNotFoundException("Cannot use discount, some error
  // happen");
  // }

  // // Get total price of all product in the discount program
  // Double totalPriceOfAllProducVariationtInDiscountProgram = DiscountUtils
  // .totalPriceOfAllProducInDiscountProgramFromCart(discount, cart);
  // Double totalPriceOfAllProduct =
  // CartUtils.getPriceOfAllProductSelectedInCart(cart);

  // // Reach to the Lowest price possible to be discount
  // if (discount.getMinOrderValue() >
  // totalPriceOfAllProducVariationtInDiscountProgram) {
  // throw new ResourceNotFoundException("The total price of product selected in
  // this cart insufficient");
  // }

  // // Update data before sending response
  // if (applyDiscountStatus.equals(ApplyDiscountStatus.COMMIT)) {
  // discount.getCustomer().add(customer);
  // discount.setUsesCount(discount.getUsesCount() + 1);
  // this.discountRepository.save(discount);
  // }

  // // Get the final total price
  // Double finalPrice =
  // discount.getDiscountType().equals(DiscountType.FIXED_AMOUNT) ?
  // discount.getValue()
  // : totalPriceOfAllProduct - totalPriceOfAllProducVariationtInDiscountProgram *
  // discount.getValue() / 100;

  // return
  // PriceStatisticsResponse.builder().finalPrice(finalPrice).totalPrice(totalPriceOfAllProduct)
  // .discountId(applyDiscountRequest.getDiscountId()).amountProductReduced(totalPriceOfAllProduct
  // - finalPrice)
  // .build();
  // return null;
  // }

  // @Override
  // public void handleCancelDiscountForCart(Discount discount, UUID customerId) {
  // EDiscount eDiscount =
  // OptionalExtractor.extractFromResource(this.discountRepository.findByIdAndIsActiveTrue(discount.getId()));
  // We can add relationship between CART and DISCOUNT for easy to analyze when
  // toggle cart page

  // CartEntity cart =
  // this.cartRepository.findByShopIdAndCustomerId(discount.getShop().getId(),
  // customer.getId()).orElseThrow(() -> new ResourceNotFoundException("Not found
  // cart"));

  // Boolean res = discount.getCustomer().remove(customer);
  // if (!res) {
  // throw new ResourceNotFoundException("Cancel discount for cart failed");
  // }
  // discount.setUsesCount(discount.getUsesCount() - 1);

  // discountRepository.save(discount);
  // }

}
