package com.winnguyen1905.promotion.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.request.CreateFlashSaleRequest;
import com.winnguyen1905.promotion.model.response.FlashSaleVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface FlashSaleService {

  void createFlashSale(TAccountRequest accountRequest, CreateFlashSaleRequest request);

  FlashSaleVm getFlashSaleById(TAccountRequest accountRequest, UUID id);

  FlashSaleVm getFlashSaleByProgramId(TAccountRequest accountRequest, UUID programId);

  PagedResponse<FlashSaleVm> getFlashSales(TAccountRequest accountRequest, Pageable pageable);

  List<FlashSaleVm> getActiveFlashSales(TAccountRequest accountRequest);

  List<FlashSaleVm> getUpcomingFlashSales(TAccountRequest accountRequest);

  void updateFlashSale(TAccountRequest accountRequest, UUID id, CreateFlashSaleRequest request);

  void deleteFlashSale(TAccountRequest accountRequest, UUID id);

  void purchaseFlashSaleItem(TAccountRequest accountRequest, UUID flashSaleId, Integer quantity);

  void processFlashSaleStatus();

  void sendFlashSaleNotifications();
}
