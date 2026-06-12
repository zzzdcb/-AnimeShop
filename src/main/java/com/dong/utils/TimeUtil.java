package com.dong.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TimeUtil {

    // 普通订单过期时间（分钟）
    @Value("${order.expire.minutes:30}")
    private int normalExpireMinutes;

    // 秒杀订单过期时间（分钟）
    @Value("${seckill.order.expire.minutes:10}")
    private int seckillExpireMinutes;

    /**
     * 获取普通订单过期时间
     */
    public LocalDateTime getNormalOrderExpireTime() {
        return LocalDateTime.now().plusMinutes(normalExpireMinutes);
    }

    /**
     * 获取秒杀订单过期时间
     */
    public LocalDateTime getSeckillOrderExpireTime() {
        return LocalDateTime.now().plusMinutes(seckillExpireMinutes);
    }

    /**
     * 判断是否过期
     */
    public boolean isExpired(LocalDateTime expireTime) {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取剩余秒数（倒计时用）
     */
    public long getRemainingSeconds(LocalDateTime expireTime) {
        return Duration.between(LocalDateTime.now(), expireTime).getSeconds();
    }
}