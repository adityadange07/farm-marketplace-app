package com.farmmarket.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            long start = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long duration = System.currentTimeMillis() - start;

            Integer userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users", Integer.class);
            Integer productCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'",
                    Integer.class);
            Integer orderCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM orders", Integer.class);

            return Health.up()
                    .withDetail("responseTime", duration + "ms")
                    .withDetail("users", userCount)
                    .withDetail("activeProducts", productCount)
                    .withDetail("totalOrders", orderCount)
                    .withDetail("database", "MySQL 8.0")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
