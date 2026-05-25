package com.citylife.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.citylife.entity.VoucherOrder;
import com.citylife.enums.VoucherOrderStatus;
import com.citylife.mapper.VoucherOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.citylife.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.citylife.utils.RedisConstants.SECKILL_STOCK_KEY;

@Slf4j
@Component
public class VoucherOrderCompensationService {

    private static final int COMPENSATION_BATCH_SIZE = 100;
    private static final int PROCESSING_TIMEOUT_MINUTES = 5;
    private static final String COMPENSATION_LOCK_KEY = "lock:compensation:stale_orders";
    private static final long COMPENSATION_LOCK_WAIT_SECONDS = 1;
    private static final long COMPENSATION_LOCK_LEASE_SECONDS = 30;

    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    public boolean failAndRollback(Long orderId, Long userId, Long voucherId, String reason) {
        UpdateWrapper<VoucherOrder> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", orderId)
                .eq("status", VoucherOrderStatus.PROCESSING.getValue())
                .set("status", VoucherOrderStatus.FAILED.getValue())
                .set("fail_reason", reason);
        int updated = voucherOrderMapper.update(null, wrapper);
        if (updated == 0) {
            return false;
        }

        rollbackSeckillQualification(orderId, userId, voucherId, reason);
        return true;
    }

    public void rollbackOnly(Long orderId, Long userId, Long voucherId, String reason) {
        rollbackSeckillQualification(orderId, userId, voucherId, reason);
    }

    @Scheduled(fixedDelayString = "${citylife.seckill.compensation-delay-ms:60000}")
    public void compensateStaleProcessingOrders() {
        RLock lock = redissonClient.getLock(COMPENSATION_LOCK_KEY);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(COMPENSATION_LOCK_WAIT_SECONDS, COMPENSATION_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (!acquired) {
            log.debug("another instance is running compensation, skip");
            return;
        }

        try {
            LocalDateTime timeoutBefore = LocalDateTime.now().minusMinutes(PROCESSING_TIMEOUT_MINUTES);
            QueryWrapper<VoucherOrder> wrapper = new QueryWrapper<>();
            wrapper.eq("status", VoucherOrderStatus.PROCESSING.getValue())
                    .lt("update_time", timeoutBefore)
                    .last("LIMIT " + COMPENSATION_BATCH_SIZE);
            List<VoucherOrder> orders = voucherOrderMapper.selectList(wrapper);
            for (VoucherOrder order : orders) {
                boolean compensated = failAndRollback(
                        order.getId(),
                        order.getUserId(),
                        order.getVoucherId(),
                        "PROCESSING timeout"
                );
                if (compensated) {
                    log.warn("compensated stale processing voucher order, orderId: {}", order.getId());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void rollbackSeckillQualification(Long orderId, Long userId, Long voucherId, String reason) {
        String voucherIdValue = voucherId.toString();
        String userIdValue = userId.toString();
        stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY + voucherIdValue);
        stringRedisTemplate.opsForSet().remove(SECKILL_ORDER_KEY + voucherIdValue, userIdValue);
        log.warn("rolled back seckill redis state, orderId: {}, voucherId: {}, userId: {}, reason: {}",
                orderId, voucherId, userId, reason);
    }
}
