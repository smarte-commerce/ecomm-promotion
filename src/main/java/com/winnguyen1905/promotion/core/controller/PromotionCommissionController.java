package com.winnguyen1905.promotion.core.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
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
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotion-commissions")
@RequiredArgsConstructor
public class PromotionCommissionController {
    
    private final PromotionCommissionService commissionService;
    
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<PromotionCommissionVm>> getCommission(
            TAccountRequest accountRequest,
            @PathVariable UUID id) {
        
        PromotionCommissionVm commission = commissionService.getCommissionById(accountRequest, id);
        
        return ResponseEntity.ok(RestResponse.<PromotionCommissionVm>builder()
            .data(commission)
            .message("Commission retrieved successfully")
            .build());
    }
    
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<RestResponse<PagedResponse<PromotionCommissionVm>>> getVendorCommissions(
            TAccountRequest accountRequest,
            @PathVariable UUID vendorId,
            Pageable pageable) {
        
        PagedResponse<PromotionCommissionVm> commissions = commissionService.getVendorCommissions(
            accountRequest, vendorId, pageable);
        
        return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionCommissionVm>>builder()
            .data(commissions)
            .message("Vendor commissions retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}")
    public ResponseEntity<RestResponse<PagedResponse<PromotionCommissionVm>>> getProgramCommissions(
            TAccountRequest accountRequest,
            @PathVariable UUID programId,
            Pageable pageable) {
        
        PagedResponse<PromotionCommissionVm> commissions = commissionService.getProgramCommissions(
            accountRequest, programId, pageable);
        
        return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionCommissionVm>>builder()
            .data(commissions)
            .message("Program commissions retrieved successfully")
            .build());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<RestResponse<PagedResponse<PromotionCommissionVm>>> getCommissionsByStatus(
            TAccountRequest accountRequest,
            @PathVariable PaymentStatus status,
            Pageable pageable) {
        
        PagedResponse<PromotionCommissionVm> commissions = commissionService.getCommissionsByStatus(
            accountRequest, status, pageable);
        
        return ResponseEntity.ok(RestResponse.<PagedResponse<PromotionCommissionVm>>builder()
            .data(commissions)
            .message("Commissions by status retrieved successfully")
            .build());
    }
    
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<RestResponse<Void>> updatePaymentStatus(
            TAccountRequest accountRequest,
            @PathVariable UUID id,
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) String transactionId) {
        
        commissionService.updatePaymentStatus(accountRequest, id, status, transactionId);
        
        return ResponseEntity.ok(RestResponse.<Void>builder()
            .message("Payment status updated successfully")
            .build());
    }
    
    @PostMapping("/{id}/process")
    public ResponseEntity<RestResponse<Void>> processCommissionPayment(
            TAccountRequest accountRequest,
            @PathVariable UUID id) {
        
        commissionService.processCommissionPayment(accountRequest, id);
        
        return ResponseEntity.ok(RestResponse.<Void>builder()
            .message("Commission payment processed successfully")
            .build());
    }
    
    @PostMapping("/vendor/{vendorId}/process-all")
    public ResponseEntity<RestResponse<Void>> processVendorCommissions(
            TAccountRequest accountRequest,
            @PathVariable UUID vendorId) {
        
        commissionService.processVendorCommissionPayments(vendorId);
        
        return ResponseEntity.ok(RestResponse.<Void>builder()
            .message("Vendor commissions processed successfully")
            .build());
    }
    
    @GetMapping("/vendor/{vendorId}/total")
    public ResponseEntity<RestResponse<Double>> getTotalVendorCommission(
            TAccountRequest accountRequest,
            @PathVariable UUID vendorId,
            @RequestParam PaymentStatus status) {
        
        Double total = commissionService.getTotalVendorCommission(vendorId, status);
        
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .data(total)
            .message("Total vendor commission retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/total")
    public ResponseEntity<RestResponse<Double>> getTotalProgramCommission(
            TAccountRequest accountRequest,
            @PathVariable UUID programId) {
        
        Double total = commissionService.getTotalProgramCommission(programId);
        
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .data(total)
            .message("Total program commission retrieved successfully")
            .build());
    }
} 
