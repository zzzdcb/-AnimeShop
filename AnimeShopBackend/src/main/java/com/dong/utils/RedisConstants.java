package com.dong.utils;

public class RedisConstants {

    // 登录相关
    public static final String LOGIN_USER_KEY = "login:";
    public static final Long LOGIN_USER_TTL = 2L;

    // 购物车相关
    public static final String CART_KEY = "cart:";
    public static final Integer CART_USER_TTL = 7;

    //秒杀相关
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_USER_KEY = "seckill:userPay:";

    // 秒杀订单相关
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final Long SECKILL_ORDER_KEY_TTL = 5L;

    // 首页
    public static final String HOME_HOT_PRODUCT_KEY = "home:hot:product";
    public static final String HOME_NEW_PRODUCT_KEY = "home:new:product";
    public static final String HOME_SECKILL_PRODUCT_KEY = "home:seckill:product";
}
