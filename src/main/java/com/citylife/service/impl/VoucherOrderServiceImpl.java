package com.citylife.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.citylife.dto.OrderStatusDTO;
import com.citylife.dto.Result;
import com.citylife.dto.VoucherOrderMessage;
import com.citylife.entity.VoucherOrder;
import com.citylife.enums.VoucherOrderCreateResult;
import com.citylife.enums.VoucherOrderStatus;
import com.citylife.mapper.VoucherOrderMapper;
import com.citylife.mq.SeckillOrderMessagePublisher;
import com.citylife.service.ISeckillVoucherService;
import com.citylife.service.IVoucherOrderService;
import com.citylife.service.VoucherOrderCompensationService;
import com.citylife.utils.RedisIdWorker;
import com.citylife.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;

import static com.citylife.utils.RedisConstants.ORDER_LOCK_KEY;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SeckillOrderMessagePublisher seckillOrderMessagePublisher;

    @Resource
    private VoucherOrderCompensationService compensationService;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result<Long> seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");

        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );
        if (result == null) {
            return Result.fail("Failed to execute seckill check");
        }

        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "Stock not enough" : "Duplicate order is not allowed");
        }

        VoucherOrderMessage orderMessage = new VoucherOrderMessage();
        orderMessage.setId(orderId);
        orderMessage.setUserId(userId);
        orderMessage.setVoucherId(voucherId);

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setStatus(VoucherOrderStatus.PROCESSING.getValue());
        if (!saveProcessingOrder(voucherOrder)) {
            compensationService.rollbackOnly(orderId, userId, voucherId, "Failed to persist processing order");
            return Result.fail("Failed to create order");
        }

        try {
            seckillOrderMessagePublisher.send(orderMessage);
        } catch (RuntimeException e) {
            compensationService.failAndRollback(orderId, userId, voucherId, "MQ publish exception");
            throw e;
        }
        log.info("sent seckill order message, orderId: {}, voucherId: {}, userId: {}", orderId, voucherId, userId);

        return Result.ok(orderId);
    }

    @Override
    public Result<?> queryOrderStatus(Long orderId) {
        Long userId = UserHolder.getUser().getId();
        VoucherOrder voucherOrder = getById(orderId);
        if (voucherOrder != null) {
            if (!userId.equals(voucherOrder.getUserId())) {
                return Result.fail("Forbidden");
            }
            if (VoucherOrderStatus.PROCESSING.getValue() == voucherOrder.getStatus()
                    || VoucherOrderStatus.FAILED.getValue() == voucherOrder.getStatus()) {
                OrderStatusDTO status = new OrderStatusDTO();
                status.setOrderId(orderId);
                status.setStatus(VoucherOrderStatus.describe(voucherOrder.getStatus()));
                return Result.ok(status);
            }
            return Result.ok(voucherOrder);
        }
        OrderStatusDTO status = new OrderStatusDTO();
        status.setOrderId(orderId);
        status.setStatus("PROCESSING");
        return Result.ok(status);
    }

    @Override
    public boolean saveProcessingOrder(VoucherOrder voucherOrder) {
        try {
            voucherOrder.setStatus(VoucherOrderStatus.PROCESSING.getValue());
            return save(voucherOrder);
        } catch (DuplicateKeyException e) {
            log.warn("processing voucher order already exists, orderId: {}, userId: {}, voucherId: {}",
                    voucherOrder.getId(), voucherOrder.getUserId(), voucherOrder.getVoucherId());
            return false;
        }
    }

    @Override
    @Transactional
    public VoucherOrderCreateResult createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        RLock redisLock = redissonClient.getLock(ORDER_LOCK_KEY + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.warn("duplicate voucher order is processing, userId: {}, voucherId: {}", userId, voucherId);
            return VoucherOrderCreateResult.DUPLICATE;
        }

        try {
            VoucherOrder existingOrder = getById(voucherOrder.getId());
            if (existingOrder == null) {
                log.error("processing voucher order not found, orderId: {}", voucherOrder.getId());
                return VoucherOrderCreateResult.ORDER_NOT_FOUND;
            }
            if (!userId.equals(existingOrder.getUserId()) || !voucherId.equals(existingOrder.getVoucherId())) {
                log.error("voucher order message does not match processing order, orderId: {}", voucherOrder.getId());
                return VoucherOrderCreateResult.ORDER_NOT_FOUND;
            }
            if (VoucherOrderStatus.FAILED.getValue() == existingOrder.getStatus()) {
                log.warn("voucher order is already failed, orderId: {}", voucherOrder.getId());
                return VoucherOrderCreateResult.ORDER_ALREADY_FAILED;
            }
            if (VoucherOrderStatus.SUCCESS.getValue() == existingOrder.getStatus()) {
                log.warn("duplicate voucher order message, orderId: {}", voucherOrder.getId());
                return VoucherOrderCreateResult.SUCCESS;
            }

            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    .gt("stock", 0)
                    .update();
            if (!success) {
                log.error("seckill voucher stock not enough, voucherId: {}", voucherId);
                return VoucherOrderCreateResult.STOCK_NOT_ENOUGH;
            }

            boolean orderUpdated = update()
                    .set("status", VoucherOrderStatus.SUCCESS.getValue())
                    .set("fail_reason", null)
                    .eq("id", voucherOrder.getId())
                    .eq("status", VoucherOrderStatus.PROCESSING.getValue())
                    .update();
            if (!orderUpdated) {
                log.warn("voucher order status was changed before creation, orderId: {}", voucherOrder.getId());
                return VoucherOrderCreateResult.ORDER_ALREADY_FAILED;
            }
            log.info("created voucher order, orderId: {}, voucherId: {}, userId: {}",
                    voucherOrder.getId(), voucherId, userId);
            return VoucherOrderCreateResult.SUCCESS;
        } finally {
            redisLock.unlock();
        }
    }
}
