package com.winnguyen1905.promotion.config;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnguyen1905.promotion.common.ApplyDiscountType;
import com.winnguyen1905.promotion.common.DiscountCategory;
import com.winnguyen1905.promotion.common.DiscountType;
import com.winnguyen1905.promotion.common.DiscountUsageStatus;
import com.winnguyen1905.promotion.persistance.entity.ECampaign;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EDiscountUsage;
import com.winnguyen1905.promotion.persistance.entity.EFlashSale;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAction;
import com.winnguyen1905.promotion.persistance.entity.EPromotionAnalytics;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProduct;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EPromotionRule;
import com.winnguyen1905.promotion.persistance.entity.EUserDiscount;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation;
import com.winnguyen1905.promotion.persistance.repository.CampaignRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.FlashSaleRepository;
import com.winnguyen1905.promotion.persistance.repository.ProductDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionActionRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionAnalyticsRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionCommissionRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProductRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionRuleRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.VendorPromotionParticipationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  // Fixed UUIDs for consistent testing from Product Service Environment
  private static final UUID SHOP_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
  private static final UUID VENDOR_US_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
  private static final UUID VENDOR_EU_ID = UUID.fromString("20000000-0000-4000-8000-000000000001");
  private static final UUID VENDOR_ASIA_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
  private static final UUID BRAND_NIKE_ID = UUID.fromString("01000000-0000-4000-8000-000000000001");
  private static final UUID BRAND_ADIDAS_ID = UUID.fromString("01000000-0000-4000-8000-000000000002");
  private static final UUID BRAND_APPLE_ID = UUID.fromString("01000000-0000-4000-8000-000000000003");
  private static final UUID BRAND_SAMSUNG_ID = UUID.fromString("01000000-0000-4000-8000-000000000004");
  private static final UUID BRAND_IKEA_ID = UUID.fromString("01000000-0000-4000-8000-000000000005");
  private static final UUID BRAND_SONY_ID = UUID.fromString("01000000-0000-4000-8000-000000000006");
  
  // Categories
  private static final UUID CAT_ELECTRONICS_ID = UUID.fromString("02000000-0000-4000-8000-000000000001");
  private static final UUID CAT_SMARTPHONES_ID = UUID.fromString("02000000-0000-4000-8000-000000000002");
  private static final UUID CAT_LAPTOPS_ID = UUID.fromString("02000000-0000-4000-8000-000000000003");
  private static final UUID CAT_FASHION_ID = UUID.fromString("02000000-0000-4000-8000-000000000004");
  private static final UUID CAT_SHOES_ID = UUID.fromString("02000000-0000-4000-8000-000000000005");
  private static final UUID CAT_CLOTHING_ID = UUID.fromString("02000000-0000-4000-8000-000000000006");
  private static final UUID CAT_FURNITURE_ID = UUID.fromString("02000000-0000-4000-8000-000000000007");
  private static final UUID CAT_HOME_OFFICE_ID = UUID.fromString("02000000-0000-4000-8000-000000000008");
  
  // Products
  private static final UUID IPHONE_15_ID = UUID.fromString("03000000-0000-4000-8000-000000000001");
  private static final UUID SAMSUNG_S24_ID = UUID.fromString("03000000-0000-4000-8000-000000000002");
  private static final UUID NIKE_AIR_MAX_ID = UUID.fromString("03000000-0000-4000-8000-000000000003");
  private static final UUID ADIDAS_ULTRABOOST_ID = UUID.fromString("03000000-0000-4000-8000-000000000004");
  private static final UUID IKEA_DESK_ID = UUID.fromString("03000000-0000-4000-8000-000000000005");
  private static final UUID SONY_HEADPHONES_ID = UUID.fromString("03000000-0000-4000-8000-000000000006");
  
  // Product Variants
  private static final UUID IPHONE_15_PINK_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000001");
  private static final UUID IPHONE_15_BLUE_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000002");
  private static final UUID IPHONE_15_BLACK_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000003");
  private static final UUID GALAXY_GRAY_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000004");
  private static final UUID GALAXY_VIOLET_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000005");
  private static final UUID NIKE_BLACK_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000006");
  private static final UUID NIKE_WHITE_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000007");
  private static final UUID NIKE_RED_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000008");
  private static final UUID ADIDAS_BLACK_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000009");
  private static final UUID ADIDAS_WHITE_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000010");
  private static final UUID IKEA_WHITE_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000011");
  private static final UUID IKEA_BROWN_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000012");
  private static final UUID SONY_BLACK_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000013");
  private static final UUID SONY_SILVER_VARIANT_ID = UUID.fromString("04000000-0000-4000-8000-000000000014");
  
  // Customer IDs for testing
  private static final UUID CUSTOMER_1_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
  private static final UUID CUSTOMER_2_ID = UUID.fromString("11111111-1111-4111-8111-111111111112");
  private static final UUID CUSTOMER_3_ID = UUID.fromString("11111111-1111-4111-8111-111111111113");
  
  // Order IDs for testing
  private static final UUID ORDER_1_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final UUID ORDER_2_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final UUID ORDER_3_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

  private final CampaignRepository campaignRepository;
  private final PromotionProgramRepository promotionProgramRepository;
  private final DiscountRepository discountRepository;
  private final UserDiscountRepository userDiscountRepository;
  private final ProductDiscountRepository productDiscountRepository;
  private final DiscountUsageRepository discountUsageRepository;
  private final FlashSaleRepository flashSaleRepository;
  private final VendorPromotionParticipationRepository vendorParticipationRepository;
  private final PromotionRuleRepository promotionRuleRepository;
  private final PromotionActionRepository promotionActionRepository;
  private final PromotionProductRepository promotionProductRepository;
  private final PromotionCommissionRepository promotionCommissionRepository;
  private final PromotionAnalyticsRepository promotionAnalyticsRepository;
  
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    if (campaignRepository.count() == 0) {
      initializeComprehensiveData();
    }
  }

  private void initializeComprehensiveData() throws Exception {
    // 1. Create Campaigns
    List<ECampaign> campaigns = createCampaigns();
    
    // 2. Create Promotion Programs
    List<EPromotionProgram> programs = createPromotionPrograms(campaigns);
    
    // 3. Create Promotion Rules
    createPromotionRules(programs);
    
    // 4. Create Promotion Actions
    createPromotionActions(programs);
    
    // 5. Create Discounts
    List<EDiscount> discounts = createDiscounts(programs);
    
    // 6. Create User Discounts (Customer Claims)
    createUserDiscounts(discounts);
    
    // 7. Create Product Discounts
    createProductDiscounts(discounts);
    
    // 8. Create Vendor Participations
    createVendorParticipations(programs);
    
    // 9. Create Promotion Products
    createPromotionProducts(programs);
    
    // 10. Create Flash Sales
    createFlashSales(programs);
    
    // 11. Create Discount Usage
    createDiscountUsage(discounts);
    
    // 12. Create Promotion Commissions
    createPromotionCommissions(programs);
    
    // 13. Create Promotion Analytics
    createPromotionAnalytics(programs);
  }

  private List<ECampaign> createCampaigns() throws Exception {
    List<ECampaign> campaigns = new ArrayList<>();
    
    // Black Friday Campaign
    JsonNode blackFridayAudience = objectMapper.readTree("""
        {
          "customerTiers": ["PREMIUM", "VIP"],
          "regions": ["US", "EU"],
          "ageRange": {"min": 18, "max": 65},
          "minOrderHistory": 3
        }
        """);
        
    ECampaign blackFriday = ECampaign.builder()
        .id(UUID.fromString("10000000-1000-1000-1000-100000000001"))
        .name("Black Friday 2024")
        .description("The biggest sale of the year with up to 70% off")
        .campaignType(ECampaign.CampaignType.SEASONAL)
        .startDate(Instant.now().plusSeconds(86400)) // Tomorrow
        .endDate(Instant.now().plusSeconds(86400 * 4)) // 4 days from now
        .status(ECampaign.CampaignStatus.SCHEDULED)
        .budget(100000.0)
        .spentBudget(0.0)
        .targetAudience(blackFridayAudience)
        .createdBy(VENDOR_US_ID)
        .build();
    campaigns.add(blackFriday);
    
    // Summer Sale Campaign
    JsonNode summerAudience = objectMapper.readTree("""
        {
          "customerTiers": ["REGULAR", "PREMIUM"],
          "regions": ["US", "EU", "ASIA"],
          "categories": ["FASHION", "SHOES"]
        }
        """);
        
    ECampaign summerSale = ECampaign.builder()
        .id(UUID.fromString("10000000-1000-1000-1000-100000000002"))
        .name("Summer Fashion Sale")
        .description("Beat the heat with cool summer fashion deals")
        .campaignType(ECampaign.CampaignType.SEASONAL)
        .startDate(Instant.now())
        .endDate(Instant.now().plusSeconds(86400 * 30)) // 30 days
        .status(ECampaign.CampaignStatus.ACTIVE)
        .budget(50000.0)
        .spentBudget(12000.0)
        .targetAudience(summerAudience)
        .createdBy(VENDOR_EU_ID)
        .build();
    campaigns.add(summerSale);
    
    // Tech Flash Sale Campaign
    JsonNode techAudience = objectMapper.readTree("""
        {
          "customerTiers": ["PREMIUM", "VIP"],
          "regions": ["US", "ASIA"],
          "categories": ["ELECTRONICS", "SMARTPHONES"],
          "minOrderValue": 500
        }
        """);
        
    ECampaign techFlash = ECampaign.builder()
        .id(UUID.fromString("10000000-1000-1000-1000-100000000003"))
        .name("Tech Flash Sale")
        .description("Limited time offers on premium electronics")
        .campaignType(ECampaign.CampaignType.FLASH_SALE)
        .startDate(Instant.now().plusSeconds(3600)) // 1 hour from now
        .endDate(Instant.now().plusSeconds(86400)) // 24 hours from now
        .status(ECampaign.CampaignStatus.SCHEDULED)
        .budget(25000.0)
        .spentBudget(0.0)
        .targetAudience(techAudience)
        .createdBy(VENDOR_ASIA_ID)
        .build();
    campaigns.add(techFlash);
    
    return campaignRepository.saveAll(campaigns);
  }

  private List<EPromotionProgram> createPromotionPrograms(List<ECampaign> campaigns) {
    List<EPromotionProgram> programs = new ArrayList<>();
    
    // Black Friday Program
    EPromotionProgram blackFridayProgram = EPromotionProgram.builder()
        .id(UUID.fromString("20000000-2000-2000-2000-200000000001"))
        .campaign(campaigns.get(0))
        .name("Black Friday Mega Deals")
        .description("Stackable discounts for Black Friday")
        .programType(EPromotionProgram.ProgramType.DISCOUNT)
        .startDate(campaigns.get(0).getStartDate())
        .endDate(campaigns.get(0).getEndDate())
        .priority(1)
        .isStackable(true)
        .platformCommissionRate(0.05)
        .requiredVendorContribution(30.0)
        .visibility(EPromotionProgram.Visibility.PUBLIC)
        .usageLimitGlobal(10000)
        .usageCountGlobal(0)
        .termsConditions("Terms and conditions for Black Friday promotion")
        .autoApply(false)
        .status(EPromotionProgram.ProgramStatus.ACTIVE)
        .createdBy(VENDOR_US_ID)
        .build();
    programs.add(blackFridayProgram);
    
    // Summer Fashion Program
    EPromotionProgram summerProgram = EPromotionProgram.builder()
        .id(UUID.fromString("20000000-2000-2000-2000-200000000002"))
        .campaign(campaigns.get(1))
        .name("Summer Fashion Bonanza")
        .description("Fashion-focused summer promotions")
        .programType(EPromotionProgram.ProgramType.DISCOUNT)
        .startDate(campaigns.get(1).getStartDate())
        .endDate(campaigns.get(1).getEndDate())
        .priority(2)
        .isStackable(false)
        .platformCommissionRate(0.03)
        .requiredVendorContribution(20.0)
        .visibility(EPromotionProgram.Visibility.PUBLIC)
        .usageLimitGlobal(5000)
        .usageCountGlobal(1200)
        .termsConditions("Summer sale terms and conditions")
        .autoApply(true)
        .status(EPromotionProgram.ProgramStatus.ACTIVE)
        .createdBy(VENDOR_EU_ID)
        .build();
    programs.add(summerProgram);
    
    // Tech Flash Program
    EPromotionProgram techProgram = EPromotionProgram.builder()
        .id(UUID.fromString("20000000-2000-2000-2000-200000000003"))
        .campaign(campaigns.get(2))
        .name("Tech Flash Lightning Deals")
        .description("Quick flash deals on premium tech")
        .programType(EPromotionProgram.ProgramType.DISCOUNT)
        .startDate(campaigns.get(2).getStartDate())
        .endDate(campaigns.get(2).getEndDate())
        .priority(1)
        .isStackable(false)
        .platformCommissionRate(0.08)
        .requiredVendorContribution(40.0)
        .visibility(EPromotionProgram.Visibility.VIP_ONLY)
        .usageLimitGlobal(500)
        .usageCountGlobal(0)
        .termsConditions("Flash sale terms - limited quantities")
        .autoApply(false)
        .status(EPromotionProgram.ProgramStatus.DRAFT)
        .createdBy(VENDOR_ASIA_ID)
        .build();
    programs.add(techProgram);
    
    // Cashback Program
    EPromotionProgram cashbackProgram = EPromotionProgram.builder()
        .id(UUID.fromString("20000000-2000-2000-2000-200000000004"))
        .name("Customer Loyalty Cashback")
        .description("Earn cashback on every purchase")
        .programType(EPromotionProgram.ProgramType.CASHBACK)
        .startDate(Instant.now().minusSeconds(86400 * 30)) // Started 30 days ago
        .endDate(Instant.now().plusSeconds(86400 * 365)) // Ends in 1 year
        .priority(3)
        .isStackable(true)
        .platformCommissionRate(0.02)
        .requiredVendorContribution(10.0)
        .visibility(EPromotionProgram.Visibility.PUBLIC)
        .usageLimitGlobal(null) // No limit
        .usageCountGlobal(5000)
        .termsConditions("Cashback program terms")
        .autoApply(true)
        .status(EPromotionProgram.ProgramStatus.ACTIVE)
        .createdBy(VENDOR_US_ID)
        .build();
    programs.add(cashbackProgram);
    
    return promotionProgramRepository.saveAll(programs);
  }

  private void createPromotionRules(List<EPromotionProgram> programs) throws Exception {
    List<EPromotionRule> rules = new ArrayList<>();
    
    // Rules for Black Friday Program
    EPromotionRule blackFridayMinOrder = EPromotionRule.builder()
        .program(programs.get(0))
        .ruleType(EPromotionRule.RuleType.MIN_ORDER_VALUE)
        .operator(EPromotionRule.Operator.GREATER_THAN)
        .value(objectMapper.readTree("100.0"))
        .isRequired(true)
        .build();
    rules.add(blackFridayMinOrder);
    
    EPromotionRule blackFridayCustomerTier = EPromotionRule.builder()
        .program(programs.get(0))
        .ruleType(EPromotionRule.RuleType.CUSTOMER_TIER)
        .operator(EPromotionRule.Operator.IN)
        .value(objectMapper.readTree("[\"PREMIUM\", \"VIP\"]"))
        .isRequired(false)
        .build();
    rules.add(blackFridayCustomerTier);
    
    // Rules for Summer Program
    EPromotionRule summerCategory = EPromotionRule.builder()
        .program(programs.get(1))
        .ruleType(EPromotionRule.RuleType.PRODUCT_CATEGORY)
        .operator(EPromotionRule.Operator.IN)
        .value(objectMapper.readTree("[\"FASHION\", \"SHOES\"]"))
        .isRequired(true)
        .build();
    rules.add(summerCategory);
    
    // Rules for Tech Program
    EPromotionRule techMinOrder = EPromotionRule.builder()
        .program(programs.get(2))
        .ruleType(EPromotionRule.RuleType.MIN_ORDER_VALUE)
        .operator(EPromotionRule.Operator.GREATER_THAN)
        .value(objectMapper.readTree("500.0"))
        .isRequired(true)
        .build();
    rules.add(techMinOrder);
    
    EPromotionRule techCategory = EPromotionRule.builder()
        .program(programs.get(2))
        .ruleType(EPromotionRule.RuleType.PRODUCT_CATEGORY)
        .operator(EPromotionRule.Operator.IN)
        .value(objectMapper.readTree("[\"ELECTRONICS\", \"SMARTPHONES\"]"))
        .isRequired(true)
        .build();
    rules.add(techCategory);
    
    promotionRuleRepository.saveAll(rules);
  }

  private void createPromotionActions(List<EPromotionProgram> programs) throws Exception {
    List<EPromotionAction> actions = new ArrayList<>();
    
    // Actions for Black Friday Program
    EPromotionAction blackFridayDiscount = EPromotionAction.builder()
        .program(programs.get(0))
        .actionType(EPromotionAction.ActionType.PERCENTAGE_DISCOUNT)
        .target(EPromotionAction.Target.ORDER_TOTAL)
        .value(30.0)
        .maxDiscountAmount(200.0)
        .appliesTo(objectMapper.readTree("{\"categories\": [\"ALL\"]}"))
        .build();
    actions.add(blackFridayDiscount);
    
    EPromotionAction blackFridayShipping = EPromotionAction.builder()
        .program(programs.get(0))
        .actionType(EPromotionAction.ActionType.FREE_SHIPPING)
        .target(EPromotionAction.Target.SHIPPING)
        .value(100.0)
        .maxDiscountAmount(50.0)
        .appliesTo(objectMapper.readTree("{\"minOrderValue\": 150}"))
        .build();
    actions.add(blackFridayShipping);
    
    // Actions for Summer Program
    EPromotionAction summerDiscount = EPromotionAction.builder()
        .program(programs.get(1))
        .actionType(EPromotionAction.ActionType.PERCENTAGE_DISCOUNT)
        .target(EPromotionAction.Target.ORDER_TOTAL)
        .value(25.0)
        .maxDiscountAmount(100.0)
        .appliesTo(objectMapper.readTree("{\"categories\": [\"FASHION\", \"SHOES\"]}"))
        .build();
    actions.add(summerDiscount);
    
    // Actions for Tech Program
    EPromotionAction techDiscount = EPromotionAction.builder()
        .program(programs.get(2))
        .actionType(EPromotionAction.ActionType.FIXED_DISCOUNT)
        .target(EPromotionAction.Target.ORDER_TOTAL)
        .value(200.0)
        .maxDiscountAmount(200.0)
        .appliesTo(objectMapper.readTree("{\"categories\": [\"ELECTRONICS\"]}"))
        .build();
    actions.add(techDiscount);
    
    // Actions for Cashback Program
    EPromotionAction cashbackAction = EPromotionAction.builder()
        .program(programs.get(3))
        .actionType(EPromotionAction.ActionType.CASHBACK_PERCENTAGE)
        .target(EPromotionAction.Target.ORDER_TOTAL)
        .value(5.0)
        .maxDiscountAmount(50.0)
        .appliesTo(objectMapper.readTree("{\"categories\": [\"ALL\"]}"))
        .build();
    actions.add(cashbackAction);
    
    promotionActionRepository.saveAll(actions);
  }

  private List<EDiscount> createDiscounts(List<EPromotionProgram> programs) {
    List<EDiscount> discounts = new ArrayList<>();
    
    // Black Friday Discounts
    EDiscount blackFriday30 = EDiscount.builder()
        .id(UUID.fromString("30000000-3000-3000-3000-300000000001"))
        .program(programs.get(0))
        .creatorType(EDiscount.CreatorType.ADMIN)
        .creatorId(VENDOR_US_ID)
        .discountType(DiscountType.PERCENTAGE)
        .appliesTo(ApplyDiscountType.ALL)
        .discountCategory(DiscountCategory.PRODUCT)
        .name("Black Friday 30% Off")
        .description("30% off everything for Black Friday")
        .code("BLACKFRIDAY30")
        .value(30.0)
        .maxDiscountAmount(200.0)
        .minOrderValue(100.0)
        .startDate(Instant.now().plusSeconds(86400))
        .endDate(Instant.now().plusSeconds(86400 * 4))
        .usageLimitTotal(5000)
        .usageLimitPerCustomer(1)
        .usageCount(0)
        .isActive(true)
        .isPublic(true)
        .autoApply(false)
        .build();
    discounts.add(blackFriday30);
    
    EDiscount freeShipping = EDiscount.builder()
        .id(UUID.fromString("30000000-3000-3000-3000-300000000002"))
        .program(programs.get(0))
        .creatorType(EDiscount.CreatorType.ADMIN)
        .creatorId(VENDOR_US_ID)
        .discountType(DiscountType.PERCENTAGE)
        .appliesTo(ApplyDiscountType.ALL)
        .discountCategory(DiscountCategory.SHIPPING)
        .name("Free Shipping Black Friday")
        .description("Free shipping on orders over $150")
        .code("FREESHIP150")
        .value(100.0)
        .maxDiscountAmount(50.0)
        .minOrderValue(150.0)
        .startDate(Instant.now().plusSeconds(86400))
        .endDate(Instant.now().plusSeconds(86400 * 4))
        .usageLimitTotal(null)
        .usageLimitPerCustomer(null)
        .usageCount(0)
        .isActive(true)
        .isPublic(true)
        .autoApply(true)
        .build();
    discounts.add(freeShipping);
    
    // Summer Fashion Discounts
    EDiscount summer25 = EDiscount.builder()
        .id(UUID.fromString("30000000-3000-3000-3000-300000000003"))
        .program(programs.get(1))
        .creatorType(EDiscount.CreatorType.VENDOR)
        .creatorId(VENDOR_EU_ID)
        .vendorId(VENDOR_EU_ID)
        .discountType(DiscountType.PERCENTAGE)
        .appliesTo(ApplyDiscountType.CATEGORY)
        .discountCategory(DiscountCategory.PRODUCT)
        .name("Summer Fashion 25% Off")
        .description("25% off summer fashion and shoes")
        .code("SUMMER25")
        .value(25.0)
        .maxDiscountAmount(100.0)
        .minOrderValue(75.0)
        .startDate(Instant.now().minusSeconds(86400))
        .endDate(Instant.now().plusSeconds(86400 * 29))
        .usageLimitTotal(2000)
        .usageLimitPerCustomer(3)
        .usageCount(450)
        .isActive(true)
        .isPublic(true)
        .autoApply(true)
        .build();
    discounts.add(summer25);
    
    // Tech Flash Discount
    EDiscount tech200 = EDiscount.builder()
        .id(UUID.fromString("30000000-3000-3000-3000-300000000004"))
        .program(programs.get(2))
        .creatorType(EDiscount.CreatorType.VENDOR)
        .creatorId(VENDOR_ASIA_ID)
        .vendorId(VENDOR_ASIA_ID)
        .discountType(DiscountType.FIXED_AMOUNT)
        .appliesTo(ApplyDiscountType.SPECIFIC)
        .discountCategory(DiscountCategory.PRODUCT)
        .name("Tech Flash $200 Off")
        .description("$200 off premium electronics")
        .code("TECH200")
        .value(200.0)
        .maxDiscountAmount(200.0)
        .minOrderValue(500.0)
        .startDate(Instant.now().plusSeconds(3600))
        .endDate(Instant.now().plusSeconds(86400))
        .usageLimitTotal(100)
        .usageLimitPerCustomer(1)
        .usageCount(0)
        .isActive(true)
        .isPublic(false)
        .autoApply(false)
        .build();
    discounts.add(tech200);
    
    // Cashback Discount
    EDiscount cashback5 = EDiscount.builder()
        .id(UUID.fromString("30000000-3000-3000-3000-300000000005"))
        .program(programs.get(3))
        .creatorType(EDiscount.CreatorType.ADMIN)
        .creatorId(VENDOR_US_ID)
        .discountType(DiscountType.PERCENTAGE)
        .appliesTo(ApplyDiscountType.ALL)
        .discountCategory(DiscountCategory.PRODUCT)
        .name("5% Cashback")
        .description("Earn 5% cashback on every purchase")
        .code("CASHBACK5")
        .value(5.0)
        .maxDiscountAmount(50.0)
        .minOrderValue(25.0)
        .startDate(Instant.now().minusSeconds(86400 * 30))
        .endDate(Instant.now().plusSeconds(86400 * 365))
        .usageLimitTotal(null)
        .usageLimitPerCustomer(null)
        .usageCount(2500)
        .isActive(true)
        .isPublic(true)
        .autoApply(true)
        .build();
    discounts.add(cashback5);
    
    // Welcome Discount
    EDiscount welcome10 = EDiscount.builder()
        .id(UUID.fromString("30000000-3000-3000-3000-300000000006"))
        .creatorType(EDiscount.CreatorType.ADMIN)
        .creatorId(VENDOR_US_ID)
        .discountType(DiscountType.FIXED_AMOUNT)
        .appliesTo(ApplyDiscountType.ALL)
        .discountCategory(DiscountCategory.PRODUCT)
        .name("Welcome $10 Off")
        .description("$10 off for new customers")
        .code("WELCOME10")
        .value(10.0)
        .maxDiscountAmount(10.0)
        .minOrderValue(50.0)
        .startDate(Instant.now().minusSeconds(86400 * 7))
        .endDate(Instant.now().plusSeconds(86400 * 90))
        .usageLimitTotal(1000)
        .usageLimitPerCustomer(1)
        .usageCount(234)
        .isActive(true)
        .isPublic(true)
        .autoApply(false)
        .build();
    discounts.add(welcome10);
    
    return discountRepository.saveAll(discounts);
  }

  private void createUserDiscounts(List<EDiscount> discounts) {
    List<EUserDiscount> userDiscounts = new ArrayList<>();
    
    // Customer 1 has claimed Black Friday and Welcome discounts
    userDiscounts.add(EUserDiscount.builder()
        .customerId(CUSTOMER_1_ID)
        .discount(discounts.get(0)) // Black Friday 30%
        .build());
        
    userDiscounts.add(EUserDiscount.builder()
        .customerId(CUSTOMER_1_ID)
        .discount(discounts.get(5)) // Welcome $10
        .build());
    
    // Customer 2 has claimed Summer and Tech discounts
    userDiscounts.add(EUserDiscount.builder()
        .customerId(CUSTOMER_2_ID)
        .discount(discounts.get(2)) // Summer 25%
        .build());
        
    userDiscounts.add(EUserDiscount.builder()
        .customerId(CUSTOMER_2_ID)
        .discount(discounts.get(3)) // Tech $200
        .build());
    
    // Customer 3 has claimed Welcome and Cashback
    userDiscounts.add(EUserDiscount.builder()
        .customerId(CUSTOMER_3_ID)
        .discount(discounts.get(4)) // Cashback 5%
        .build());
        
    userDiscounts.add(EUserDiscount.builder()
        .customerId(CUSTOMER_3_ID)
        .discount(discounts.get(5)) // Welcome $10
        .build());
    
    // All customers get auto-applied discounts
    for (UUID customerId : List.of(CUSTOMER_1_ID, CUSTOMER_2_ID, CUSTOMER_3_ID)) {
      userDiscounts.add(EUserDiscount.builder()
          .customerId(customerId)
          .discount(discounts.get(1)) // Free Shipping
          .build());
          
      userDiscounts.add(EUserDiscount.builder()
          .customerId(customerId)
          .discount(discounts.get(4)) // Cashback 5%
          .build());
    }
    
    userDiscountRepository.saveAll(userDiscounts);
  }

  private void createProductDiscounts(List<EDiscount> discounts) {
    List<EProductDiscount> productDiscounts = new ArrayList<>();
    
    // Tech Flash discount applies to specific electronics
    productDiscounts.add(EProductDiscount.builder()
        .discount(discounts.get(3)) // Tech $200
        .productId(IPHONE_15_ID)
        .build());
        
    productDiscounts.add(EProductDiscount.builder()
        .discount(discounts.get(3)) // Tech $200
        .productId(SAMSUNG_S24_ID)
        .build());
        
    productDiscounts.add(EProductDiscount.builder()
        .discount(discounts.get(3)) // Tech $200
        .productId(SONY_HEADPHONES_ID)
        .build());
        
    // Summer discount applies to fashion items
    productDiscounts.add(EProductDiscount.builder()
        .discount(discounts.get(2)) // Summer 25%
        .productId(NIKE_AIR_MAX_ID)
        .build());
        
    productDiscounts.add(EProductDiscount.builder()
        .discount(discounts.get(2)) // Summer 25%
        .productId(ADIDAS_ULTRABOOST_ID)
        .build());
    
    productDiscountRepository.saveAll(productDiscounts);
  }

  private void createVendorParticipations(List<EPromotionProgram> programs) {
    List<EVendorPromotionParticipation> participations = new ArrayList<>();
    
    // US Vendor participates in Black Friday
    participations.add(EVendorPromotionParticipation.builder()
        .vendorId(VENDOR_US_ID)
        .program(programs.get(0))
        .participationType(EVendorPromotionParticipation.ParticipationType.VOLUNTARY)
        .status(EVendorPromotionParticipation.Status.APPROVED)
        .vendorContributionRate(0.30)
        .expectedDiscountRate(0.30)
        .minDiscountAmount(10.0)
        .maxDiscountAmount(200.0)
        .productSelection(EVendorPromotionParticipation.ProductSelection.ALL)
        .acceptedTerms(true)
        .approvedAt(Instant.now().minusSeconds(86400))
        .approvedBy(VENDOR_US_ID)
        .build());
    
    // EU Vendor participates in Summer Sale
    participations.add(EVendorPromotionParticipation.builder()
        .vendorId(VENDOR_EU_ID)
        .program(programs.get(1))
        .participationType(EVendorPromotionParticipation.ParticipationType.VOLUNTARY)
        .status(EVendorPromotionParticipation.Status.APPROVED)
        .vendorContributionRate(0.25)
        .expectedDiscountRate(0.25)
        .minDiscountAmount(5.0)
        .maxDiscountAmount(100.0)
        .productSelection(EVendorPromotionParticipation.ProductSelection.CATEGORY)
        .acceptedTerms(true)
        .approvedAt(Instant.now().minusSeconds(86400 * 7))
        .approvedBy(VENDOR_EU_ID)
        .build());
    
    // Asia Vendor has pending participation in Tech Flash
    participations.add(EVendorPromotionParticipation.builder()
        .vendorId(VENDOR_ASIA_ID)
        .program(programs.get(2))
        .participationType(EVendorPromotionParticipation.ParticipationType.INVITED)
        .status(EVendorPromotionParticipation.Status.PENDING)
        .vendorContributionRate(0.40)
        .expectedDiscountRate(0.40)
        .minDiscountAmount(50.0)
        .maxDiscountAmount(200.0)
        .productSelection(EVendorPromotionParticipation.ProductSelection.SELECTED)
        .acceptedTerms(true)
        .build());
    
    // All vendors participate in Cashback
    for (UUID vendorId : List.of(VENDOR_US_ID, VENDOR_EU_ID, VENDOR_ASIA_ID)) {
      participations.add(EVendorPromotionParticipation.builder()
          .vendorId(vendorId)
          .program(programs.get(3))
          .participationType(EVendorPromotionParticipation.ParticipationType.MANDATORY)
          .status(EVendorPromotionParticipation.Status.APPROVED)
          .vendorContributionRate(0.05)
          .expectedDiscountRate(0.05)
          .minDiscountAmount(1.0)
          .maxDiscountAmount(50.0)
          .productSelection(EVendorPromotionParticipation.ProductSelection.ALL)
          .acceptedTerms(true)
          .approvedAt(Instant.now().minusSeconds(86400 * 30))
          .approvedBy(VENDOR_US_ID)
          .build());
    }
    
    vendorParticipationRepository.saveAll(participations);
  }

  private void createPromotionProducts(List<EPromotionProgram> programs) {
    List<EPromotionProduct> promotionProducts = new ArrayList<>();
    
    // Black Friday Products
    promotionProducts.add(EPromotionProduct.builder()
        .program(programs.get(0))
        .productId(IPHONE_15_ID)
        .vendorId(VENDOR_US_ID)
        .originalPrice(999.0)
        .promotionPrice(699.0)
        .discountAmount(300.0)
        .discountPercentage(30.0)
        .stockAllocated(100)
        .stockSold(0)
        .priority(1)
        .isFeatured(true)
        .status(EPromotionProduct.Status.ACTIVE)
        .build());
        
    promotionProducts.add(EPromotionProduct.builder()
        .program(programs.get(0))
        .productId(SAMSUNG_S24_ID)
        .vendorId(VENDOR_US_ID)
        .originalPrice(899.0)
        .promotionPrice(629.0)
        .discountAmount(270.0)
        .discountPercentage(30.0)
        .stockAllocated(150)
        .stockSold(0)
        .priority(2)
        .isFeatured(true)
        .status(EPromotionProduct.Status.ACTIVE)
        .build());
    
    // Summer Fashion Products
    promotionProducts.add(EPromotionProduct.builder()
        .program(programs.get(1))
        .productId(NIKE_AIR_MAX_ID)
        .vendorId(VENDOR_EU_ID)
        .originalPrice(180.0)
        .promotionPrice(135.0)
        .discountAmount(45.0)
        .discountPercentage(25.0)
        .stockAllocated(200)
        .stockSold(85)
        .priority(1)
        .isFeatured(true)
        .status(EPromotionProduct.Status.ACTIVE)
        .build());
        
    promotionProducts.add(EPromotionProduct.builder()
        .program(programs.get(1))
        .productId(ADIDAS_ULTRABOOST_ID)
        .vendorId(VENDOR_EU_ID)
        .originalPrice(220.0)
        .promotionPrice(165.0)
        .discountAmount(55.0)
        .discountPercentage(25.0)
        .stockAllocated(180)
        .stockSold(67)
        .priority(2)
        .isFeatured(false)
        .status(EPromotionProduct.Status.ACTIVE)
        .build());
    
    // Tech Flash Products
    promotionProducts.add(EPromotionProduct.builder()
        .program(programs.get(2))
        .productId(SONY_HEADPHONES_ID)
        .vendorId(VENDOR_ASIA_ID)
        .originalPrice(399.0)
        .promotionPrice(199.0)
        .discountAmount(200.0)
        .discountPercentage(50.0)
        .stockAllocated(50)
        .stockSold(0)
        .priority(1)
        .isFeatured(true)
        .status(EPromotionProduct.Status.ACTIVE)
        .build());
    
    promotionProductRepository.saveAll(promotionProducts);
  }

  private void createFlashSales(List<EPromotionProgram> programs) throws Exception {
    List<EFlashSale> flashSales = new ArrayList<>();
    
    // Flash sale for Tech Program
    JsonNode priceTiers = objectMapper.readTree("""
        {
          "tiers": [
            {"quantity": 10, "discount": 0.30},
            {"quantity": 25, "discount": 0.40},
            {"quantity": 40, "discount": 0.50}
          ]
        }
        """);
    
    EFlashSale techFlashSale = EFlashSale.builder()
        .program(programs.get(2))
        .countdownStart(Instant.now().plusSeconds(3600))
        .countdownEnd(Instant.now().plusSeconds(86400))
        .maxQuantity(50)
        .soldQuantity(0)
        .priceTiers(priceTiers)
        .notificationSent(false)
        .isNotifyEnabled(true)
        .status(EFlashSale.Status.UPCOMING)
        .build();
    flashSales.add(techFlashSale);
    
    flashSaleRepository.saveAll(flashSales);
  }

  private void createDiscountUsage(List<EDiscount> discounts) {
    List<EDiscountUsage> usages = new ArrayList<>();
    
    // Customer 1 used Welcome discount
    usages.add(EDiscountUsage.builder()
        .customerId(CUSTOMER_1_ID)
        .program(discounts.get(5).getProgram())
        .discount(discounts.get(5)) // Welcome $10
        .orderId(ORDER_1_ID)
        .usageCount(1)
        .discountAmount(10.0)
        .cashbackAmount(0.0)
        .pointsEarned(0)
        .usageStatus(DiscountUsageStatus.SUCCESS)
        .build());
    
    // Customer 2 used Summer discount multiple times
    usages.add(EDiscountUsage.builder()
        .customerId(CUSTOMER_2_ID)
        .program(discounts.get(2).getProgram())
        .discount(discounts.get(2)) // Summer 25%
        .orderId(ORDER_2_ID)
        .usageCount(1)
        .discountAmount(22.50)
        .cashbackAmount(0.0)
        .pointsEarned(0)
        .usageStatus(DiscountUsageStatus.SUCCESS)
        .build());
    
    usages.add(EDiscountUsage.builder()
        .customerId(CUSTOMER_2_ID)
        .program(discounts.get(2).getProgram())
        .discount(discounts.get(2)) // Summer 25%
        .orderId(ORDER_3_ID)
        .usageCount(1)
        .discountAmount(37.75)
        .cashbackAmount(0.0)
        .pointsEarned(0)
        .usageStatus(DiscountUsageStatus.SUCCESS)
        .build());
    
    // Multiple customers used Cashback
    for (int i = 0; i < 15; i++) {
      UUID customerId = i % 3 == 0 ? CUSTOMER_1_ID : i % 3 == 1 ? CUSTOMER_2_ID : CUSTOMER_3_ID;
      usages.add(EDiscountUsage.builder()
          .customerId(customerId)
          .program(discounts.get(4).getProgram())
          .discount(discounts.get(4)) // Cashback 5%
          .orderId(UUID.randomUUID())
          .usageCount(1)
          .discountAmount(5.0 + (i * 2.5))
          .cashbackAmount(5.0 + (i * 2.5))
          .pointsEarned(Math.round((5.0f + (i * 2.5f)) * 10))
          .usageStatus(DiscountUsageStatus.SUCCESS)
          .build());
    }
    
    discountUsageRepository.saveAll(usages);
  }

  private void createPromotionCommissions(List<EPromotionProgram> programs) {
    List<EPromotionCommission> commissions = new ArrayList<>();
    
    // Commissions from Summer Sales
    commissions.add(EPromotionCommission.builder()
        .program(programs.get(1))
        .vendorId(VENDOR_EU_ID)
        .orderId(ORDER_2_ID)
        .customerId(CUSTOMER_2_ID)
        .orderAmount(90.0)
        .discountAmount(22.50)
        .vendorContribution(22.50)
        .platformContribution(0.0)
        .commissionAmount(2.70) // 3% of order
        .commissionRate(0.03)
        .paymentStatus(EPromotionCommission.PaymentStatus.PAID)
        .paymentDate(Instant.now().minusSeconds(86400))
        .transactionId("TXN_EU_001")
        .build());
    
    commissions.add(EPromotionCommission.builder()
        .program(programs.get(1))
        .vendorId(VENDOR_EU_ID)
        .orderId(ORDER_3_ID)
        .customerId(CUSTOMER_2_ID)
        .orderAmount(151.0)
        .discountAmount(37.75)
        .vendorContribution(37.75)
        .platformContribution(0.0)
        .commissionAmount(4.53) // 3% of order
        .commissionRate(0.03)
        .paymentStatus(EPromotionCommission.PaymentStatus.PENDING)
        .build());
    
    // Cashback commissions
    for (int i = 0; i < 10; i++) {
      UUID vendorId = i % 3 == 0 ? VENDOR_US_ID : i % 3 == 1 ? VENDOR_EU_ID : VENDOR_ASIA_ID;
      UUID customerId = i % 3 == 0 ? CUSTOMER_1_ID : i % 3 == 1 ? CUSTOMER_2_ID : CUSTOMER_3_ID;
      double orderAmount = 100.0 + (i * 25.0);
      double discountAmount = orderAmount * 0.05;
      
      commissions.add(EPromotionCommission.builder()
          .program(programs.get(3))
          .vendorId(vendorId)
          .orderId(UUID.randomUUID())
          .customerId(customerId)
          .orderAmount(orderAmount)
          .discountAmount(discountAmount)
          .vendorContribution(discountAmount * 0.5)
          .platformContribution(discountAmount * 0.5)
          .commissionAmount(orderAmount * 0.02) // 2% commission
          .commissionRate(0.02)
          .paymentStatus(i < 5 ? EPromotionCommission.PaymentStatus.PAID : EPromotionCommission.PaymentStatus.PENDING)
          .paymentDate(i < 5 ? Instant.now().minusSeconds(86400 * (i + 1)) : null)
          .transactionId(i < 5 ? "TXN_CASHBACK_" + String.format("%03d", i + 1) : null)
          .build());
    }
    
    promotionCommissionRepository.saveAll(commissions);
  }

  private void createPromotionAnalytics(List<EPromotionProgram> programs) {
    List<EPromotionAnalytics> analytics = new ArrayList<>();
    
    // Analytics for last 30 days for each program
    for (int day = 0; day < 30; day++) {
      LocalDate date = LocalDate.now().minusDays(day);
      
      // Black Friday Program Analytics (not started yet, so zeros)
      analytics.add(EPromotionAnalytics.builder()
          .program(programs.get(0))
          .date(date)
          .totalOrders(0)
          .totalRevenue(0.0)
          .totalDiscountGiven(0.0)
          .totalCustomers(0)
          .newCustomers(0)
          .returningCustomers(0)
          .conversionRate(0.0)
          .averageOrderValue(0.0)
          .roi(0.0)
          .vendorParticipationCount(1)
          .build());
      
      // Summer Program Analytics (active with varying performance)
      int summerOrders = day < 7 ? 15 + (int)(Math.random() * 10) : 25 + (int)(Math.random() * 15);
      double summerRevenue = summerOrders * (120.0 + (Math.random() * 80));
      double summerDiscount = summerRevenue * 0.20; // Average 20% discount
      
      analytics.add(EPromotionAnalytics.builder()
          .program(programs.get(1))
          .date(date)
          .totalOrders(summerOrders)
          .totalRevenue(summerRevenue)
          .totalDiscountGiven(summerDiscount)
          .totalCustomers(summerOrders * 8 / 10) // 80% unique customers
          .newCustomers(summerOrders / 3)
          .returningCustomers(summerOrders * 2 / 3)
          .conversionRate(0.12 + (Math.random() * 0.08))
          .averageOrderValue(summerRevenue / summerOrders)
          .roi((summerRevenue - summerDiscount) / summerDiscount)
          .vendorParticipationCount(1)
          .build());
      
      // Tech Program Analytics (upcoming, so zeros)
      analytics.add(EPromotionAnalytics.builder()
          .program(programs.get(2))
          .date(date)
          .totalOrders(0)
          .totalRevenue(0.0)
          .totalDiscountGiven(0.0)
          .totalCustomers(0)
          .newCustomers(0)
          .returningCustomers(0)
          .conversionRate(0.0)
          .averageOrderValue(0.0)
          .roi(0.0)
          .vendorParticipationCount(1)
          .build());
      
      // Cashback Program Analytics (steady performance)
      int cashbackOrders = 50 + (int)(Math.random() * 20);
      double cashbackRevenue = cashbackOrders * (85.0 + (Math.random() * 50));
      double cashbackDiscount = cashbackRevenue * 0.05; // 5% cashback
      
      analytics.add(EPromotionAnalytics.builder()
          .program(programs.get(3))
          .date(date)
          .totalOrders(cashbackOrders)
          .totalRevenue(cashbackRevenue)
          .totalDiscountGiven(cashbackDiscount)
          .totalCustomers(cashbackOrders * 9 / 10) // 90% unique customers
          .newCustomers(cashbackOrders / 5)
          .returningCustomers(cashbackOrders * 4 / 5)
          .conversionRate(0.08 + (Math.random() * 0.06))
          .averageOrderValue(cashbackRevenue / cashbackOrders)
          .roi((cashbackRevenue - cashbackDiscount) / cashbackDiscount)
          .vendorParticipationCount(3)
          .build());
    }
    
    promotionAnalyticsRepository.saveAll(analytics);
  }
}
