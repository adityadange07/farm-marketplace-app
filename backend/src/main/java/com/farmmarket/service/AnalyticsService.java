package com.farmmarket.service;

import com.farmmarket.dto.response.DashboardStatsResponse;
import com.farmmarket.entity.Product;
import com.farmmarket.entity.User;
import com.farmmarket.enums.OrderStatus;
import com.farmmarket.enums.ProductStatus;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.repository.FarmRepository;
import com.farmmarket.repository.OrderRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FarmRepository farmRepository;
    private final UserRepository userRepository;

    /**
     * Complete farmer dashboard stats including:
     * - Today's revenue & change %
     * - Pending orders count
     * - Active / low-stock product counts
     * - Monthly revenue & change %
     * - Revenue chart (daily for last 30 days)
     * - Top selling products
     */
    public DashboardStatsResponse getFarmerDashboardStats(UUID farmerId) {

        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Farmer not found"));

        // ── Time boundaries ─────────────────
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime monthStart = LocalDate.now()
                .withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime last30Days = now.minusDays(30);
        LocalDateTime last7Days = now.minusDays(7);

        // ═══════════════════════════════════
        // 1. REVENUE STATS
        // ═══════════════════════════════════

        BigDecimal todayRevenue = orderRepository
                .sumTodayRevenue(farmerId, todayStart);

        BigDecimal yesterdayRevenue = orderRepository
                .sumRevenueBetween(farmerId, yesterdayStart, todayStart);

        BigDecimal monthlyRevenue = orderRepository
                .sumRevenueSince(farmerId, monthStart);

        BigDecimal lastMonthRevenue = orderRepository
                .sumRevenueBetween(farmerId, lastMonthStart, monthStart);

        // Calculate % changes
        double todayChange = calculatePercentChange(
                yesterdayRevenue, todayRevenue);
        double monthlyChange = calculatePercentChange(
                lastMonthRevenue, monthlyRevenue);

        // ═══════════════════════════════════
        // 2. ORDER COUNTS
        // ═══════════════════════════════════

        int pendingOrders = orderRepository
                .countByFarmerAndStatus(farmerId, OrderStatus.PENDING);

        int confirmedOrders = orderRepository
                .countByFarmerAndStatus(farmerId, OrderStatus.CONFIRMED);

        int processingOrders = orderRepository
                .countByFarmerAndStatus(farmerId, OrderStatus.PROCESSING);

        int todayOrderCount = orderRepository
                .countTodayOrders(farmerId, todayStart);

        int totalOrders = orderRepository
                .countAllByFarmer(farmerId);

        // ═══════════════════════════════════
        // 3. PRODUCT COUNTS
        // ═══════════════════════════════════

        int activeProducts = productRepository
                .countByFarmerAndStatus(farmerId, ProductStatus.ACTIVE);

        int draftProducts = productRepository
                .countByFarmerAndStatus(farmerId, ProductStatus.DRAFT);

        int lowStockProducts = productRepository
                .countLowStockProducts(farmerId);

        int outOfStockProducts = productRepository
                .countOutOfStockProducts(farmerId);

        // ═══════════════════════════════════
        // 4. REVENUE CHART (Last 30 days)
        // ═══════════════════════════════════

        List<Map<String, Object>> revenueChart =
                buildRevenueChart(farmerId, last30Days);

        // ═══════════════════════════════════
        // 5. TOP PRODUCTS
        // ═══════════════════════════════════

        List<Map<String, Object>> topProducts =
                buildTopProducts(farmerId);

        // ═══════════════════════════════════
        // 6. BUILD RESPONSE
        // ═══════════════════════════════════

        return DashboardStatsResponse.builder()
                .farmerName(farmer.getFirstName())
                // Revenue
                .todayRevenue(todayRevenue)
                .revenueChange(todayChange)
                .monthlyRevenue(monthlyRevenue)
                .monthlyChange(monthlyChange)
                // Orders
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .processingOrders(processingOrders)
                .todayOrders(todayOrderCount)
                .totalOrders(totalOrders)
                // Products
                .activeProducts(activeProducts)
                .draftProducts(draftProducts)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                // Charts
                .revenueChart(revenueChart)
                .topProducts(topProducts)
                .build();
    }

    // ═══════════════════════════════════════════
    // REVENUE CHART BUILDER
    // ═══════════════════════════════════════════

    private List<Map<String, Object>> buildRevenueChart(
            UUID farmerId, LocalDateTime startDate) {
        try {
            // Fetch daily revenue from DB
            List<Object[]> dailyData = orderRepository
                    .findDailyRevenue(farmerId.toString(), startDate);

            // Create a map of date → revenue
            Map<String, BigDecimal> revenueByDate = new LinkedHashMap<>();
            Map<String, Integer> ordersByDate = new LinkedHashMap<>();

            for (Object[] row : dailyData) {
                String date = row[0].toString(); // DATE(delivered_at)
                BigDecimal revenue = row[1] instanceof BigDecimal
                        ? (BigDecimal) row[1]
                        : new BigDecimal(row[1].toString());
                int count = row[2] instanceof Long
                        ? ((Long) row[2]).intValue()
                        : Integer.parseInt(row[2].toString());
                revenueByDate.put(date, revenue);
                ordersByDate.put(date, count);
            }

            // Fill in missing dates with zero
            List<Map<String, Object>> chart = new ArrayList<>();
            LocalDate current = startDate.toLocalDate();
            LocalDate today = LocalDate.now();
            DateTimeFormatter dayFmt =
                    DateTimeFormatter.ofPattern("MMM dd");

            while (!current.isAfter(today)) {
                String dateKey = current.toString();
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("date", dateKey);
                point.put("name", current.format(dayFmt));
                point.put("dayOfWeek", current.getDayOfWeek()
                        .toString().substring(0, 3));
                point.put("revenue",
                        revenueByDate.getOrDefault(dateKey, BigDecimal.ZERO));
                point.put("orders",
                        ordersByDate.getOrDefault(dateKey, 0));
                chart.add(point);
                current = current.plusDays(1);
            }

            return chart;
        } catch (Exception e) {
            log.error("Failed to build revenue chart for farmer {}",
                    farmerId, e);
            return generateEmptyChart(startDate.toLocalDate());
        }
    }

    // ═══════════════════════════════════════════
    // TOP PRODUCTS BUILDER
    // ═══════════════════════════════════════════

    private List<Map<String, Object>> buildTopProducts(UUID farmerId) {
        try {
            // Method 1: Use native query for revenue calculation
            List<Object[]> rawData = productRepository
                    .findTopProductsByRevenue(farmerId.toString(), 10);

            if (!rawData.isEmpty()) {
                return rawData.stream()
                        .map(row -> {
                            Map<String, Object> product = new LinkedHashMap<>();
                            product.put("id", row[0].toString());
                            product.put("name", row[1].toString());
                            product.put("totalSold",
                                    row[2] instanceof Number
                                            ? ((Number) row[2]).intValue() : 0);
                            product.put("avgRating",
                                    row[3] instanceof Number
                                            ? ((Number) row[3]).doubleValue() : 0.0);
                            product.put("price",
                                    row[4] instanceof BigDecimal
                                            ? (BigDecimal) row[4]
                                            : new BigDecimal(row[4].toString()));
                            product.put("revenue",
                                    row[5] instanceof BigDecimal
                                            ? (BigDecimal) row[5]
                                            : new BigDecimal(row[5].toString()));
                            return product;
                        })
                        .collect(Collectors.toList());
            }

            // Method 2: Fallback — use JPA entities
            List<Product> topSelling = productRepository
                    .findTopSellingByFarmer(farmerId,
                            PageRequest.of(0, 10));

            return topSelling.stream()
                    .map(p -> {
                        Map<String, Object> product = new LinkedHashMap<>();
                        product.put("id", p.getId().toString());
                        product.put("name", p.getName());
                        product.put("totalSold", p.getTotalSold());
                        product.put("avgRating", p.getAvgRating());
                        product.put("price", p.getPrice());
                        product.put("revenue",
                                p.getPrice().multiply(
                                        BigDecimal.valueOf(p.getTotalSold())));
                        // Get primary image
                        String image = p.getImages() != null
                                && !p.getImages().isEmpty()
                                ? p.getImages().stream()
                                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                                .findFirst()
                                .orElse(p.getImages().get(0))
                                .getUrl()
                                : null;
                        product.put("image", image);
                        return product;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to build top products for farmer {}",
                    farmerId, e);
            return Collections.emptyList();
        }
    }

    // ═══════════════════════════════════════════
    // EXTENDED ANALYTICS
    // ═══════════════════════════════════════════

    /**
     * Detailed analytics for the farmer analytics page.
     */
    public Map<String, Object> getDetailedAnalytics(
            UUID farmerId, String period) {

        LocalDateTime startDate = switch (period) {
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "30d" -> LocalDateTime.now().minusDays(30);
            case "90d" -> LocalDateTime.now().minusDays(90);
            case "1y" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusDays(30);
        };

        Map<String, Object> analytics = new LinkedHashMap<>();

        // Revenue summary
        BigDecimal totalRevenue = orderRepository
                .sumRevenueSince(farmerId, startDate);
        int totalOrders = orderRepository
                .countAllByFarmer(farmerId);

        analytics.put("totalRevenue", totalRevenue);
        analytics.put("totalOrders", totalOrders);
        analytics.put("avgOrderValue",
                totalOrders > 0
                        ? totalRevenue.divide(
                        BigDecimal.valueOf(totalOrders),
                        2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

        // Revenue chart
        if ("1y".equals(period) || "90d".equals(period)) {
            analytics.put("revenueChart",
                    buildMonthlyChart(farmerId, startDate));
        } else {
            analytics.put("revenueChart",
                    buildRevenueChart(farmerId, startDate));
        }

        // Order status breakdown
        List<Object[]> statusData = orderRepository
                .findRevenueByStatus(farmerId, startDate);
        List<Map<String, Object>> statusBreakdown = statusData.stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("status", row[0].toString());
                    item.put("count", ((Number) row[1]).intValue());
                    item.put("revenue", row[2]);
                    return item;
                })
                .collect(Collectors.toList());
        analytics.put("ordersByStatus", statusBreakdown);

        // Top products
        analytics.put("topProducts", buildTopProducts(farmerId));

        return analytics;
    }

    // ═══════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════

    private double calculatePercentChange(
            BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0
                    ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private List<Map<String, Object>> generateEmptyChart(
            LocalDate startDate) {
        List<Map<String, Object>> chart = new ArrayList<>();
        LocalDate current = startDate;
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");

        while (!current.isAfter(today)) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", current.toString());
            point.put("name", current.format(fmt));
            point.put("revenue", BigDecimal.ZERO);
            point.put("orders", 0);
            chart.add(point);
            current = current.plusDays(1);
        }
        return chart;
    }

    private List<Map<String, Object>> buildMonthlyChart(
            UUID farmerId, LocalDateTime startDate) {
        try {
            List<Object[]> monthlyData = orderRepository
                    .findMonthlyRevenue(farmerId.toString(), startDate);

            return monthlyData.stream()
                    .map(row -> {
                        Map<String, Object> point = new LinkedHashMap<>();
                        point.put("name", row[1].toString());
                        point.put("revenue",
                                row[2] instanceof BigDecimal
                                        ? (BigDecimal) row[2]
                                        : new BigDecimal(row[2].toString()));
                        point.put("orders",
                                row[3] instanceof Number
                                        ? ((Number) row[3]).intValue() : 0);
                        return point;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to build monthly chart", e);
            return Collections.emptyList();
        }
    }
}