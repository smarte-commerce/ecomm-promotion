package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.promotion.core.service.PromotionProgramService;
import com.winnguyen1905.promotion.exception.BadRequestException;
import com.winnguyen1905.promotion.exception.ResourceNotFoundException;
import com.winnguyen1905.promotion.model.request.CreatePromotionProgramRequest;
import com.winnguyen1905.promotion.model.request.SearchPromotionProgramRequest;
import com.winnguyen1905.promotion.model.request.UpdatePromotionProgramRequest;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PromotionProgramVm;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram.ProgramStatus;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PromotionProgramServiceImpl implements PromotionProgramService {

  private final PromotionProgramRepository promotionProgramRepository;

  @Override
  public void createPromotionProgram(TAccountRequest accountRequest, CreatePromotionProgramRequest request) {
    log.info("Creating promotion program: {}", request.name());

    // Business logic for creation
    if (request.startDate().isAfter(request.endDate())) {
      throw new BadRequestException("Start date must be before end date");
    }

    EPromotionProgram program = EPromotionProgram.builder()
        .name(request.name())
        .description(request.description())
        .programType(request.programType())
        .startDate(request.startDate())
        .endDate(request.endDate())
        .priority(request.priority() != null ? request.priority() : 1)
        .status(ProgramStatus.DRAFT)
        .createdBy(accountRequest.id())
        .build();

    promotionProgramRepository.save(program);
    log.info("Successfully created promotion program with id: {}", program.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public PromotionProgramVm getPromotionProgramById(TAccountRequest accountRequest, UUID id) {
    EPromotionProgram program = promotionProgramRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Promotion program not found with id: " + id));

    return convertToVm(program);
  }

  @Override
  public PagedResponse<PromotionProgramVm> getPromotionPrograms(TAccountRequest accountRequest,
      SearchPromotionProgramRequest request,
      Pageable pageable) {
    // Implementation with specifications
    Page<EPromotionProgram> programPage = promotionProgramRepository.findAll(pageable);

    List<PromotionProgramVm> programs = programPage.getContent().stream()
        .map(this::convertToVm)
        .collect(Collectors.toList());

    return PagedResponse.<PromotionProgramVm>builder()
        .results(programs)
        .page(programPage.getNumber())
        .size(programPage.getSize())
        .totalElements(programPage.getTotalElements())
        .totalPages(programPage.getTotalPages())
        .maxPageItems(programPage.getSize())
        .build();
  }

  // Implement other required methods...
  @Override
  public void updatePromotionProgram(TAccountRequest accountRequest, UUID id, UpdatePromotionProgramRequest request) {
    // Implementation
  }

  @Override
  public void deletePromotionProgram(TAccountRequest accountRequest, UUID id) {
    // Implementation
  }

  @Override
  public void activateProgram(TAccountRequest accountRequest, UUID id) {
    // Implementation
  }

  @Override
  public void pauseProgram(TAccountRequest accountRequest, UUID id) {
    // Implementation
  }

  @Override
  public void expireProgram(TAccountRequest accountRequest, UUID id) {
    // Implementation
  }

  @Override
  public PagedResponse<PromotionProgramVm> getActivePrograms(TAccountRequest accountRequest, Pageable pageable) {
    // Implementation
    return null;
  }

  @Override
  public PagedResponse<PromotionProgramVm> getUserPrograms(TAccountRequest accountRequest, Pageable pageable) {
    // Implementation
    return null;
  }

  @Override
  public void processExpiredPrograms() {
    // Implementation
  }

  @Override
  public void processScheduledPrograms() {
    // Implementation
  }

  private PromotionProgramVm convertToVm(EPromotionProgram program) {
    return PromotionProgramVm.builder()
        .id(program.getId())
        .name(program.getName())
        .description(program.getDescription())
        .programType(program.getProgramType())
        .startDate(program.getStartDate())
        .endDate(program.getEndDate())
        .status(program.getStatus())
        .createdBy(program.getCreatedBy())
        .createdAt(program.getCreatedAt())
        .updatedAt(program.getUpdatedAt())
        .build();
  }
}
