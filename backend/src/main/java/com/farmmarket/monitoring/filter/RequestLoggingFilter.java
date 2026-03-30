package com.farmmarket.monitoring.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip actuator endpoints
        if (request.getRequestURI().startsWith("/api/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate correlation ID
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put("correlationId", correlationId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("clientIp", getClientIp(request));

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            log.info("→ {} {} from {} | User-Agent: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    getClientIp(request),
                    request.getHeader("User-Agent"));

            // Add correlation ID to response
            response.setHeader("X-Correlation-ID", correlationId);

            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Log completed request
            log.info("← {} {} → {} | {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration);

            // Record custom HTTP metrics with path labels
            String path = normalizePath(request.getRequestURI());
            Timer.builder("farm.http.requests")
                    .tag("method", request.getMethod())
                    .tag("path", path)
                    .tag("status", String.valueOf(status))
                    .tag("outcome", status < 400 ? "SUCCESS" : "ERROR")
                    .publishPercentiles(0.5, 0.90, 0.95, 0.99)
                    .register(meterRegistry)
                    .record(java.time.Duration.ofMillis(duration));

            // Warn on slow requests
            if (duration > 2000) {
                log.warn("⚠ SLOW REQUEST: {} {} took {}ms (status={})",
                        request.getMethod(),
                        request.getRequestURI(),
                        duration,
                        status);
            }

            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Normalize paths to avoid high cardinality in metrics.
     * /api/products/abc-123 → /api/products/{id}
     */
    private String normalizePath(String uri) {
        return uri
                .replaceAll(
                        "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}" +
                                "-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{id}")
                .replaceAll("/[0-9]+", "/{id}")
                .replaceAll("/[a-z0-9-]{20,}", "/{slug}");
    }
}