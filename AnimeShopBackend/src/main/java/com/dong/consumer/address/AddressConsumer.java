package com.dong.consumer.address;

import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQConsumer;
import com.dong.dto.AddressDTO;
import com.dong.entity.Address;
import com.dong.producer.address.AddressMessage;
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

@RequiredArgsConstructor
@Component
@Slf4j
public class AddressConsumer {

    private final IAddressService addressService;
    private final RabbitMQConsumer rabbitMQConsumer;

    @RabbitListener(queues = RabbitMQConstants.ADDRESS_QUEUE)
    public void consume(@Payload String message,
                        @Header("businessId") String businessId,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        Channel channel) {
        // 2. 执行业务
        rabbitMQConsumer.consumeWithRedisIdempotent(
                businessId, message, deliveryTag, channel,
                        () ->addAddress(message)
        );
    }

    private void addAddress(String message) {
        AddressMessage addressMessage = JSONUtil.toBean(message, AddressMessage.class);
        // 1. 获取参数
        Long userId = addressMessage.getUserId();
        AddressDTO addressDTO = addressMessage.getAddressDTO();
        if (userId == null || addressDTO == null){
            log.error("添加地址参数错误: userId={}, addressDTO={}", userId, addressDTO);
            return;
        }
        // 2. 拼接详细地址
        String detail =
                addressDTO.getProvince() + addressDTO.getCity() +
                        addressDTO.getDistrict() + addressDTO.getDetail();
        // 3. 构建Address对象
        Address address = new Address();
        address.setUserId(userId);
        address.setReceiver(addressDTO.getReceiver());
        address.setPhone(addressDTO.getPhone());
        address.setAddress(detail);
        address.setIsDefault(addressDTO.getIsDefault());
        // 4. 保存
        addressService.save(address);
    }
}
