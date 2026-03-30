package com.farmmarket.monitoring.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    // ── Pointcuts ─────────────────────────────

    @Pointcut("within(com.farmmarket.service..*)")
    public void serviceLayer() {}

    @Pointcut("within(com.farmmarket.repository..*)")
    public void repositoryLayer() {}

    @Pointcut("@annotation(com.farmmarket.monitoring.annotation.Monitored)")
    public void monitoredMethod() {}

    // ── Service Layer Timing ──────────────────

    @Around("serviceLayer()")
    public Object measureServiceMethod(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            sample.stop(Timer.builder("farm.service.duration")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("outcome", "SUCCESS")
                    .publishPercentiles(0.5, 0.90, 0.95)
                    .register(meterRegistry));

            return result;
        } catch (Throwable e) {
            sample.stop(Timer.builder("farm.service.duration")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("outcome", "ERROR")
                    .register(meterRegistry));

            meterRegistry.counter("farm.service.errors",
                    "class", className,
                    "method", methodName,
                    "exception", e.getClass().getSimpleName()
            ).increment();

            log.error("Service error: {}.{} — {}",
                    className, methodName, e.getMessage());

            throw e;
        }
    }

    // ── Repository Layer Timing ───────────────

    @Around("repositoryLayer()")
    public Object measureRepoMethod(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            sample.stop(Timer.builder("farm.repository.duration")
                    .tag("repository", className)
                    .tag("method", methodName)
                    .tag("outcome", "SUCCESS")
                    .register(meterRegistry));

            return result;
        } catch (Throwable e) {
            sample.stop(Timer.builder("farm.repository.duration")
                    .tag("repository", className)
                    .tag("method", methodName)
                    .tag("outcome", "ERROR")
                    .register(meterRegistry));

            throw e;
        }
    }
}