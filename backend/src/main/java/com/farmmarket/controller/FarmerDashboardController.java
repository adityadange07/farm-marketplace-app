package com.farmmarket.controller;

import com.farmmarket.dto.response.ApiResponse;
import com.farmmarket.dto.response.DashboardStatsResponse;
import com.farmmarket.security.CustomUserDetails;
import com.farmmarket.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/farmer")
@PreAuthorize("hasRole('FARMER')")
@RequiredArgsConstructor
public class FarmerDashboardController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/farmer/dashboard/stats
     * Main dashboard — stats, chart, top products
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(
            @AuthenticationPrincipal CustomUserDetails user) {

        DashboardStatsResponse stats =
                analyticsService.getFarmerDashboardStats(user.getId());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * GET /api/farmer/analytics?period=30d
     * Detailed analytics page
     */
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "30d") String period) {

        Map<String, Object> analytics =
                analyticsService.getDetailedAnalytics(user.getId(), period);

        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}