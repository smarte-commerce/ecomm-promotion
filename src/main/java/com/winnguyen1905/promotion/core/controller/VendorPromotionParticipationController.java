package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.VendorPromotionParticipationService;
import com.winnguyen1905.promotion.model.request.VendorParticipationRequest;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.model.response.VendorParticipationVm;
import com.winnguyen1905.promotion.persistance.entity.EVendorPromotionParticipation.Status;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.ResponseMessage;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/vendor-promotion-participations")
@RequiredArgsConstructor
@Tag(name = "Vendor Promotion Participation", description = "Vendor Promotion Participation Management API")
public class VendorPromotionParticipationController {

  private final VendorPromotionParticipationService participationService;

  @PostMapping
  @Operation(summary = "Request participation in a promotion program", description = "Allows a vendor to request participation in a promotion program")
  public ResponseEntity<RestResponse<Void>> requestParticipation(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody VendorParticipationRequest request) {
    participationService.requestParticipation(accountRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RestResponse.<Void>builder()
            .statusCode(HttpStatus.CREATED.value())
            .message("Participation request submitted successfully")
            .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get participation by ID", description = "Retrieve detailed information about a specific participation")
  public ResponseEntity<RestResponse<VendorParticipationVm>> getParticipationById(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    VendorParticipationVm participation = participationService.getParticipationById(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<VendorParticipationVm>builder()
        .statusCode(HttpStatus.OK.value())
        .data(participation)
        .message("Participation retrieved successfully")
        .build());
  }

  @GetMapping("/vendor/{vendorId}")
  @Operation(summary = "Get vendor participations", description = "Get all participations for a specific vendor with pagination")
  public ResponseEntity<RestResponse<PagedResponse<VendorParticipationVm>>> getVendorParticipations(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID vendorId,
      Pageable pageable) {
    PagedResponse<VendorParticipationVm> participations = participationService.getVendorParticipations(
        accountRequest, vendorId, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<VendorParticipationVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(participations)
        .message("Vendor participations retrieved successfully")
        .build());
  }

  @GetMapping("/program/{programId}")
  @Operation(summary = "Get program participations", description = "Get all vendor participations for a specific promotion program")
  public ResponseEntity<RestResponse<PagedResponse<VendorParticipationVm>>> getProgramParticipations(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID programId,
      Pageable pageable) {
    PagedResponse<VendorParticipationVm> participations = participationService.getProgramParticipations(
        accountRequest, programId, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<VendorParticipationVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(participations)
        .message("Program participations retrieved successfully")
        .build());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update participation", description = "Update participation details (vendor only)")
  public ResponseEntity<RestResponse<Void>> updateParticipation(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @Valid @RequestBody VendorParticipationRequest request) {
    participationService.updateParticipation(accountRequest, id, request);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Participation updated successfully")
        .build());
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Update participation status", description = "Update the status of a participation (admin only)")
  public ResponseEntity<RestResponse<Void>> updateParticipationStatus(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @RequestParam Status status,
      @RequestParam(required = false) String reason) {
    participationService.updateParticipationStatus(accountRequest, id, status, reason);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Participation status updated successfully")
        .build());
  }

  @PatchMapping("/{id}/approve")
  @Operation(summary = "Approve participation", description = "Approve a vendor's participation request (admin only)")
  public ResponseEntity<RestResponse<Void>> approveParticipation(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    participationService.approveParticipation(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Participation approved successfully")
        .build());
  }

  @PatchMapping("/{id}/reject")
  @Operation(summary = "Reject participation", description = "Reject a vendor's participation request (admin only)")
  public ResponseEntity<RestResponse<Void>> rejectParticipation(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @RequestParam String reason) {
    participationService.rejectParticipation(accountRequest, id, reason);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Participation rejected successfully")
        .build());
  }

  @PatchMapping("/{id}/withdraw")
  @Operation(summary = "Withdraw participation", description = "Withdraw from a promotion program (vendor only)")
  public ResponseEntity<RestResponse<Void>> withdrawParticipation(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id,
      @RequestParam String reason) {
    participationService.withdrawParticipation(accountRequest, id, reason);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Participation withdrawn successfully")
        .build());
  }

  @PostMapping("/{id}/calculate-metrics")
  @Operation(summary = "Calculate performance metrics", description = "Calculate and update performance metrics for a participation")
  public ResponseEntity<RestResponse<Void>> calculatePerformanceMetrics(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    participationService.calculatePerformanceMetrics(id);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Performance metrics calculated successfully")
        .build());
  }

  @GetMapping("/my-participations")
  @Operation(summary = "Get current user's participations", description = "Get all participations for the current authenticated vendor")
  public ResponseEntity<RestResponse<PagedResponse<VendorParticipationVm>>> getMyParticipations(
      @AccountRequest TAccountRequest accountRequest,
      Pageable pageable) {
    PagedResponse<VendorParticipationVm> participations = participationService.getVendorParticipations(
        accountRequest, accountRequest.id(), pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<VendorParticipationVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(participations)
        .message("My participations retrieved successfully")
        .build());
  }

  @GetMapping("/status/{status}")
  @Operation(summary = "Get participations by status", description = "Get all participations filtered by status (admin only)")
  public ResponseEntity<RestResponse<PagedResponse<VendorParticipationVm>>> getParticipationsByStatus(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable Status status,
      Pageable pageable) {
    PagedResponse<VendorParticipationVm> participations = participationService.getParticipationsByStatus(
        accountRequest, status, pageable);
    return ResponseEntity.ok(RestResponse.<PagedResponse<VendorParticipationVm>>builder()
        .statusCode(HttpStatus.OK.value())
        .data(participations)
        .message("Participations by status retrieved successfully")
        .build());
  }

  @GetMapping("/pending-approval")
  @Operation(summary = "Get pending approval participations", description = "Get all participations pending approval (admin only)")
  public ResponseEntity<RestResponse<PagedResponse<VendorParticipationVm>>> getPendingApprovalParticipations(
      @AccountRequest TAccountRequest accountRequest,
      Pageable pageable) {
    return getParticipationsByStatus(accountRequest, Status.PENDING, pageable);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete participation", description = "Permanently delete a participation record (admin only)")
  public ResponseEntity<RestResponse<Void>> deleteParticipation(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable UUID id) {
    participationService.deleteParticipation(accountRequest, id);
    return ResponseEntity.ok(RestResponse.<Void>builder()
        .statusCode(HttpStatus.OK.value())
        .message("Participation deleted successfully")
        .build());
  }
}
