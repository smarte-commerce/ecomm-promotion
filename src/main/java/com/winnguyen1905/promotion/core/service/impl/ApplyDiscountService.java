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
import com.winnguyen1905.promotion.core.model.request.ApplyDiscountRequest;
import com.winnguyen1905.promotion.core.model.request.CustomerCart.CustomerCartWithShop;
import com.winnguyen1905.promotion.core.model.response.DiscountValidityResponse;
import com.winnguyen1905.promotion.core.model.response.PriceStatisticsResponse;
import com.winnguyen1905.promotion.core.service.DiscountServiceImpl;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.persistance.entity.EDiscount;
import com.winnguyen1905.promotion.persistance.entity.EProductDiscount;
import com.winnguyen1905.promotion.persistance.repository.DiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.DiscountUsageRepository;
import com.winnguyen1905.promotion.persistance.repository.ProductDiscountRepository;
import com.winnguyen1905.promotion.persistance.repository.ShopPromotionRepository;
import com.winnguyen1905.promotion.persistance.repository.UserDiscountRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

// @Service
public final class ApplyDiscountService {

  // public ApplyDiscountService(CartServiceClient cartServiceClient, DiscountRepository discountRepository,
  //     UserDiscountRepository userDiscountRepository,
  //     DiscountUsageRepository discountUsageRepository, ProductDiscountRepository productDiscountRepository) {
  //   super(cartServiceClient, discountRepository, userDiscountRepository, discountUsageRepository, productDiscountRepository);

  // }
}
