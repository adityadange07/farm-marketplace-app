package com.farmmarket.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component("redisCache")
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            long start = System.currentTimeMillis();

            // Ping Redis
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection().ping();

            long duration = System.currentTimeMillis() - start;

            // Get memory info
            var info = redisTemplate.getConnectionFactory()
                    .getConnection().serverCommands().info("memory");

            return Health.up()
                    .withDetail("responseTime", duration + "ms")
                    .withDetail("ping", pong)
                    .withDetail("memoryInfo", info != null
                            ? info.getProperty("used_memory_human") : "N/A")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
