package com.winnguyen1905.promotion.core.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.winnguyen1905.promotion.core.service.FlashSaleService;
import com.winnguyen1905.promotion.model.request.CreateFlashSaleRequest;
import com.winnguyen1905.promotion.model.response.FlashSaleVm;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.persistance.entity.EFlashSale;
import com.winnguyen1905.promotion.persistance.entity.EFlashSale.Status;
import com.winnguyen1905.promotion.persistance.entity.EPromotionProgram;
import com.winnguyen1905.promotion.persistance.repository.FlashSaleRepository;
import com.winnguyen1905.promotion.persistance.repository.PromotionProgramRepository;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {

  private final FlashSaleRepository flashSaleRepository;
  private final PromotionProgramRepository programRepository;

  private FlashSaleVm toVm(EFlashSale fs) {
    if (fs == null)
      return null;
    return FlashSaleVm.builder()
        .id(fs.getId())
        .programId(fs.getProgram().getId())
        .programName(fs.getProgram().getName())
        .countdownStart(fs.getCountdownStart())
        .countdownEnd(fs.getCountdownEnd())
        .maxQuantity(fs.getMaxQuantity())
        .soldQuantity(fs.getSoldQuantity())
        .priceTiers(fs.getPriceTiers())
        .notificationSent(fs.getNotificationSent())
        .isNotifyEnabled(fs.getIsNotifyEnabled())
        .status(fs.getStatus())
        .build();
  }

  @Override
  @Transactional
  public void createFlashSale(TAccountRequest accountRequest, CreateFlashSaleRequest request) {
    EPromotionProgram program = programRepository.findById(request.programId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found"));

    // Check for existing flash sale for program
    flashSaleRepository.findByProgramId(program.getId()).ifPresent(fs -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Flash sale already exists for this program");
    });

    if (request.countdownStart().isAfter(request.countdownEnd())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start must be before end");
    }

    EFlashSale flashSale = EFlashSale.builder()
        .program(program)
        .countdownStart(request.countdownStart())
        .countdownEnd(request.countdownEnd())
        .maxQuantity(request.maxQuantity())
        .priceTiers(request.priceTiers())
        .isNotifyEnabled(request.isNotifyEnabled() != null ? request.isNotifyEnabled() : true)
        .status(Status.UPCOMING)
        .build();
    flashSaleRepository.save(flashSale);
  }

  @Override
  @Transactional(readOnly = true)
  public FlashSaleVm getFlashSaleById(TAccountRequest accountRequest, UUID id) {
    return toVm(flashSaleRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flash sale not found")));
  }

  @Override
  @Transactional(readOnly = true)
  public FlashSaleVm getFlashSaleByProgramId(TAccountRequest accountRequest, UUID programId) {
    return flashSaleRepository.findByProgramId(programId).map(this::toVm)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flash sale not found for program"));
  }

  @Override
  public PagedResponse<FlashSaleVm> getFlashSales(TAccountRequest accountRequest, Pageable pageable) {
    Page<EFlashSale> page = flashSaleRepository.findAll(pageable);
    List<FlashSaleVm> items = page.getContent().stream().map(this::toVm).collect(Collectors.toList());
    return mapPage(page, items);
  }

  @Override
  public List<FlashSaleVm> getActiveFlashSales(TAccountRequest accountRequest) {
    return flashSaleRepository.findByStatus(Status.LIVE).stream().map(this::toVm).collect(Collectors.toList());
  }

  @Override
  public List<FlashSaleVm> getUpcomingFlashSales(TAccountRequest accountRequest) {
    return flashSaleRepository.findByStatus(Status.UPCOMING).stream().map(this::toVm).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void updateFlashSale(TAccountRequest accountRequest, UUID id, CreateFlashSaleRequest request) {
    EFlashSale fs = flashSaleRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flash sale not found"));

    if (fs.getStatus() == Status.LIVE || fs.getStatus() == Status.SOLD_OUT) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify live/sold-out flash sale");
    }

    fs.setCountdownStart(request.countdownStart());
    fs.setCountdownEnd(request.countdownEnd());
    fs.setMaxQuantity(request.maxQuantity());
    fs.setPriceTiers(request.priceTiers());
    fs.setIsNotifyEnabled(request.isNotifyEnabled());
    flashSaleRepository.save(fs);
  }

  @Override
  public void deleteFlashSale(TAccountRequest accountRequest, UUID id) {
    flashSaleRepository.deleteById(id);
  }

  @Override
  public void purchaseFlashSaleItem(TAccountRequest accountRequest, UUID flashSaleId, Integer quantity) {
    if (quantity == null || quantity <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
    }
    EFlashSale fs = flashSaleRepository.findById(flashSaleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flash sale not found"));
    if (fs.getStatus() != Status.LIVE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flash sale not live");
    }
    int updated = flashSaleRepository.updateSoldQuantity(flashSaleId, quantity);
    if (updated == 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to update quantity. Retry later");
    }
  }

  // Background tasks
  @Override
  public void processFlashSaleStatus() {
    Instant now = Instant.now();
    // Start upcoming flash sales
    flashSaleRepository.findFlashSalesToStart(now).forEach(fs -> {
      fs.setStatus(Status.LIVE);
      flashSaleRepository.save(fs);
    });
    // End live flash sales that reached end time
    flashSaleRepository.findFlashSalesToEnd(now).forEach(fs -> {
      fs.setStatus(Status.ENDED);
      flashSaleRepository.save(fs);
    });
  }

  @Override
  public void sendFlashSaleNotifications() {
    // Simple implementation: mark as sent
    flashSaleRepository.findFlashSalesForNotification(Instant.now()).forEach(fs -> {
      fs.setNotificationSent(true);
      flashSaleRepository.save(fs);
    });
  }

  private PagedResponse<FlashSaleVm> mapPage(Page<?> page, List<FlashSaleVm> data) {
    return PagedResponse.<FlashSaleVm>builder()
        .results(data)
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .maxPageItems(page.getSize())
        .build();
  }
}
