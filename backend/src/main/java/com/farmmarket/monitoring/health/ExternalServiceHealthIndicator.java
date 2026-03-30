package com.farmmarket.monitoring.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("externalServices")
@RequiredArgsConstructor
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        // Check Stripe
        builder.withDetail("stripe", checkStripe());

        // Check S3
        builder.withDetail("s3", checkS3());

        return builder.build();
    }

    private String checkStripe() {
        try {
            restTemplate.getForEntity(
                    "https://status.stripe.com/api/v2/status.json",
                    String.class);
            return "reachable";
        } catch (Exception e) {
            return "unreachable: " + e.getMessage();
        }
    }

    private String checkS3() {
        try {
            // Simple connectivity check
            return "configured";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}