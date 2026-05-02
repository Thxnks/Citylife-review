package com.citylife.mq;

import com.citylife.dto.VoucherOrderMessage;
import com.citylife.entity.VoucherOrder;
import com.citylife.enums.VoucherOrderCreateResult;
import com.citylife.service.IVoucherOrderService;
import com.citylife.utils.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class VoucherOrderConsumer {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @RabbitListener(
            queues = RabbitMQConstants.SECKILL_ORDER_QUEUE,
            containerFactory = "manualAckRabbitListenerContainerFactory"
    )
    public void handleVoucherOrder(VoucherOrderMessage orderMessage, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            VoucherOrder voucherOrder = new VoucherOrder();
            voucherOrder.setId(orderMessage.getId());
            voucherOrder.setUserId(orderMessage.getUserId());
            voucherOrder.setVoucherId(orderMessage.getVoucherId());

            VoucherOrderCreateResult result = voucherOrderService.createVoucherOrder(voucherOrder);
            if (result == VoucherOrderCreateResult.STOCK_NOT_ENOUGH) {
                channel.basicReject(deliveryTag, false);
                return;
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("create voucher order failed, message: {}", orderMessage, e);
            channel.basicReject(deliveryTag, false);
        }
    }
}
