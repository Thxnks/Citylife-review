package com.citylife.aspect;

import com.citylife.annotation.RateLimit;
import com.citylife.exception.RateLimitException;
import com.citylife.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Resource
    private RedissonClient redissonClient;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(rateLimit);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        rateLimiter.trySetRate(
                RateType.OVERALL,
                rateLimit.rate(),
                rateLimit.rateInterval(),
                toRateIntervalUnit(rateLimit.rateIntervalUnit())
        );

        if (!rateLimiter.tryAcquire()) {
            if (rateLimit.perUser()) {
                log.warn("rate limit exceeded for user, key: {}", key);
            } else {
                log.warn("global rate limit exceeded, key: {}", key);
            }
            throw new RateLimitException(rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private String buildKey(RateLimit rateLimit) {
        if (rateLimit.perUser()) {
            Long userId = UserHolder.getUser().getId();
            return RATE_LIMIT_PREFIX + rateLimit.key() + ":" + userId;
        }
        return RATE_LIMIT_PREFIX + rateLimit.key();
    }

    private RateIntervalUnit toRateIntervalUnit(java.util.concurrent.TimeUnit unit) {
        switch (unit) {
            case SECONDS:
                return RateIntervalUnit.SECONDS;
            case MINUTES:
                return RateIntervalUnit.MINUTES;
            case HOURS:
                return RateIntervalUnit.HOURS;
            case DAYS:
                return RateIntervalUnit.DAYS;
            default:
                return RateIntervalUnit.MILLISECONDS;
        }
    }
}
