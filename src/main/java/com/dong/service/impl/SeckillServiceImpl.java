package com.dong.service.impl;

import com.dong.common.RabbitMQProducer;
import com.dong.common.RedisCatchExpire;
import com.dong.common.RedisExpireData;
import com.dong.common.Result;
import com.dong.entity.Seckill;
import com.dong.mapper.SeckillMapper;
import com.dong.producer.CacheRefreshMessage;
import com.dong.producer.seckill.SeckillMessage;
import com.dong.service.ISeckillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.RedisConstants;
import com.dong.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 秒杀活动表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements ISeckillService {

    private final RabbitMQProducer rabbitMQProducer;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCatchExpire redisCatchExpire;

    // 初始化Lua脚本
    private static final DefaultRedisScript<List> LUA_SCRIPT;

    static {
        LUA_SCRIPT = new DefaultRedisScript<>();
        LUA_SCRIPT.setLocation(new ClassPathResource("stock.lua"));
        LUA_SCRIPT.setResultType(List.class);
    }

    /**
     * 秒杀下单
     *
     * @param seckillId 秒杀商品id
     * @param quantity  秒杀商品数量
     */
    @Override
    public Result<String> placeOrder(Long seckillId, Integer quantity) {
        // 1. 获取用户id
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 获取商品id，从缓存获取，使用工具类防止缓存击穿
        String seckillKey = RedisConstants.SECKILL_ORDER_KEY + seckillId;
        Seckill seckill = redisCatchExpire.catchExpire(
                seckillKey,
                seckillId,
                Seckill.class,
                this::getById,
                // 这里id就是一个占位符，内部还是用seckillId进行操作
                id -> refreshCache(id, seckillKey),
                RedisConstants.SECKILL_ORDER_KEY_TTL,
                TimeUnit.SECONDS
        );
        if (seckill == null) {
            return Result.error("秒杀活动不存在");
        }
        // 3. 时间判断
        if (LocalDateTime.now().isBefore(seckill.getStartTime())) {
            return Result.error("秒杀活动未开始");
        }
        if (LocalDateTime.now().isAfter(seckill.getEndTime())) {
            return Result.error("秒杀活动已结束");
        }
        Long productId = seckill.getProductId();
        // 4. 构建Keys和ARGV
        String key1 = RedisConstants.SECKILL_STOCK_KEY + seckillId;
        String key2 = RedisConstants.SECKILL_USER_KEY + seckillId;
        List<String> keys = List.of(key1, key2);
        List<String> argv = List.of(userId.toString(), quantity.toString());
        // 5. 执行Lua脚本并判断结果
        List seckillResult = stringRedisTemplate.execute(
                LUA_SCRIPT,
                keys,
                argv
        );
        Long code = (Long) seckillResult.get(0);
        String message = (String) seckillResult.get(1);
        if (code != 0) {
            return Result.error(message);
        }
        // 6. 存入消息体
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setUserId(userId);
        seckillMessage.setProductId(productId);
        seckillMessage.setQuantity(quantity);
        // 7. 发送消息
        String businessId =
                "seckill:" + userId + ":" + seckillId + System.currentTimeMillis();
        rabbitMQProducer.send(RabbitMQConstants.SECKILL_EXCHANGE,
                RabbitMQConstants.SECKILL_ROUTING,
                seckillMessage,
                businessId);
        return Result.success("抢购成功");
    }

    /**
     * 发送到消息队列异步重建缓存
     *
     * @param seckillId 秒杀商品id
     * @param seckillKey 秒杀商品key
     */
    private void refreshCache(Long seckillId, String seckillKey) {
        CacheRefreshMessage cacheRefreshMessage = new CacheRefreshMessage();
        cacheRefreshMessage.setKey(seckillKey);
        cacheRefreshMessage.setId(seckillId);
        cacheRefreshMessage.setTime(RedisConstants.SECKILL_ORDER_KEY_TTL);
        cacheRefreshMessage.setUnit("MINUTES");
        String businessId = "seckill:expire" + ":" + seckillId + System.currentTimeMillis();
        rabbitMQProducer.send(RabbitMQConstants.EXPIRE_EXCHANGE,
                RabbitMQConstants.EXPIRE_ROUTING,
                cacheRefreshMessage,
                businessId);
    }
}
