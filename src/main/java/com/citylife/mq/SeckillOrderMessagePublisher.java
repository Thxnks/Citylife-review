package com.citylife.mq;

import com.citylife.dto.VoucherOrderMessage;
import com.citylife.utils.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.citylife.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.citylife.utils.RedisConstants.SECKILL_STOCK_KEY;

@Slf4j
@Component
public class SeckillOrderMessagePublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Map<String, VoucherOrderMessage> pendingMessages = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCallbacks() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData == null || correlationData.getId() == null) {
                return;
            }
            String correlationId = correlationData.getId();
            if (ack) {
                pendingMessages.remove(correlationId);
                return;
            }
            log.error("seckill order message not confirmed, correlationId: {}, cause: {}", correlationId, cause);
            rollbackSeckillQualification(correlationId);
        });

        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            String correlationId = message.getMessageProperties().getCorrelationId();
            if (correlationId == null) {
                log.error("seckill order message returned without correlation id, exchange: {}, routingKey: {}",
                        exchange, routingKey);
                return;
            }
            log.error("seckill order message returned, correlationId: {}, replyCode: {}, replyText: {}",
                    correlationId, replyCode, replyText);
            rollbackSeckillQualification(correlationId);
        });
    }

    public void send(VoucherOrderMessage orderMessage) {
        String correlationId = orderMessage.getId().toString();
        pendingMessages.put(correlationId, orderMessage);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.SECKILL_ORDER_EXCHANGE,
                    RabbitMQConstants.SECKILL_ORDER_ROUTING_KEY,
                    orderMessage,
                    message -> {
                        message.getMessageProperties().setCorrelationId(correlationId);
                        return message;
                    },
                    new CorrelationData(correlationId)
            );
        } catch (RuntimeException e) {
            rollbackSeckillQualification(correlationId);
            throw e;
        }
    }

    private void rollbackSeckillQualification(String correlationId) {
        VoucherOrderMessage orderMessage = pendingMessages.remove(correlationId);
        if (orderMessage == null) {
            return;
        }
        String voucherId = orderMessage.getVoucherId().toString();
        String userId = orderMessage.getUserId().toString();
        stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY + voucherId);
        stringRedisTemplate.opsForSet().remove(SECKILL_ORDER_KEY + voucherId, userId);
        log.warn("rolled back seckill redis state, orderId: {}, voucherId: {}, userId: {}",
                orderMessage.getId(), voucherId, userId);
    }
}
