package com.citylife.mq;

import com.citylife.dto.VoucherOrderMessage;
import com.citylife.service.VoucherOrderCompensationService;
import com.citylife.utils.RabbitMQConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeckillOrderMessagePublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private VoucherOrderCompensationService compensationService;

    private SeckillOrderMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SeckillOrderMessagePublisher();
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "compensationService", compensationService);
    }

    @Test
    void shouldRollbackOrderWhenRabbitPublishThrowsException() {
        VoucherOrderMessage message = new VoucherOrderMessage();
        message.setId(1001L);
        message.setUserId(10L);
        message.setVoucherId(20L);

        doThrow(new AmqpException("broker unavailable")).when(rabbitTemplate).convertAndSend(
                eq(RabbitMQConstants.SECKILL_ORDER_EXCHANGE),
                eq(RabbitMQConstants.SECKILL_ORDER_ROUTING_KEY),
                same(message),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        try {
            publisher.send(message);
        } catch (AmqpException ignored) {
            // Expected: send failures are rethrown after compensation.
        }

        verify(compensationService).failAndRollback(1001L, 10L, 20L, "MQ publish failed");
    }
}
