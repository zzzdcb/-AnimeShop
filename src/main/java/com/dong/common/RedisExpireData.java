package com.dong.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisExpireData {

    private Object redisdata; // 缓存数据
    private LocalDateTime expireTime; // 过期时间
}
