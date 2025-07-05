package com.winnguyen1905.promotion.core.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.promotion.model.request.CreatePromotionProgramRequest;
import com.winnguyen1905.promotion.model.request.SearchPromotionProgramRequest;
import com.winnguyen1905.promotion.model.request.UpdatePromotionProgramRequest;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PromotionProgramVm;
import com.winnguyen1905.promotion.secure.TAccountRequest;

public interface PromotionProgramService {

  void createPromotionProgram(TAccountRequest accountRequest, CreatePromotionProgramRequest request);

  PromotionProgramVm getPromotionProgramById(TAccountRequest accountRequest, UUID id);

  PagedResponse<PromotionProgramVm> getPromotionPrograms(TAccountRequest accountRequest,
      SearchPromotionProgramRequest request,
      Pageable pageable);

  void updatePromotionProgram(TAccountRequest accountRequest, UUID id, UpdatePromotionProgramRequest request);

  void deletePromotionProgram(TAccountRequest accountRequest, UUID id);

  void activateProgram(TAccountRequest accountRequest, UUID id);

  void pauseProgram(TAccountRequest accountRequest, UUID id);

  void expireProgram(TAccountRequest accountRequest, UUID id);

  PagedResponse<PromotionProgramVm> getActivePrograms(TAccountRequest accountRequest, Pageable pageable);

  PagedResponse<PromotionProgramVm> getUserPrograms(TAccountRequest accountRequest, Pageable pageable);

  void processExpiredPrograms();

  void processScheduledPrograms();
}
