package com.citylife.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key();

    long rate() default 10;

    long rateInterval() default 1;

    TimeUnit rateIntervalUnit() default TimeUnit.SECONDS;

    boolean perUser() default true;

    String message() default "Too many requests, please try later";
}
