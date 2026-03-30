package com.farmmarket.monitoring.annotation;

import java.lang.annotation.*;

/**
 * Mark methods for explicit monitoring.
 * Usage: @Monitored on any method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Monitored {
    String value() default "";
}