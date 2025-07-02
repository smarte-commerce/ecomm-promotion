-- =====================================================
-- REFACTORED E-COMMERCE DATABASE - PROMOTION & SALE PROGRAM
-- =====================================================

-- Core Tables (unchanged but referenced)
CREATE TABLE `accounts` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `username` VARCHAR(255),
  `password` VARCHAR(255),
  `status` BOOLEAN DEFAULT true,
  `role_code` ENUM ('ADMIN', 'VENDOR', 'CUSTOMER'),
  `last_token` VARCHAR(1024),
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `customers` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `account_id` UUID UNIQUE,
  `customer_name` VARCHAR(255),
  `customer_address` TEXT,
  `customer_phone` VARCHAR(50),
  `customer_email` VARCHAR(255),
  `customer_logo` VARCHAR(500),
  `customer_status` BOOLEAN DEFAULT true,
  `customer_tier` ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') DEFAULT 'BRONZE',
  `total_orders` INTEGER DEFAULT 0,
  `total_spent` DECIMAL(12,2) DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `vendors` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `account_id` UUID UNIQUE,
  `vendor_name` VARCHAR(255),
  `vendor_description` TEXT,
  `vendor_address` TEXT,
  `vendor_phone` VARCHAR(50),
  `vendor_email` VARCHAR(255),
  `vendor_logo` VARCHAR(500),
  `vendor_status` BOOLEAN DEFAULT true,
  `rating` DECIMAL(3,2) DEFAULT 0,
  `total_sales` DECIMAL(15,2) DEFAULT 0,
  `commission_rate` DECIMAL(5,4) DEFAULT 0.05,
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- PROMOTION & SALE PROGRAM TABLES (REFACTORED)
-- =====================================================

-- 1. Campaign Management - Quản lý chiến dịch tổng thể
CREATE TABLE `campaigns` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `campaign_type` ENUM('FLASH_SALE', 'SEASONAL', 'CLEARANCE', 'NEW_PRODUCT', 'LOYALTY', 'AFFILIATE') NOT NULL,
  `start_date` TIMESTAMP NOT NULL,
  `end_date` TIMESTAMP NOT NULL,
  `status` ENUM('DRAFT', 'SCHEDULED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
  `budget` DECIMAL(12,2),
  `spent_budget` DECIMAL(12,2) DEFAULT 0,
  `target_audience` JSON, -- Stored as JSON for flexibility
  `created_by` UUID NOT NULL,
  `approved_by` UUID,
  `approved_at` TIMESTAMP,
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Promotion Programs - Chương trình khuyến mãi cụ thể
CREATE TABLE `promotion_programs` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `campaign_id` UUID,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `program_type` ENUM('DISCOUNT', 'CASHBACK', 'POINTS', 'GIFT', 'SHIPPING', 'BUNDLE') NOT NULL,
  `start_date` TIMESTAMP NOT NULL,
  `end_date` TIMESTAMP NOT NULL,
  `priority` INTEGER DEFAULT 1, -- Để xử lý khi có nhiều promotion cùng lúc
  `is_stackable` BOOLEAN DEFAULT false, -- Có thể kết hợp với promotion khác
  `platform_commission_rate` DECIMAL(5,4) DEFAULT 0,
  `required_vendor_contribution` DECIMAL(5,4) DEFAULT 0,
  `visibility` ENUM ('PUBLIC', 'INVITE_ONLY', 'MEMBER_ONLY', 'VIP_ONLY') NOT NULL DEFAULT 'PUBLIC',
  `usage_limit_global` INTEGER, -- Giới hạn sử dụng toàn platform
  `usage_count_global` INTEGER DEFAULT 0,
  `terms_conditions` TEXT,
  `terms_url` VARCHAR(500),
  `auto_apply` BOOLEAN DEFAULT false, -- Tự động áp dụng khi đủ điều kiện
  `status` ENUM('DRAFT', 'ACTIVE', 'PAUSED', 'EXPIRED', 'CANCELLED') DEFAULT 'DRAFT',
  `created_by` UUID NOT NULL,
  `updated_by` UUID,
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Promotion Rules - Quy tắc áp dụng promotion
CREATE TABLE `promotion_rules` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `program_id` UUID NOT NULL,
  `rule_type` ENUM(
    'MIN_ORDER_VALUE', 'MIN_QUANTITY', 'CUSTOMER_TIER', 
    'FIRST_ORDER', 'REPEAT_CUSTOMER', 'PRODUCT_CATEGORY',
    'BRAND', 'VENDOR', 'PAYMENT_METHOD', 'SHIPPING_METHOD',
    'DAY_OF_WEEK', 'TIME_OF_DAY', 'LOCATION'
  ) NOT NULL,
  `operator` ENUM('EQUALS', 'NOT_EQUALS', 'GREATER_THAN', 'LESS_THAN', 'IN', 'NOT_IN', 'CONTAINS') NOT NULL,
  `value` JSON NOT NULL, -- Flexible value storage
  `is_required` BOOLEAN DEFAULT true, -- true = AND, false = OR
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP)
);

-- 4. Promotion Actions - Hành động khi promotion được kích hoạt
CREATE TABLE `promotion_actions` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `program_id` UUID NOT NULL,
  `action_type` ENUM(
    'PERCENTAGE_DISCOUNT', 'FIXED_DISCOUNT', 'FREE_SHIPPING',
    'CASHBACK_PERCENTAGE', 'CASHBACK_FIXED', 'LOYALTY_POINTS',
    'FREE_GIFT', 'BUY_X_GET_Y', 'UPGRADE_SHIPPING'
  ) NOT NULL,
  `target` ENUM('ORDER_TOTAL', 'SHIPPING', 'SPECIFIC_PRODUCTS', 'CHEAPEST_PRODUCT', 'MOST_EXPENSIVE') NOT NULL,
  `value` DECIMAL(10,2) NOT NULL,
  `max_discount_amount` DECIMAL(10,2), -- Giới hạn số tiền giảm tối đa
  `applies_to` JSON, -- Product IDs, Category IDs, etc.
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP)
);

-- 5. Vendor Participation in Promotions
CREATE TABLE `vendor_promotion_participations` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `vendor_id` UUID NOT NULL,
  `program_id` UUID NOT NULL,
  `participation_type` ENUM('VOLUNTARY', 'MANDATORY', 'INVITED') DEFAULT 'VOLUNTARY',
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'WITHDRAWN', 'SUSPENDED') NOT NULL DEFAULT 'PENDING',
  `vendor_contribution_rate` DECIMAL(5,4) NOT NULL, -- % vendor đóng góp
  `expected_discount_rate` DECIMAL(5,4) NOT NULL, -- % giảm giá dự kiến
  `min_discount_amount` DECIMAL(10,2),
  `max_discount_amount` DECIMAL(10,2),
  `product_selection` ENUM('ALL', 'SELECTED', 'CATEGORY') DEFAULT 'ALL',
  `accepted_terms` BOOLEAN NOT NULL DEFAULT false,
  `joined_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `approved_at` TIMESTAMP,
  `approved_by` UUID,
  `withdrawal_reason` TEXT,
  `performance_metrics` JSON, -- Metrics tracking
  UNIQUE KEY `uk_vendor_program` (`vendor_id`, `program_id`)
);

-- 6. Products in Promotion Programs
CREATE TABLE `promotion_products` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `program_id` UUID NOT NULL,
  `product_id` UUID NOT NULL,
  `vendor_id` UUID NOT NULL,
  `original_price` DECIMAL(10,2) NOT NULL,
  `promotion_price` DECIMAL(10,2) NOT NULL,
  `discount_amount` DECIMAL(10,2) NOT NULL,
  `discount_percentage` DECIMAL(5,2) NOT NULL,
  `stock_allocated` INTEGER DEFAULT 0, -- Số lượng phân bổ cho promotion
  `stock_sold` INTEGER DEFAULT 0,
  `priority` INTEGER DEFAULT 1,
  `is_featured` BOOLEAN DEFAULT false,
  `status` ENUM('ACTIVE', 'PAUSED', 'OUT_OF_STOCK', 'REMOVED') DEFAULT 'ACTIVE',
  `added_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_program_product` (`program_id`, `product_id`)
);

-- 7. Customer Promotion Usage Tracking
CREATE TABLE `customer_promotion_usage` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `customer_id` UUID NOT NULL,
  `program_id` UUID NOT NULL,
  `order_id` UUID,
  `usage_count` INTEGER DEFAULT 1,
  `discount_amount` DECIMAL(10,2) NOT NULL,
  `cashback_amount` DECIMAL(10,2) DEFAULT 0,
  `points_earned` INTEGER DEFAULT 0,
  `usage_date` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `status` ENUM('SUCCESS', 'FAILED', 'PENDING', 'CANCELLED') DEFAULT 'SUCCESS'
);

-- 8. Commission and Revenue Tracking
CREATE TABLE `promotion_commissions` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `program_id` UUID NOT NULL,
  `vendor_id` UUID NOT NULL,
  `order_id` UUID NOT NULL,
  `customer_id` UUID NOT NULL,
  `order_amount` DECIMAL(10,2) NOT NULL,
  `discount_amount` DECIMAL(10,2) NOT NULL,
  `vendor_contribution` DECIMAL(10,2) NOT NULL,
  `platform_contribution` DECIMAL(10,2) NOT NULL,
  `commission_amount` DECIMAL(10,2) NOT NULL,
  `commission_rate` DECIMAL(5,4) NOT NULL,
  `payment_status` ENUM('PENDING', 'PROCESSING', 'PAID', 'FAILED') DEFAULT 'PENDING',
  `payment_date` TIMESTAMP,
  `transaction_id` VARCHAR(255),
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `processed_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP)
);

-- 9. Promotion Analytics and Performance
CREATE TABLE `promotion_analytics` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `program_id` UUID NOT NULL,
  `date` DATE NOT NULL,
  `total_orders` INTEGER DEFAULT 0,
  `total_revenue` DECIMAL(12,2) DEFAULT 0,
  `total_discount_given` DECIMAL(12,2) DEFAULT 0,
  `total_customers` INTEGER DEFAULT 0,
  `new_customers` INTEGER DEFAULT 0,
  `returning_customers` INTEGER DEFAULT 0,
  `conversion_rate` DECIMAL(5,4) DEFAULT 0,
  `average_order_value` DECIMAL(10,2) DEFAULT 0,
  `roi` DECIMAL(8,4) DEFAULT 0, -- Return on Investment
  `vendor_participation_count` INTEGER DEFAULT 0,
  `top_performing_products` JSON,
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  UNIQUE KEY `uk_program_date` (`program_id`, `date`)
);

-- 10. Flash Sale Specific Table
CREATE TABLE `flash_sales` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `program_id` UUID NOT NULL UNIQUE,
  `countdown_start` TIMESTAMP NOT NULL,
  `countdown_end` TIMESTAMP NOT NULL,
  `max_quantity` INTEGER NOT NULL,
  `sold_quantity` INTEGER DEFAULT 0,
  `price_tiers` JSON, -- Different prices based on quantity sold
  `notification_sent` BOOLEAN DEFAULT false,
  `is_notify_enabled` BOOLEAN DEFAULT true,
  `status` ENUM('UPCOMING', 'LIVE', 'SOLD_OUT', 'ENDED') DEFAULT 'UPCOMING'
);

-- =====================================================
-- ENHANCED DISCOUNT SYSTEM
-- =====================================================

-- 11. Enhanced Discounts Table
CREATE TABLE `discounts` (
  `id` UUID PRIMARY KEY DEFAULT (gen_random_uuid()),
  `version` BIGINT NOT NULL DEFAULT 0,
  `program_id` UUID, -- Link to promotion program
  `creator_type` ENUM ('ADMIN', 'VENDOR', 'SYSTEM') NOT NULL,
  `creator_id` UUID NOT NULL,
  `type` ENUM ('PERCENTAGE', 'FIXED_AMOUNT', 'BUY_X_GET_Y', 'FREE_SHIPPING') NOT NULL,
  `applies_to` ENUM ('ALL_PRODUCTS', 'SPECIFIC_PRODUCTS', 'CATEGORY', 'BRAND', 'VENDOR') NOT NULL,
  `category` ENUM ('PRODUCT', 'SHIPPING', 'ORDER_TOTAL') NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `code` VARCHAR(100) UNIQUE,
  `value` DECIMAL(10,2) NOT NULL,
  `max_discount_amount` DECIMAL(10,2),
  `min_order_value` DECIMAL(10,2),
  `start_date` TIMESTAMP NOT NULL,
  `end_date` TIMESTAMP NOT NULL,
  `usage_limit_total` INTEGER,
  `usage_limit_per_customer` INTEGER DEFAULT 1,
  `usage_count` INTEGER DEFAULT 0,
  `is_active` BOOLEAN DEFAULT true,
  `is_public` BOOLEAN DEFAULT true,
  `auto_apply` BOOLEAN DEFAULT false,
  `vendor_id` UUID,
  `target_customer_tiers` JSON, -- Array of customer tiers
  `geographic_restrictions` JSON, -- Location-based restrictions
  `created_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP),
  `updated_at` TIMESTAMP DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Campaign indexes
CREATE INDEX `idx_campaigns_type_status` ON `campaigns` (`campaign_type`, `status`);
CREATE INDEX `idx_campaigns_dates` ON `campaigns` (`start_date`, `end_date`);
CREATE INDEX `idx_campaigns_created_by` ON `campaigns` (`created_by`);

-- Promotion program indexes
CREATE INDEX `idx_promotion_programs_campaign` ON `promotion_programs` (`campaign_id`);
CREATE INDEX `idx_promotion_programs_type_status` ON `promotion_programs` (`program_type`, `status`);
CREATE INDEX `idx_promotion_programs_dates` ON `promotion_programs` (`start_date`, `end_date`);
CREATE INDEX `idx_promotion_programs_visibility` ON `promotion_programs` (`visibility`);

-- Promotion rules indexes
CREATE INDEX `idx_promotion_rules_program` ON `promotion_rules` (`program_id`);
CREATE INDEX `idx_promotion_rules_type` ON `promotion_rules` (`rule_type`);

-- Promotion actions indexes
CREATE INDEX `idx_promotion_actions_program` ON `promotion_actions` (`program_id`);
CREATE INDEX `idx_promotion_actions_type` ON `promotion_actions` (`action_type`);

-- Vendor participation indexes
CREATE INDEX `idx_vendor_participation_vendor` ON `vendor_promotion_participations` (`vendor_id`);
CREATE INDEX `idx_vendor_participation_program` ON `vendor_promotion_participations` (`program_id`);
CREATE INDEX `idx_vendor_participation_status` ON `vendor_promotion_participations` (`status`);

-- Promotion products indexes
CREATE INDEX `idx_promotion_products_program` ON `promotion_products` (`program_id`);
CREATE INDEX `idx_promotion_products_vendor` ON `promotion_products` (`vendor_id`);
CREATE INDEX `idx_promotion_products_status` ON `promotion_products` (`status`);
CREATE INDEX `idx_promotion_products_featured` ON `promotion_products` (`is_featured`);

-- Customer usage indexes
CREATE INDEX `idx_customer_usage_customer` ON `customer_promotion_usage` (`customer_id`);
CREATE INDEX `idx_customer_usage_program` ON `customer_promotion_usage` (`program_id`);
CREATE INDEX `idx_customer_usage_date` ON `customer_promotion_usage` (`usage_date`);

-- Commission indexes
CREATE INDEX `idx_promotion_commissions_vendor` ON `promotion_commissions` (`vendor_id`);
CREATE INDEX `idx_promotion_commissions_program` ON `promotion_commissions` (`program_id`);
CREATE INDEX `idx_promotion_commissions_status` ON `promotion_commissions` (`payment_status`);
CREATE INDEX `idx_promotion_commissions_date` ON `promotion_commissions` (`created_at`);

-- Analytics indexes
CREATE INDEX `idx_promotion_analytics_program` ON `promotion_analytics` (`program_id`);
CREATE INDEX `idx_promotion_analytics_date` ON `promotion_analytics` (`date`);

-- Flash sale indexes
CREATE INDEX `idx_flash_sales_status` ON `flash_sales` (`status`);
CREATE INDEX `idx_flash_sales_countdown` ON `flash_sales` (`countdown_start`, `countdown_end`);

-- Enhanced discount indexes
CREATE INDEX `idx_discounts_program` ON `discounts` (`program_id`);
CREATE INDEX `idx_discounts_creator` ON `discounts` (`creator_type`, `creator_id`);
CREATE INDEX `idx_discounts_code` ON `discounts` (`code`);
CREATE INDEX `idx_discounts_active` ON `discounts` (`is_active`);
CREATE INDEX `idx_discounts_dates` ON `discounts` (`start_date`, `end_date`);
CREATE INDEX `idx_discounts_vendor` ON `discounts` (`vendor_id`);

-- =====================================================
-- FOREIGN KEY CONSTRAINTS
-- =====================================================

-- Campaign constraints
ALTER TABLE `campaigns` ADD FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`);
ALTER TABLE `campaigns` ADD FOREIGN KEY (`approved_by`) REFERENCES `accounts` (`id`);

-- Promotion program constraints
ALTER TABLE `promotion_programs` ADD FOREIGN KEY (`campaign_id`) REFERENCES `campaigns` (`id`);
ALTER TABLE `promotion_programs` ADD FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`);
ALTER TABLE `promotion_programs` ADD FOREIGN KEY (`updated_by`) REFERENCES `accounts` (`id`);

-- Promotion rules constraints
ALTER TABLE `promotion_rules` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`) ON DELETE CASCADE;

-- Promotion actions constraints
ALTER TABLE `promotion_actions` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`) ON DELETE CASCADE;

-- Vendor participation constraints
ALTER TABLE `vendor_promotion_participations` ADD FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`);
ALTER TABLE `vendor_promotion_participations` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);
ALTER TABLE `vendor_promotion_participations` ADD FOREIGN KEY (`approved_by`) REFERENCES `accounts` (`id`);

-- Promotion products constraints
ALTER TABLE `promotion_products` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);
ALTER TABLE `promotion_products` ADD FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`);

-- Customer usage constraints
ALTER TABLE `customer_promotion_usage` ADD FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`);
ALTER TABLE `customer_promotion_usage` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);

-- Commission constraints
ALTER TABLE `promotion_commissions` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);
ALTER TABLE `promotion_commissions` ADD FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`);
ALTER TABLE `promotion_commissions` ADD FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`);

-- Analytics constraints
ALTER TABLE `promotion_analytics` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);

-- Flash sale constraints
ALTER TABLE `flash_sales` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);

-- Enhanced discount constraints
ALTER TABLE `discounts` ADD FOREIGN KEY (`program_id`) REFERENCES `promotion_programs` (`id`);
ALTER TABLE `discounts` ADD FOREIGN KEY (`creator_id`) REFERENCES `accounts` (`id`);
ALTER TABLE `discounts` ADD FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`id`);

-- Core table constraints (if not already defined)
ALTER TABLE `customers` ADD FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`);
ALTER TABLE `vendors` ADD FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`);