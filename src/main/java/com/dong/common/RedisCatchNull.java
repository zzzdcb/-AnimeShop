package com.dong.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RedisCatchNull {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存空对象，防止缓存穿透，最基本缓存
     */
    public void catchNull(String key, Object  value,Long time, TimeUnit timeUnit){
        stringRedisTemplate.opsForValue()
                .setIfAbsent(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    public <ID,R> R catchNull(String key,
                              Class<R> type,
                              ID id,
                              Function<ID,R> dbFunction,
                              Long time, TimeUnit timeUnit){
        // 1. 从Redis中获取数据
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 有数据，返回
        if (StrUtil.isNotBlank( json)){
            return JSONUtil.toBean(json, type);
        }
        // 3. 空值，返回（第一次过滤已经返回正常有数据了，剩下的要么为空，要么不存在）
        if (json != null){
            return null;
        }
        // 根据id查数据库
        R r = dbFunction.apply(id);
        // 4. 空值直接写入Redis
        if (r == null){
            stringRedisTemplate.opsForValue().set(key, "", time, timeUnit);
        }
        // 5. 数据库数据写入Redis
        catchNull(key, r, time, timeUnit);
        // 6. 返回查到的数据
        return r;
    }

}
