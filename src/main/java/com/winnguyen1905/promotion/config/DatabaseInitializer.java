package com.winnguyen1905.promotion.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.persistance.entity.ECommissionPayout;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.entity.EPromotion;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EShopPromotionParticipation;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.repository.CommissionPayoutRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ProductDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionParticipationRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  // Fixed UUIDs for consistent testing
  private static final UUID PROMOTION_PROGRAM_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID PROMOTION_PROGRAM_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID SHOP_1 = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final UUID SHOP_2 = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final UUID ORDER_1 = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID ORDER_2 = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final UUID CUSTOMER_1 = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final UUID CUSTOMER_2 = UUID.fromString("88888888-8888-8888-8888-888888888888");
  private static final UUID PRODUCT_1 = UUID.fromString("99999999-9999-9999-9999-999999999999");
  private static final UUID PRODUCT_2 = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private final DiscountRepository discountRepository;
  private final DiscountUsageRepository discountUsageRepository;
  private final ProductDiscountRepository productDiscountRepository;
  private final UserDiscountRepository userDiscountRepository;
  private final PromotionProgramRepository promotionProgramRepository;
  private final ShopPromotionParticipationRepository shopPromotionParticipationRepository;
  private final CommissionPayoutRepository commissionPayoutRepository;

  // Fixed UUIDs for discounts
  private static final UUID SUMMER_SALE_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID WELCOME_DISCOUNT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
  private static final UUID FREE_SHIPPING_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
  private static final UUID BLACK_FRIDAY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");

  @Override
  public void run(String... args) throws Exception {
    initializeDiscounts();
  }

  private void initializeDiscounts() {
    // Only create discounts if they don't exist
    if (discountRepository.count() == 0) {
      // Summer Sale - 20% off on selected products
      EDiscount summerSale = EDiscount.builder()
          .id(SUMMER_SALE_ID)
          .name("Summer Sale 2023")
          .description("Get 20% off on selected summer collection items")
          .discountCategory(DiscountCategory.PRODUCT)
          .discountType(DiscountType.PERCENTAGE)
          .value(20.0)
          .maxReducedValue(50.0)
          .code("SUMMER20")
          .startDate(Instant.now())
          .endDate(Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS))
          .usageLimit(1000)
          .usageCount(0)
          .limitUsagePerCutomer(2)
          .minOrderValue(50.0)
          .isActive(true)
          .appliesTo(ApplyDiscountType.ALL)
          .creatorType(EDiscount.CreatorType.ADMIN)
          .build();

      // Welcome Discount - $10 off for new customers
      EDiscount welcomeDiscount = EDiscount.builder()
          .id(WELCOME_DISCOUNT_ID)
          .name("Welcome Discount")
          .description("$10 off on your first order")
          .discountCategory(DiscountCategory.PRODUCT)
          .discountType(DiscountType.FIXED_AMOUNT)
          .value(10.0)
          .maxReducedValue(10.0)
          .code("WELCOME10")
          .startDate(Instant.now())
          .endDate(Instant.now().plus(90, java.time.temporal.ChronoUnit.DAYS))
          .usageLimit(5000)
          .usageCount(0)
          .limitUsagePerCutomer(1)
          .minOrderValue(49.99)
          .isActive(true)
          .appliesTo(ApplyDiscountType.ALL)
          .creatorType(EDiscount.CreatorType.ADMIN)
          .build();

      // Free Shipping - Free shipping on orders over $50
      EDiscount freeShipping = EDiscount.builder()
          .id(FREE_SHIPPING_ID)
          .name("Free Shipping")
          .description("Free shipping on all orders over $50")
          .discountCategory(DiscountCategory.SHIPPING)
          .discountType(DiscountType.PERCENTAGE)
          .value(100.0)
          .maxReducedValue(15.0)
          .code("FREESHIP")
          .startDate(Instant.now())
          .endDate(Instant.now().plus(60, java.time.temporal.ChronoUnit.DAYS))
          .usageLimit(10000)
          .usageCount(0)
          .limitUsagePerCutomer(5)
          .minOrderValue(50.0)
          .isActive(true)
          .appliesTo(ApplyDiscountType.ALL)
          .creatorType(EDiscount.CreatorType.ADMIN)
          .build();

      // Black Friday Special - 30% off sitewide
      EDiscount blackFriday = EDiscount.builder()
          .id(BLACK_FRIDAY_ID)
          .name("Black Friday Special")
          .description("30% off on all items")
          .discountCategory(DiscountCategory.PRODUCT)
          .discountType(DiscountType.PERCENTAGE)
          .value(30.0)
          .maxReducedValue(100.0)
          .code("BLACKFRIDAY30")
          .startDate(Instant.now())
          .endDate(Instant.now().plus(3, java.time.temporal.ChronoUnit.DAYS))
          .usageLimit(5000)
          .usageCount(0)
          .limitUsagePerCutomer(3)
          .minOrderValue(0.0)
          .isActive(true)
          .appliesTo(ApplyDiscountType.ALL)
          .creatorType(EDiscount.CreatorType.ADMIN)
          .build();

      // Save all discounts
      // List<EDiscount> savedDiscounts = discountRepository.saveAll(List.of(
      // summerSale,
      // welcomeDiscount,
      // freeShipping,
      // blackFriday));

      // // Optionally create product discounts for the summer sale
      // EProductDiscount productDiscount1 = EProductDiscount.builder()
      // .productId(PRODUCT_1)
      // .discount(summerSale)
      // .build();

      // EProductDiscount productDiscount2 = EProductDiscount.builder()
      // .productId(PRODUCT_2)
      // .discount(summerSale)
      // .build();

      EUserDiscount userDiscount = EUserDiscount.builder()
          .customerId(UUID.fromString("11111111-1111-4111-8111-111111111111"))
          .discount(summerSale)
          .build();

      EUserDiscount userDiscount2 = EUserDiscount.builder()
          .customerId(UUID.fromString("11111111-1111-4111-8111-111111111112"))
          .discount(freeShipping)
          .build();

      EUserDiscount userDiscount3 = EUserDiscount.builder()
          .customerId(UUID.fromString("11111111-1111-4111-8111-111111111113"))
          .discount(blackFriday)
          .build();

      summerSale.getUserDiscounts().add(userDiscount);
      freeShipping.getUserDiscounts().add(userDiscount2);
      blackFriday.getUserDiscounts().add(userDiscount3);
      discountRepository.saveAll(List.of(summerSale, welcomeDiscount, freeShipping, blackFriday));

      // userDiscountRepository.saveAll(List.of(userDiscount, userDiscount2, userDiscount3));

    }
  }

  private void createSampleShopParticipations(List<EPromotionProgram> programs) {
    List<EShopPromotionParticipation> participations = new ArrayList<>();

    participations.add(EShopPromotionParticipation.builder()
        .shopId(SHOP_1)
        .program(programs.get(0))
        .status(EShopPromotionParticipation.Status.APPROVED)
        .discountValue(25.0)
        .acceptedTerms(true)
        .build());

    participations.add(EShopPromotionParticipation.builder()
        .shopId(SHOP_2)
        .program(programs.get(1))
        .status(EShopPromotionParticipation.Status.PENDING)
        .discountValue(30.0)
        .acceptedTerms(false)
        .build());

    shopPromotionParticipationRepository.saveAll(participations);
  }

  private void createSampleCommissionPayouts(List<EPromotionProgram> programs) {
    List<ECommissionPayout> payouts = new ArrayList<>();

    payouts.add(ECommissionPayout.builder()
        .shopId(SHOP_1)
        .program(programs.get(0))
        .orderId(ORDER_1)
        .amount(150.0)
        .paid(true)
        .paymentDate(LocalDateTime.now())
        .build());

    payouts.add(ECommissionPayout.builder()
        .shopId(SHOP_2)
        .program(programs.get(1))
        .orderId(ORDER_2)
        .amount(200.0)
        .paid(false)
        .build());

    commissionPayoutRepository.saveAll(payouts);
  }

  private List<EPromotion> createSamplePromotions() {
    List<EPromotion> promotions = new ArrayList<>();

    for (int i = 1; i <= 10; i++) {
      EPromotion promotion = EPromotion.builder()
          .id(UUID.nameUUIDFromBytes(("promo" + i).getBytes()))
          .promotionName("Promotion " + i)
          .baseCommissionRate(String.valueOf(5 + i))
          .build();
      promotions.add(promotion);
    }

    return promotions;
  }

  private List<EDiscount> createSampleDiscounts() {
    List<EDiscount> discounts = new ArrayList<>();
    Set<String> categoryNames = new HashSet<>();

    for (int i = 1; i <= 20; i++) {
      categoryNames.clear();
      categoryNames.add("Category" + i);
      categoryNames.add("Category" + (i + 1));

      EDiscount discount = EDiscount.builder()
          .id(UUID.nameUUIDFromBytes(("discount" + i).getBytes()))
          .creatorType(i % 2 == 0 ? EDiscount.CreatorType.ADMIN : EDiscount.CreatorType.SHOP)
          .discountType(i % 2 == 0 ? DiscountType.PERCENTAGE : DiscountType.FIXED_AMOUNT)
          .appliesTo(
              i % 3 == 0 ? ApplyDiscountType.ALL : i % 3 == 1 ? ApplyDiscountType.SPECIFIC : ApplyDiscountType.CATEGORY)
          .discountCategory(i % 2 == 0 ? DiscountCategory.PRODUCT : DiscountCategory.SHIPPING)
          .name("Discount " + i)
          .description("Description for discount " + i)
          .value(i % 2 == 0 ? 10.0 * i : 5.0 * i)
          .maxReducedValue(100.0 * i)
          .code("CODE" + i)
          .startDate(Instant.now())
          .endDate(Instant.now().plusSeconds(86400 * 30)) // 30 days from now
          .usageLimit(100)
          .usageCount(0)
          .limitUsagePerCutomer(3)
          .minOrderValue(50.0 * i)
          .isActive(true)
          .shopId(i % 2 == 0 ? SHOP_1 : SHOP_2)
          // .categoryNames(categoryNames)
          .build();

      discounts.add(discount);
    }

    return discountRepository.saveAll(discounts);
  }

  private void createSampleProductDiscounts(List<EDiscount> discounts) {
    List<EProductDiscount> productDiscounts = new ArrayList<>();

    for (int i = 1; i <= 30; i++) {
      EProductDiscount productDiscount = EProductDiscount.builder()
          .id(UUID.nameUUIDFromBytes(("prod_disc" + i).getBytes()))
          .discount(discounts.get(i % discounts.size()))
          .productId(i % 2 == 0 ? PRODUCT_1 : PRODUCT_2)
          .build();

      productDiscounts.add(productDiscount);
    }

    productDiscountRepository.saveAll(productDiscounts);
  }

  private void createSampleUserDiscounts(List<EDiscount> discounts) {
    List<EUserDiscount> userDiscounts = new ArrayList<>();

    for (int i = 1; i <= 20; i++) {
      EUserDiscount userDiscount = EUserDiscount.builder()
          .id(UUID.nameUUIDFromBytes(("user_disc" + i).getBytes()))
          .discount(discounts.get(i % discounts.size()))
          .customerId(i % 2 == 0 ? CUSTOMER_1 : CUSTOMER_2)
          .build();

      userDiscounts.add(userDiscount);
    }

    userDiscountRepository.saveAll(userDiscounts);
  }

  private void createSampleDiscountUsages(List<EDiscount> discounts) {
    List<EDiscountUsage> discountUsages = new ArrayList<>();

    for (int i = 1; i <= 20; i++) {
      EDiscountUsage discountUsage = EDiscountUsage.builder()
          .id(UUID.nameUUIDFromBytes(("usage" + i).getBytes()))
          .orderId(i % 2 == 0 ? ORDER_1 : ORDER_2)
          .customerId(i % 2 == 0 ? CUSTOMER_1 : CUSTOMER_2)
          .discount(discounts.get(i % discounts.size()))
          // .usageStatus(i % 2 == 0)
          .build();

      discountUsages.add(discountUsage);
    }

    discountUsageRepository.saveAll(discountUsages);
  }
}
