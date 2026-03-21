package com.farmmarket.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // ── Farmer Info ─────────────
    private String farmerName;

    // ── Revenue ─────────────────
    private BigDecimal todayRevenue;
    private double revenueChange;         // vs yesterday %
    private BigDecimal monthlyRevenue;
    private double monthlyChange;         // vs last month %

    // ── Order Counts ────────────
    private int pendingOrders;
    private int confirmedOrders;
    private int processingOrders;
    private int todayOrders;
    private int totalOrders;

    // ── Product Counts ──────────
    private int activeProducts;
    private int draftProducts;
    private int lowStockProducts;
    private int outOfStockProducts;

    // ── Chart Data ──────────────
    // Each entry: { name: "Jan 15", date: "2024-01-15",
    //               revenue: 125.50, orders: 3 }
    private List<Map<String, Object>> revenueChart;

    // ── Top Products ────────────
    // Each entry: { id, name, totalSold, avgRating,
    //               price, revenue, image }
    private List<Map<String, Object>> topProducts;
}