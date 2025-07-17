package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.PromotionCommissionService;
import com.winnguyen1905.promotion.model.response.PagedResponse;
import com.winnguyen1905.promotion.model.response.PromotionCommissionVm;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.persistance.entity.EPromotionCommission.PaymentStatus;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotion-commissions")
@RequiredArgsConstructor
@Tag(name = "Promotion Commissions", description = "Promotion Commission Management API")
public class PromotionCommissionController {
    
    private final PromotionCommissionService commissionService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get commission by ID")
    public ResponseEntity<RestResponse<PromotionCommissionVm>> getCommission(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID id) {
        PromotionCommissionVm commission = commissionService.getCommissionById(accountRequest, id);
        return ResponseEntity.ok(RestResponse.<PromotionCommissionVm>builder()
            .statusCode(HttpStatus.OK.value())
            .data(commission)
            .message("Commission retrieved successfully")
            .build());
    }
    
    @GetMapping("/vendor/{vendorId}")
    @Operation(summary = "Get vendor commissions")
    public ResponseEntity<RestResponse<PagedResponse<PromotionCommissionVm>>> getVendorCommissions(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID vendorId,
            Pageable pageable) {
        PagedResponse<PromotionCommissionVm> commissions = commissionService.getVendorCommissions(
            accountRequest, vendorId, pageable);
        return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionCommissionVm>>builder()
            .statusCode(HttpStatus.OK.value())
            .data(commissions)
            .message("Vendor commissions retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}")
    @Operation(summary = "Get program commissions")
    public ResponseEntity<RestResponse<PagedResponse<PromotionCommissionVm>>> getProgramCommissions(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID programId,
            Pageable pageable) {
        PagedResponse<PromotionCommissionVm> commissions = commissionService.getProgramCommissions(
            accountRequest, programId, pageable);
        return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionCommissionVm>>builder()
            .statusCode(HttpStatus.OK.value())
            .data(commissions)
            .message("Program commissions retrieved successfully")
            .build());
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get commissions by status")
    public ResponseEntity<RestResponse<PagedResponse<PromotionCommissionVm>>> getCommissionsByStatus(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable PaymentStatus status,
            Pageable pageable) {
        PagedResponse<PromotionCommissionVm> commissions = commissionService.getCommissionsByStatus(
            accountRequest, status, pageable);
        return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionCommissionVm>>builder()
            .statusCode(HttpStatus.OK.value())
            .data(commissions)
            .message("Commissions by status retrieved successfully")
            .build());
    }
    
    @PutMapping("/{id}/payment-status")
    @Operation(summary = "Update payment status")
    public ResponseEntity<RestResponse<Void>> updatePaymentStatus(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID id,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String transactionId) {
        commissionService.updatePaymentStatus(accountRequest, id, status, transactionId);
        return ResponseEntity.ok(RestResponse.<Void>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Payment status updated successfully")
            .build());
    }
    
    @PostMapping("/{id}/process")
    @Operation(summary = "Process commission payment")
    public ResponseEntity<RestResponse<Void>> processCommissionPayment(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID id) {
        commissionService.processCommissionPayment(accountRequest, id);
        return ResponseEntity.ok(RestResponse.<Void>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Commission payment processed successfully")
            .build());
    }
    
    @PostMapping("/vendor/{vendorId}/process-all")
    @Operation(summary = "Process all vendor commissions")
    public ResponseEntity<RestResponse<Void>> processVendorCommissions(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID vendorId) {
        commissionService.processVendorCommissionPayments(vendorId);
        return ResponseEntity.ok(RestResponse.<Void>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Vendor commissions processed successfully")
            .build());
    }
    
    @GetMapping("/vendor/{vendorId}/total")
    @Operation(summary = "Get total vendor commission")
    public ResponseEntity<RestResponse<Double>> getTotalVendorCommission(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID vendorId,
            @RequestParam PaymentStatus status) {
        Double total = commissionService.getTotalVendorCommission(vendorId, status);
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .statusCode(HttpStatus.OK.value())
            .data(total)
            .message("Total vendor commission retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/total")
    @Operation(summary = "Get total program commission")
    public ResponseEntity<RestResponse<Double>> getTotalProgramCommission(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID programId) {
        Double total = commissionService.getTotalProgramCommission(programId);
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .statusCode(HttpStatus.OK.value())
            .data(total)
            .message("Total program commission retrieved successfully")
            .build());
    }
} 
