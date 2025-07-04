package com.winnguyen1905.promotion.core.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.winnguyen1905.promotion.model.response.PromotionAnalyticsVm;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface PromotionAnalyticsService {

  PromotionAnalyticsVm getAnalyticsById(TAccountRequest accountRequest, UUID id);

  List<PromotionAnalyticsVm> getProgramAnalytics(TAccountRequest accountRequest, UUID programId);

  PromotionAnalyticsVm getProgramAnalyticsByDate(TAccountRequest accountRequest, UUID programId, LocalDate date);

  List<PromotionAnalyticsVm> getProgramAnalyticsByDateRange(TAccountRequest accountRequest,
      UUID programId,
      LocalDate startDate,
      LocalDate endDate);

  void generateDailyAnalytics(UUID programId, LocalDate date);

  void generateAnalyticsForAllActivePrograms(LocalDate date);

  Double calculateProgramROI(UUID programId, LocalDate startDate, LocalDate endDate);

  Double calculateProgramConversionRate(UUID programId, LocalDate startDate, LocalDate endDate);

  void updateAnalyticsFromOrder(UUID orderId, UUID programId, Double orderAmount, Double discountAmount);
}
