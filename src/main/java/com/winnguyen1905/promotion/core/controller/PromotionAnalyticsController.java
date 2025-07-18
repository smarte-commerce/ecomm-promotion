package com.winnguyen1905.promotion.core.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.promotion.core.service.PromotionAnalyticsService;
import com.winnguyen1905.promotion.model.response.PromotionAnalyticsVm;
import com.winnguyen1905.promotion.model.response.RestResponse;
import com.winnguyen1905.promotion.secure.AccountRequest;
import com.winnguyen1905.promotion.secure.TAccountRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/promotion-analytics")
@RequiredArgsConstructor
@Tag(name = "Promotion Analytics", description = "Promotion Analytics API")
public class PromotionAnalyticsController {
    
    private final PromotionAnalyticsService analyticsService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get analytics by ID")
    public ResponseEntity<RestResponse<PromotionAnalyticsVm>> getAnalytics(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID id) {
        PromotionAnalyticsVm analytics = analyticsService.getAnalyticsById(accountRequest, id);
        return ResponseEntity.ok(RestResponse.<PromotionAnalyticsVm>builder()
            .statusCode(HttpStatus.OK.value())
            .data(analytics)
            .message("Analytics retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}")
    @Operation(summary = "Get program analytics")
    public ResponseEntity<RestResponse<List<PromotionAnalyticsVm>>> getProgramAnalytics(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID programId) {
        List<PromotionAnalyticsVm> analytics = analyticsService.getProgramAnalytics(accountRequest, programId);
        return ResponseEntity.ok(RestResponse.<List<PromotionAnalyticsVm>>builder()
            .statusCode(HttpStatus.OK.value())
            .data(analytics)
            .message("Program analytics retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/date-range")
    @Operation(summary = "Get program analytics by date range")
    public ResponseEntity<RestResponse<List<PromotionAnalyticsVm>>> getProgramAnalyticsByDateRange(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PromotionAnalyticsVm> analytics = analyticsService.getProgramAnalyticsByDateRange(
            accountRequest, programId, startDate, endDate);
        return ResponseEntity.ok(RestResponse.<List<PromotionAnalyticsVm>>builder()
            .statusCode(HttpStatus.OK.value())
            .data(analytics)
            .message("Program analytics by date range retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/roi")
    @Operation(summary = "Get program ROI")
    public ResponseEntity<RestResponse<Double>> getProgramROI(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double roi = analyticsService.calculateProgramROI(programId, startDate, endDate);
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .statusCode(HttpStatus.OK.value())
            .data(roi)
            .message("Program ROI retrieved successfully")
            .build());
    }
    
    @GetMapping("/program/{programId}/conversion-rate")
    @Operation(summary = "Get program conversion rate")
    public ResponseEntity<RestResponse<Double>> getProgramConversionRate(
            @AccountRequest TAccountRequest accountRequest,
            @PathVariable UUID programId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double conversionRate = analyticsService.calculateProgramConversionRate(programId, startDate, endDate);
        return ResponseEntity.ok(RestResponse.<Double>builder()
            .statusCode(HttpStatus.OK.value())
            .data(conversionRate)
            .message("Program conversion rate retrieved successfully")
            .build());
    }
} 
