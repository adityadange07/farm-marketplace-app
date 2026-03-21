package com.farmmarket.repository;

import com.farmmarket.entity.Order;
import com.farmmarket.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // ═══════════════════════════════════════════
    // FIND QUERIES
    // ═══════════════════════════════════════════

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "LEFT JOIN FETCH o.farm " +
            "LEFT JOIN FETCH o.consumer " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") UUID id);

    Page<Order> findByConsumerIdOrderByCreatedAtDesc(
            UUID consumerId, Pageable pageable);

    Page<Order> findByFarmIdOrderByCreatedAtDesc(
            UUID farmId, Pageable pageable);

    Page<Order> findByFarmIdAndStatusOrderByCreatedAtDesc(
            UUID farmId, OrderStatus status, Pageable pageable);

    // ═══════════════════════════════════════════
    // ★★★ COUNTING — DASHBOARD STATS ★★★
    // ═══════════════════════════════════════════

    /**
     * Count orders by farmer and status.
     * Joins through farm → farmer relationship.
     */
    @Query("SELECT COUNT(o) FROM Order o " +
            "JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId " +
            "AND o.status = :status")
    int countByFarmerAndStatus(
            @Param("farmerId") UUID farmerId,
            @Param("status") OrderStatus status);

    /**
     * Count all orders for a farmer (any status).
     */
    @Query("SELECT COUNT(o) FROM Order o " +
            "JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId")
    int countAllByFarmer(@Param("farmerId") UUID farmerId);

    /**
     * Count today's orders for a farmer.
     */
    @Query("SELECT COUNT(o) FROM Order o " +
            "JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId " +
            "AND o.placedAt >= :todayStart")
    int countTodayOrders(
            @Param("farmerId") UUID farmerId,
            @Param("todayStart") LocalDateTime todayStart);

    // ═══════════════════════════════════════════
    // ★★★ REVENUE — DASHBOARD STATS ★★★
    // ═══════════════════════════════════════════

    /**
     * Sum revenue since a given date (delivered orders only).
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId " +
            "AND o.status = 'DELIVERED' " +
            "AND o.deliveredAt >= :since")
    BigDecimal sumRevenueSince(
            @Param("farmerId") UUID farmerId,
            @Param("since") LocalDateTime since);

    /**
     * Sum revenue between two dates.
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId " +
            "AND o.status = 'DELIVERED' " +
            "AND o.deliveredAt >= :start " +
            "AND o.deliveredAt < :end")
    BigDecimal sumRevenueBetween(
            @Param("farmerId") UUID farmerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Today's revenue.
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
            "JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId " +
            "AND o.status = 'DELIVERED' " +
            "AND o.deliveredAt >= :todayStart")
    BigDecimal sumTodayRevenue(
            @Param("farmerId") UUID farmerId,
            @Param("todayStart") LocalDateTime todayStart);

    // ═══════════════════════════════════════════
    // ★★★ CHART DATA — DAILY REVENUE ★★★
    // ═══════════════════════════════════════════

    /**
     * Daily revenue for chart (MySQL native).
     * Returns date + revenue per day for the last N days.
     */
    @Query(value = """
        SELECT
            DATE(o.delivered_at) AS order_date,
            COALESCE(SUM(o.total), 0) AS daily_revenue,
            COUNT(o.id) AS order_count
        FROM orders o
        JOIN farms f ON o.farm_id = f.id
        WHERE f.farmer_id = :farmerId
        AND o.status = 'DELIVERED'
        AND o.delivered_at >= :startDate
        GROUP BY DATE(o.delivered_at)
        ORDER BY order_date ASC
        """, nativeQuery = true)
    List<Object[]> findDailyRevenue(
            @Param("farmerId") String farmerId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * Weekly revenue for chart.
     */
    @Query(value = """
        SELECT
            YEARWEEK(o.delivered_at, 1) AS year_week,
            MIN(DATE(o.delivered_at)) AS week_start,
            COALESCE(SUM(o.total), 0) AS weekly_revenue,
            COUNT(o.id) AS order_count
        FROM orders o
        JOIN farms f ON o.farm_id = f.id
        WHERE f.farmer_id = :farmerId
        AND o.status = 'DELIVERED'
        AND o.delivered_at >= :startDate
        GROUP BY YEARWEEK(o.delivered_at, 1)
        ORDER BY year_week ASC
        """, nativeQuery = true)
    List<Object[]> findWeeklyRevenue(
            @Param("farmerId") String farmerId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * Monthly revenue for chart.
     */
    @Query(value = """
        SELECT
            DATE_FORMAT(o.delivered_at, '%Y-%m') AS month_key,
            DATE_FORMAT(o.delivered_at, '%b %Y') AS month_label,
            COALESCE(SUM(o.total), 0) AS monthly_revenue,
            COUNT(o.id) AS order_count
        FROM orders o
        JOIN farms f ON o.farm_id = f.id
        WHERE f.farmer_id = :farmerId
        AND o.status = 'DELIVERED'
        AND o.delivered_at >= :startDate
        GROUP BY month_key, month_label
        ORDER BY month_key ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyRevenue(
            @Param("farmerId") String farmerId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * Revenue by status (pie chart data).
     */
    @Query("SELECT o.status, COUNT(o), COALESCE(SUM(o.total), 0) " +
            "FROM Order o JOIN o.farm f " +
            "WHERE f.farmer.id = :farmerId " +
            "AND o.placedAt >= :since " +
            "GROUP BY o.status")
    List<Object[]> findRevenueByStatus(
            @Param("farmerId") UUID farmerId,
            @Param("since") LocalDateTime since);
}