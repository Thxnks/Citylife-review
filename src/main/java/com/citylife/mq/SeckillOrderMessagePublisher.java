package com.citylife.mq;

import com.citylife.dto.VoucherOrderMessage;
import com.citylife.service.VoucherOrderCompensationService;
import com.citylife.utils.RabbitMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SeckillOrderMessagePublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private VoucherOrderCompensationService compensationService;

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

        rabbitTemplate.setReturnsCallback(returned -> {
            String correlationId = returned.getMessage().getMessageProperties().getCorrelationId();
            if (correlationId == null) {
                log.error("seckill order message returned without correlation id, exchange: {}, routingKey: {}",
                        returned.getExchange(), returned.getRoutingKey());
                return;
            }
            log.error("seckill order message returned, correlationId: {}, replyCode: {}, replyText: {}",
                    correlationId, returned.getReplyCode(), returned.getReplyText());
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
        compensationService.failAndRollback(
                orderMessage.getId(),
                orderMessage.getUserId(),
                orderMessage.getVoucherId(),
                "MQ publish failed"
        );
    }
}
