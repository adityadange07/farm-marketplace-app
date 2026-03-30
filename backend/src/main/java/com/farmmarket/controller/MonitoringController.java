package com.farmmarket.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

    private final MeterRegistry meterRegistry;

    /**
     * Receive client-side errors from React frontend.
     */
    @PostMapping("/client-errors")
    public ResponseEntity<Void> receiveClientErrors(
            @RequestBody Map<String, List<Map<String, Object>>> body) {

        List<Map<String, Object>> errors = body.get("errors");
        if (errors == null) return ResponseEntity.ok().build();

        for (Map<String, Object> error : errors) {
            String type = (String) error.getOrDefault("type", "unknown");
            String message = (String) error.getOrDefault("message", "");
            String url = (String) error.getOrDefault("url", "");

            log.error("CLIENT ERROR [{}] on {} — {}",
                    type, url, message);

            Counter.builder("farm.client.errors")
                    .tag("type", type)
                    .tag("page", normalizePath(url))
                    .register(meterRegistry)
                    .increment();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Receive Alertmanager webhook.
     */
    @PostMapping("/webhooks/alerts")
    public ResponseEntity<Void> receiveAlert(
            @RequestBody Map<String, Object> alert) {

        log.warn("ALERT RECEIVED: {}", alert);
        // Could send to Slack, email, etc.
        return ResponseEntity.ok().build();
    }

    private String normalizePath(String url) {
        if (url == null) return "unknown";
        return url.replaceAll("https?://[^/]+", "")
                .replaceAll("/[a-f0-9-]{36}", "/{id}")
                .replaceAll("\\?.*", "");
    }
}