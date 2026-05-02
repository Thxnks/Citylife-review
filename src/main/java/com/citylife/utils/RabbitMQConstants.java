package com.citylife.utils;

public class RabbitMQConstants {

    public static final String SECKILL_ORDER_EXCHANGE = "hm.dianping.seckill.order.exchange";
    public static final String SECKILL_ORDER_QUEUE = "hm.dianping.seckill.order.queue";
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order";

    public static final String SECKILL_ORDER_DEAD_EXCHANGE = "hm.dianping.seckill.order.dead.exchange";
    public static final String SECKILL_ORDER_DEAD_QUEUE = "hm.dianping.seckill.order.dead.queue";
    public static final String SECKILL_ORDER_DEAD_ROUTING_KEY = "seckill.order.dead";
}
