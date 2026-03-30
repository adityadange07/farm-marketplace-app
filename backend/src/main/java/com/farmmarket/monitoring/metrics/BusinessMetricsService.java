package com.farmmarket.monitoring.metrics;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessMetricsService {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;

    // ── Counters ──────────────────────────────
    private Counter ordersPlacedCounter;
    private Counter ordersDeliveredCounter;
    private Counter ordersCancelledCounter;
    private Counter usersRegisteredCounter;
    private Counter paymentsProcessedCounter;
    private Counter paymentsFailedCounter;
    private Counter productsCreatedCounter;
    private Counter reviewsCreatedCounter;
    private Counter searchQueriesCounter;

    // ── Gauges (current state) ────────────────
    private AtomicInteger activeUsersGauge;
    private AtomicInteger activeFarmersGauge;
    private AtomicInteger activeProductsGauge;
    private AtomicInteger pendingOrdersGauge;
    private AtomicInteger lowStockProductsGauge;
    private AtomicLong todayRevenueGauge;

    // ── Distribution Summaries ────────────────
    private DistributionSummary orderValueSummary;
    private DistributionSummary cartSizeSummary;

    // ── Timers ────────────────────────────────
    private Timer checkoutTimer;
    private Timer searchTimer;
    private Timer paymentTimer;

    @PostConstruct
    public void initMetrics() {
        // ═══════════════════════════════════
        // COUNTERS — Track cumulative events
        // ═══════════════════════════════════

        ordersPlacedCounter = Counter.builder("farm.orders.placed")
                .description("Total orders placed")
                .tag("type", "placed")
                .register(meterRegistry);

        ordersDeliveredCounter = Counter.builder("farm.orders.delivered")
                .description("Total orders delivered")
                .tag("type", "delivered")
                .register(meterRegistry);

        ordersCancelledCounter = Counter.builder("farm.orders.cancelled")
                .description("Total orders cancelled")
                .tag("type", "cancelled")
                .register(meterRegistry);

        usersRegisteredCounter = Counter.builder("farm.users.registered")
                .description("Total users registered")
                .register(meterRegistry);

        paymentsProcessedCounter = Counter.builder("farm.payments.processed")
                .description("Total payments processed successfully")
                .tag("status", "success")
                .register(meterRegistry);

        paymentsFailedCounter = Counter.builder("farm.payments.failed")
                .description("Total payments failed")
                .tag("status", "failed")
                .register(meterRegistry);

        productsCreatedCounter = Counter.builder("farm.products.created")
                .description("Total products created")
                .register(meterRegistry);

        reviewsCreatedCounter = Counter.builder("farm.reviews.created")
                .description("Total reviews created")
                .register(meterRegistry);

        searchQueriesCounter = Counter.builder("farm.search.queries")
                .description("Total search queries executed")
                .register(meterRegistry);

        // ═══════════════════════════════════
        // GAUGES — Track current state
        // ═══════════════════════════════════

        activeUsersGauge = new AtomicInteger(0);
        Gauge.builder("farm.users.active", activeUsersGauge, AtomicInteger::get)
                .description("Current active users")
                .tag("role", "all")
                .register(meterRegistry);

        activeFarmersGauge = new AtomicInteger(0);
        Gauge.builder("farm.users.active.farmers", activeFarmersGauge,
                        AtomicInteger::get)
                .description("Current active farmers")
                .tag("role", "farmer")
                .register(meterRegistry);

        activeProductsGauge = new AtomicInteger(0);
        Gauge.builder("farm.products.active", activeProductsGauge,
                        AtomicInteger::get)
                .description("Current active products")
                .register(meterRegistry);

        pendingOrdersGauge = new AtomicInteger(0);
        Gauge.builder("farm.orders.pending", pendingOrdersGauge,
                        AtomicInteger::get)
                .description("Current pending orders")
                .register(meterRegistry);

        lowStockProductsGauge = new AtomicInteger(0);
        Gauge.builder("farm.products.low_stock", lowStockProductsGauge,
                        AtomicInteger::get)
                .description("Products with low stock")
                .register(meterRegistry);

        todayRevenueGauge = new AtomicLong(0);
        Gauge.builder("farm.revenue.today", todayRevenueGauge,
                        AtomicLong::get)
                .description("Today's revenue in cents")
                .register(meterRegistry);

        // ═══════════════════════════════════
        // DISTRIBUTION SUMMARIES
        // ═══════════════════════════════════

        orderValueSummary = DistributionSummary.builder("farm.order.value")
                .description("Order value distribution")
                .baseUnit("usd")
                .publishPercentiles(0.5, 0.75, 0.90, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);

        cartSizeSummary = DistributionSummary.builder("farm.cart.size")
                .description("Cart item count distribution")
                .publishPercentiles(0.5, 0.90)
                .register(meterRegistry);

        // ═══════════════════════════════════
        // TIMERS — Track duration
        // ═══════════════════════════════════

        checkoutTimer = Timer.builder("farm.checkout.duration")
                .description("Checkout process duration")
                .publishPercentiles(0.5, 0.90, 0.95, 0.99)
                .register(meterRegistry);

        searchTimer = Timer.builder("farm.search.duration")
                .description("Product search duration")
                .publishPercentiles(0.5, 0.90, 0.95)
                .register(meterRegistry);

        paymentTimer = Timer.builder("farm.payment.duration")
                .description("Payment processing duration")
                .publishPercentiles(0.5, 0.90, 0.95, 0.99)
                .register(meterRegistry);

        log.info("Business metrics initialized");
    }

    // ═══════════════════════════════════════════
    // PUBLIC — Called by Services
    // ═══════════════════════════════════════════

    public void recordOrderPlaced(BigDecimal orderValue, int itemCount) {
        ordersPlacedCounter.increment();
        orderValueSummary.record(orderValue.doubleValue());
        cartSizeSummary.record(itemCount);
    }

    public void recordOrderDelivered() {
        ordersDeliveredCounter.increment();
    }

    public void recordOrderCancelled() {
        ordersCancelledCounter.increment();
    }

    public void recordUserRegistered(String role) {
        usersRegisteredCounter.increment();
        Counter.builder("farm.users.registered.by_role")
                .tag("role", role)
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentSuccess(BigDecimal amount) {
        paymentsProcessedCounter.increment();
        Counter.builder("farm.payments.total_amount")
                .register(meterRegistry)
                .increment(amount.doubleValue());
    }

    public void recordPaymentFailed(String reason) {
        paymentsFailedCounter.increment();
        Counter.builder("farm.payments.failure_reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordProductCreated() {
        productsCreatedCounter.increment();
    }

    public void recordReviewCreated(int rating) {
        reviewsCreatedCounter.increment();
        Counter.builder("farm.reviews.by_rating")
                .tag("stars", String.valueOf(rating))
                .register(meterRegistry)
                .increment();
    }

    public void recordSearch(String query) {
        searchQueriesCounter.increment();
    }

    public Timer.Sample startCheckoutTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCheckoutTimer(Timer.Sample sample) {
        sample.stop(checkoutTimer);
    }

    public Timer.Sample startSearchTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopSearchTimer(Timer.Sample sample) {
        sample.stop(searchTimer);
    }

    public Timer.Sample startPaymentTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPaymentTimer(Timer.Sample sample) {
        sample.stop(paymentTimer);
    }

    // ═══════════════════════════════════════════
    // SCHEDULED — Refresh gauge values
    // ═══════════════════════════════════════════

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void refreshGauges() {
        try (Connection conn = dataSource.getConnection()) {
            activeUsersGauge.set(queryInt(conn,
                    "SELECT COUNT(*) FROM users WHERE is_active = 1"));

            activeFarmersGauge.set(queryInt(conn,
                    "SELECT COUNT(*) FROM users WHERE role = 'FARMER' AND is_active = 1"));

            activeProductsGauge.set(queryInt(conn,
                    "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'"));

            pendingOrdersGauge.set(queryInt(conn,
                    "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'"));

            lowStockProductsGauge.set(queryInt(conn,
                    "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE' " +
                            "AND stock_quantity <= low_stock_threshold AND stock_quantity > 0"));

            long todayCents = queryLong(conn,
                    "SELECT COALESCE(SUM(total * 100), 0) FROM orders " +
                            "WHERE status = 'DELIVERED' AND DATE(delivered_at) = CURDATE()");
            todayRevenueGauge.set(todayCents);

            log.debug("Business gauges refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh gauges", e);
        }
    }

    private int queryInt(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private long queryLong(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}