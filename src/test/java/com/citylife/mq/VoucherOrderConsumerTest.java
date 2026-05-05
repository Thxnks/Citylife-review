package com.citylife.mq;

import com.citylife.dto.VoucherOrderMessage;
import com.citylife.enums.VoucherOrderCreateResult;
import com.citylife.service.IVoucherOrderService;
import com.citylife.service.VoucherOrderCompensationService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherOrderConsumerTest {

    @Mock
    private IVoucherOrderService voucherOrderService;

    @Mock
    private VoucherOrderCompensationService compensationService;

    @Mock
    private Channel channel;

    private VoucherOrderConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VoucherOrderConsumer();
        ReflectionTestUtils.setField(consumer, "voucherOrderService", voucherOrderService);
        ReflectionTestUtils.setField(consumer, "compensationService", compensationService);
    }

    @Test
    void shouldFailAndRejectMessageWhenMysqlStockIsNotEnough() throws Exception {
        VoucherOrderMessage orderMessage = new VoucherOrderMessage();
        orderMessage.setId(1001L);
        orderMessage.setUserId(10L);
        orderMessage.setVoucherId(20L);

        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(88L);
        Message message = new Message(new byte[0], properties);

        when(voucherOrderService.createVoucherOrder(any())).thenReturn(VoucherOrderCreateResult.STOCK_NOT_ENOUGH);

        consumer.handleVoucherOrder(orderMessage, channel, message);

        verify(compensationService).failAndRollback(1001L, 10L, 20L, "MySQL stock not enough");
        verify(channel).basicReject(88L, false);
    }
}
