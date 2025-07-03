package com.winnguyen1905.promotion.core.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.PromotionAnalyticsService;
import com.winnguyen1905.promotion.model.response.PromotionAnalyticsVm;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotion-analytics")
@RequiredArgsConstructor
public class PromotionAnalyticsController {
    
    private final PromotionAnalyticsService analyticsService;
    
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<PromotionAnalyticsVm>> getAnalytics(
            TAccountRequest accountRequest,
            @PathVariable UUID id) {
        
        PromotionAnalyticsVm analytics = analyticsService.getAnalyticsById(accountRequest, id);
        
        return ResponseEntity.ok(RestResponse.<PromotionAnalyticsVm>builder()
            .data(analytics)
            .message("Analytics retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}")
    public ResponseEntity<RestResponse<List<PromotionAnalyticsVm>>> getProgramAnalytics(
            TAccountRequest accountRequest,
            @PathVariable UUID programId) {
        
        List<PromotionAnalyticsVm> analytics = analyticsService.getProgramAnalytics(accountRequest, programId);
        
        return ResponseEntity.ok(RestResponse.<List<PromotionAnalyticsVm>>builder()
            .data(analytics)
            .message("Program analytics retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/date-range")
    public ResponseEntity<RestResponse<List<PromotionAnalyticsVm>>> getProgramAnalyticsByDateRange(
            TAccountRequest accountRequest,
            @PathVariable UUID programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PromotionAnalyticsVm> analytics = analyticsService.getProgramAnalyticsByDateRange(
            accountRequest, programId, startDate, endDate);
        
        return ResponseEntity.ok(RestResponse.<List<PromotionAnalyticsVm>>builder()
            .data(analytics)
            .message("Program analytics for date range retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/roi")
    public ResponseEntity<RestResponse<Double>> getProgramROI(
            TAccountRequest accountRequest,
            @PathVariable UUID programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Double roi = analyticsService.calculateProgramROI(programId, startDate, endDate);
        
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .data(roi)
            .message("Program ROI calculated successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/conversion-rate")
    public ResponseEntity<RestResponse<Double>> getProgramConversionRate(
            TAccountRequest accountRequest,
            @PathVariable UUID programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Double conversionRate = analyticsService.calculateProgramConversionRate(programId, startDate, endDate);
        
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .data(conversionRate)
            .message("Program conversion rate calculated successfully")
            .build());
    }
} 
