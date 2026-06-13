package com.dong.consumer.cart;

import com.alibaba.fastjson.JSON;
import com.dong.common.RabbitMQConsumer;
import com.dong.entity.Cart;
import com.dong.producer.cart.CartMessage;
import com.dong.service.ICartService;
import com.dong.utils.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.dong.utils.RedisConstants.CART_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartConsumer {

    private final RabbitMQConsumer rabbitMQConsumer;
    private final ICartService cartService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 监听购物车队列
     * @param cartMsg
     * @param deliveryTag
     * @param businessId
     * @param channel
     */
    @RabbitListener(queues = RabbitMQConstants.CART_QUEUE)
    public void process(@Payload String cartMsg,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        @Header("businessId") String businessId,
                        Channel channel) {

        rabbitMQConsumer.consumeWithRedisIdempotent(businessId, cartMsg, deliveryTag, channel,
                () -> {
                    // 把数据转为对象
                    CartMessage message = JSON.parseObject(cartMsg, CartMessage.class);
                    addOrUpdateCart(message);
                }
        );
    }

    /**
     * 新增或更新购物车
     */
    private void addOrUpdateCart(CartMessage message) {

        String cartKey = CART_KEY + message.getUserId();

        // 查询是否存在
        Cart existCart = cartService.lambdaQuery()
                .eq(Cart::getUserId, message.getUserId())
                .eq(Cart::getProductId, message.getProductId())
                .one();
        // 商品不存在，新增
        if (existCart == null) {
            Cart cart = new Cart();
            cart.setUserId(message.getUserId());
            cart.setProductId(message.getProductId());
            cart.setQuantity(message.getQuantity());
            cartService.save(cart);
            // 保存到Redis
            stringRedisTemplate.opsForHash().put(cartKey,
                    cart.getProductId().toString(),
                    cart.getQuantity().toString());
            log.info("新增成功, userId: {}, productId: {}", message.getUserId(), message.getProductId());
        } else {
            // 商品存在，更新数量（不重试，失败就抛异常）
            boolean success = cartService.lambdaUpdate()
                    .eq(Cart::getUserId, existCart.getUserId())
                    .eq(Cart::getProductId, existCart.getProductId())
                    .eq(Cart::getQuantity, existCart.getQuantity()) //乐观锁
                    .set(Cart::getQuantity, message.getQuantity())
                    .update();
            if (!success) {
                throw new RuntimeException("购物车更新失败");
            }
            //删除对应商品Redis缓存
            stringRedisTemplate.opsForHash()
                    .delete(cartKey, existCart.getProductId().toString());
            log.info("更新成功, cartId: {}, 增加: {}", existCart.getId(), message.getQuantity());
        }
    }
}