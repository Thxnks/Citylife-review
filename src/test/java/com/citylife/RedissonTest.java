package com.citylife;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
@Disabled("Manual Redisson integration test. Requires a running Redis instance.")
class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    private RLock lock;

    @BeforeEach
    void setUp() {
        lock = redissonClient.getLock("order");
    }

    @Test
    void method1() throws InterruptedException {
        boolean locked = lock.tryLock(1L, TimeUnit.SECONDS);
        if (!locked) {
            log.error("failed to acquire lock in method1");
            return;
        }
        try {
            log.info("acquired lock in method1");
            method2();
            log.info("running business logic in method1");
        } finally {
            log.warn("releasing lock in method1");
            lock.unlock();
        }
    }

    void method2() {
        boolean locked = lock.tryLock();
        if (!locked) {
            log.error("failed to acquire lock in method2");
            return;
        }
        try {
            log.info("acquired lock in method2");
            log.info("running business logic in method2");
        } finally {
            log.warn("releasing lock in method2");
            lock.unlock();
        }
    }
}
