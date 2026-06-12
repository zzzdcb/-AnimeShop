package com.dong.utils;

public class RabbitMQConstants {

    // 购物车添加/更新
    public static final String CART_EXCHANGE = "cart.exchange";
    public static final String CART_QUEUE = "cart.queue";
    public static final String CART_ROUTING = "cart.routing";

    // 购物车删除商品
    public static final String REMOVE_CART_QUEUE = "remove.cart.queue";
    public static final String REMOVE_CART_ROUTING = "remove.cart.routing";

    //解决逻辑过期
    public static final String EXPIRE_EXCHANGE = "expire.exchange";
    public static final String EXPIRE_QUEUE = "expire.queue";
    public static final String EXPIRE_ROUTING = "expire.routing";

    // 地址
    public static final String ADDRESS_EXCHANGE = "address.exchange";
    public static final String ADDRESS_QUEUE = "address.queue";
    public static final String ADDRESS_ROUTING = "address.routing";
    public static final String REMOVE_ADDRESS_QUEUE = "remove.address.queue";
    public static final String REMOVE_ADDRESS_ROUTING = "remove.address.routing";

    // 秒杀
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_ROUTING = "seckill.routing";

    // 首页数据
    public static final String HOME_EXCHANGE = "home.exchange";
    public static final String HOME_QUEUE = "home.queue";
    public static final String HOME_ROUTING = "home.routing";

}
