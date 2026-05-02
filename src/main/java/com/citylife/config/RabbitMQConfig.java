package com.citylife.config;

import com.citylife.utils.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange seckillOrderExchange() {
        return new DirectExchange(RabbitMQConstants.SECKILL_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(RabbitMQConstants.SECKILL_ORDER_QUEUE)
                .deadLetterExchange(RabbitMQConstants.SECKILL_ORDER_DEAD_EXCHANGE)
                .deadLetterRoutingKey(RabbitMQConstants.SECKILL_ORDER_DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding seckillOrderBinding(Queue seckillOrderQueue, DirectExchange seckillOrderExchange) {
        return BindingBuilder.bind(seckillOrderQueue)
                .to(seckillOrderExchange)
                .with(RabbitMQConstants.SECKILL_ORDER_ROUTING_KEY);
    }

    @Bean
    public DirectExchange seckillOrderDeadExchange() {
        return new DirectExchange(RabbitMQConstants.SECKILL_ORDER_DEAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillOrderDeadQueue() {
        return QueueBuilder.durable(RabbitMQConstants.SECKILL_ORDER_DEAD_QUEUE).build();
    }

    @Bean
    public Binding seckillOrderDeadBinding(Queue seckillOrderDeadQueue, DirectExchange seckillOrderDeadExchange) {
        return BindingBuilder.bind(seckillOrderDeadQueue)
                .to(seckillOrderDeadExchange)
                .with(RabbitMQConstants.SECKILL_ORDER_DEAD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory manualAckRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        return factory;
    }
}
