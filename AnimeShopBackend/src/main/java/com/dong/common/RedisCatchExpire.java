package com.dong.common;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RedisCatchExpire {

    private static final Long NULL_TTL = 5L;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存数据，设置逻辑过期时间
     */
    public void catchExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisExpireData redisExpireData = new RedisExpireData();
        // 关键改动：将 value 转为 JSON 字符串存储
        redisExpireData.setRedisdata(JSONUtil.toJsonStr(value));
        redisExpireData.setExpireTime(LocalDateTime.now()
                .plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(redisExpireData));
    }

    /**
     * 查询缓存（逻辑过期）
     *
     * @param key                缓存key
     * @param id                 数据标识
     * @param type               返回类型
     * @param dbFunction         数据库查询方法
     * @param mqProducerFunction 发送MQ方法
     * @param time               过期时间
     * @param timeUnit           时间单位
     * @return 数据
     */
    public <R, ID> R catchExpire(String key,
                                 ID id,
                                 Class<R> type,
                                 Function<ID, R> dbFunction,
                                 Consumer<ID> mqProducerFunction,
                                 Long time, TimeUnit timeUnit) {

        // 1. 查缓存
        String data = stringRedisTemplate.opsForValue().get(key);

        // 2. 缓存命中
        if (data != null) {
            // 2.1 空缓存命中，返回
            if (data.isEmpty()) {
                return null;
            }

            // 2.2 反序列化外层数据
            RedisExpireData redisExpireData = JSONUtil.toBean(data, RedisExpireData.class);
            Object rawData = redisExpireData.getRedisdata();

            // 2.3 反序列化内层数据（支持对象、数组、字符串等所有类型）
            R r = parseToTarget(rawData, type);

            // 2.4 未过期，直接返回
            if (redisExpireData.getExpireTime().isAfter(LocalDateTime.now())) {
                return r;
            }

            // 2.5 逻辑过期，异步刷新缓存
            mqProducerFunction.accept(id);
            return r;
        }

        // 3. 缓存未命中，查询数据库
        R newData = dbFunction.apply(id);

        // 4. 数据库无数据，缓存空值
        if (newData == null) {
            stringRedisTemplate.opsForValue().set(key, "", time, timeUnit);
            return null;
        }

        // 5. 数据库有数据，写入缓存
        catchExpire(key, newData, time, timeUnit);
        return newData;
    }

    /**
     * 将任意类型数据转换为目标类型
     *
     * @param rawData 原始数据
     * @param type    目标类型
     * @param <R>     泛型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    private <R> R parseToTarget(Object rawData, Class<R> type) {
        // 已经是目标类型，直接返回
        if (type.isInstance(rawData)) {
            return (R) rawData;
        }

        // JSONObject 类型
        if (rawData instanceof JSONObject) {
            return JSONUtil.toBean((JSONObject) rawData, type);
        }

        // JSONArray 类型
        if (rawData instanceof JSONArray) {
            String jsonStr = JSONUtil.toJsonStr(rawData);
            return JSONUtil.toBean(jsonStr, type);
        }

        // 其他类型（字符串、数字等），统一转 JSON 字符串再解析
        String jsonStr = JSONUtil.toJsonStr(rawData);
        return JSONUtil.toBean(jsonStr, type);
    }
}