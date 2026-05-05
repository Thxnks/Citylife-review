package com.citylife.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.citylife.entity.VoucherOrder;
import com.citylife.enums.VoucherOrderStatus;
import com.citylife.mapper.VoucherOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.citylife.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.citylife.utils.RedisConstants.SECKILL_STOCK_KEY;

@Slf4j
@Component
public class VoucherOrderCompensationService {

    private static final int COMPENSATION_BATCH_SIZE = 100;
    private static final int PROCESSING_TIMEOUT_MINUTES = 5;

    @Resource
    private VoucherOrderMapper voucherOrderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
