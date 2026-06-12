package com.dong.consumer.seckill;

import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQConsumer;
import com.dong.common.RedisExpireData;
import com.dong.common.SnowflakeIdWorker;
import com.dong.entity.Address;
import com.dong.entity.Orders;
import com.dong.entity.Product;
import com.dong.entity.Seckill;
import com.dong.producer.seckill.SeckillMessage;
import com.dong.service.IAddressService;
import com.dong.service.IOrdersService;
import com.dong.service.IProductService;
import com.dong.service.ISeckillService;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.TimeUtil;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeckillConsumer {

    private final RabbitMQConsumer rabbitMQConsumer;
    private final IOrdersService ordersService;
    private final IAddressService addressService;
    private final SnowflakeIdWorker snowflakeIdWorker;
    private final IProductService productService;
    private final ISeckillService seckillService;

    @RabbitListener(queues = RabbitMQConstants.SECKILL_QUEUE)
    public void consume(@Payload String message,
                        @Header("businessId") String businessId,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        Channel channel) {
        rabbitMQConsumer.consumeWithRedisIdempotent(
                businessId, message, deliveryTag, channel,
                () -> createOrder( message)
        );
    }

    /**
     * 秒杀成功，创建订单
     */
    private void createOrder(String  message) {
        // 1. 解析消息
        SeckillMessage seckillMessage = JSONUtil.toBean(message, SeckillMessage.class);
        Long userId = seckillMessage.getUserId();
        Long productId = seckillMessage.getProductId();
        Integer quantity = seckillMessage.getQuantity();
        // 2. 获取地址
        Address address = addressService.lambdaQuery()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .one();
        if (address == null){
            log.error("用户{}没有设置地址", userId);
            return;
        }
        // 3. 判断
        Product product = productService.getById(productId);
        if (product == null){
            log.error("商品{}不存在", productId);
            return;
        }
        Seckill seckill = seckillService.lambdaQuery()
                .eq(Seckill::getProductId, productId)
                .one();
        if (seckill == null){
            log.error("商品{}没有秒杀活动", productId);
            return;
        }
        // 4. 计算金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));
        BigDecimal payAmount = seckill.getSeckillPrice().multiply(new BigDecimal(quantity));
        // 5. 创建订单
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setOrderNo(snowflakeIdWorker.nextIdStr());
        orders.setId(snowflakeIdWorker.nextId());
        orders.setTotalAmount(totalAmount);
        orders.setPayAmount(payAmount);
        orders.setStatus(2);
        orders.setAddressSnapshot(JSONUtil.toJsonStr(address));
        orders.setRemark("秒杀订单");
        orders.setCreateTime(LocalDateTime.now());
        ordersService.save(orders);
        log.info("用户{}秒杀成功，订单编号：{}", userId, orders.getOrderNo());
        // 6. 扣库存
        boolean success = seckillService.lambdaUpdate()
                .eq(Seckill::getProductId, productId)
                .ge(Seckill::getStock, quantity)
                .setSql("stock = stock - " + quantity)
                .update();
        if (!success) {
            log.error("秒杀库存扣减失败, seckillId={}, quantity={}", seckill.getId(), quantity);
            // 订单已创建，需要删除
            ordersService.removeById(orders.getId());
            throw new RuntimeException("秒杀库存不足");
        }
    }
}
