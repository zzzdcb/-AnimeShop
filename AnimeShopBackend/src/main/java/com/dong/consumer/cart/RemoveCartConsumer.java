package com.dong.consumer.cart;

import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQConsumer;
import com.dong.entity.Cart;
import com.dong.producer.cart.RemoveCartMessage;
import com.dong.service.ICartService;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.RedisConstants;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RemoveCartConsumer {

    private final RabbitMQConsumer rabbitMQConsumer;
    private final ICartService cartService;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConstants.REMOVE_CART_QUEUE)
    public void removeCart(@Payload String removeCartMsg,
                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                           @Header("businessId") String businessId,
                           Channel  channel){
        rabbitMQConsumer.consumeWithRedisIdempotent(
                businessId, removeCartMsg, deliveryTag, channel,
                ()-> handleRemove(removeCartMsg)
                );
    }

    private void handleRemove(String message){
        RemoveCartMessage msg = JSONUtil.toBean(message, RemoveCartMessage.class);
        String explain = msg.getExplain();

        if ("removeCart".equals(explain) || "removeAll".equals(explain)) {
            removeCart(message);
        } else {
            log.error("未知操作类型: {}", explain);
        }
    }

    private void removeCart( String removeCartMsg){
        // 1. 解析消息
        RemoveCartMessage removeCartMessage = JSONUtil.toBean(removeCartMsg, RemoveCartMessage.class);
        List<Long> productIdList = removeCartMessage.getProductIdList();
        Long userId = removeCartMessage.getUserId();

        // 2. 数据库删除
        boolean remove = cartService.lambdaUpdate()
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId, productIdList)
                .remove();
        // 3. 数据库删除失败，直接返回
        if (!remove) {
            log.error("删除购物车失败");
            return;
        }
        // 4. 同步删除Redis缓存
        String cartKey = RedisConstants.CART_KEY + userId;
        // 4.1 转成String数组批量删除,Hash结构必须数组才能批量删除
        Object[] fiedls = productIdList.stream().map(String::valueOf).toArray();
        stringRedisTemplate.opsForHash().delete(cartKey, fiedls);
        log.info("删除购物车成功, userId={}, productIds={}", userId, productIdList);
    }
}
