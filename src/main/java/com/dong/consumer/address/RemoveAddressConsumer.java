package com.dong.consumer.address;

import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQConsumer;
import com.dong.entity.Address;
import com.dong.producer.address.RemoveAddressMessage;
import com.dong.service.IAddressService;
import com.dong.utils.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemoveAddressConsumer {

    private final RabbitMQConsumer rabbitMQConsumer;
    private final IAddressService addressService;

    @RabbitListener(queues = RabbitMQConstants.REMOVE_ADDRESS_QUEUE)
    public void consume(@Payload String message,
                       @Header("businessId") String businessId,
                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                       Channel channel) {
        rabbitMQConsumer.consumeWithRedisIdempotent(
                businessId, message, deliveryTag, channel,
                () ->removeAddress(message)
        );
    }
    private void removeAddress(String message) {
        // 1. 解析消息
        RemoveAddressMessage removeAddressMessage =
                JSONUtil.toBean(message, RemoveAddressMessage.class);
        Long userId = removeAddressMessage.getUserId();
        Long addressId = removeAddressMessage.getAddressId();
        // 2. 删除
        boolean remove = addressService.lambdaUpdate()
                .eq(Address::getUserId, userId)
                .eq(Address::getId, addressId)
                .remove();
        if (!remove) {
            log.error("删除地址失败, userId={}, addressId={}", userId, addressId);
            throw new RuntimeException("删除地址失败");
        }
    }
}
