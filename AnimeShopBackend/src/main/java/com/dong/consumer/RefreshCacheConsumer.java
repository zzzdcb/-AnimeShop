package com.dong.consumer;

import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQConsumer;
import com.dong.common.RedisCatchExpire;
import com.dong.entity.Seckill;
import com.dong.producer.CacheRefreshMessage;
import com.dong.service.ISeckillService;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.TimeUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshCacheConsumer {

    private final RabbitMQConsumer rabbitMQConsumer;
    private final StringRedisTemplate stringRedisTemplate;
    private final ISeckillService seckillService;
    private final RedisCatchExpire redisCatchExpire;
    private final TimeUtil timeUtil;

    @RabbitListener(queues = RabbitMQConstants.EXPIRE_QUEUE)
    public void refreshCache(@Payload String  message,
                             @Header("businessId") String businessId,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                             Channel  channel) {
        rabbitMQConsumer.consumeWithRedisIdempotent(
                businessId,
                message,
                deliveryTag,
                channel,
                () -> refreshCacheByExpire(message)
        );
    }

    private void refreshCacheByExpire(String message) {
        // 1. 解析消息
        CacheRefreshMessage msg = JSONUtil.toBean(message, CacheRefreshMessage.class);
        // 2. 校验数据库
        Seckill seckillById = seckillService.getById(msg.getId());
        if (seckillById == null) {
            // 3. 删除缓存
            stringRedisTemplate.delete(msg.getKey());
            return;
        }
        // 4. 缓存重建
        TimeUnit timeUnit = TimeUnit.valueOf(msg.getUnit());
        redisCatchExpire.catchExpire(msg.getKey(), seckillById,msg.getTime(), timeUnit);
    }
}
