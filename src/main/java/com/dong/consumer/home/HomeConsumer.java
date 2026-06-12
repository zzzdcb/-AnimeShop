package com.dong.consumer.home;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQConsumer;
import com.dong.common.RedisCatchExpire;
import com.dong.entity.Product;
import com.dong.entity.Seckill;
import com.dong.producer.Home.HomeCatchRefreshMessage;
import com.dong.service.IProductService;
import com.dong.service.ISeckillService;
import com.dong.utils.RabbitMQConstants;
import com.dong.vo.HotProductVO;
import com.dong.vo.NewProductVO;
import com.dong.vo.SeckillProductVO;
import com.dong.vo.SeckillVO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HomeConsumer {
    private final RedisCatchExpire redisCatchExpire;
    private final IProductService productService;
    private final RabbitMQConsumer rabbitMQConsumer;
    private final ISeckillService seckillService;

    @RabbitListener(queues = RabbitMQConstants.HOME_QUEUE)
    public void homeConsume(@Payload String message,
                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                            @Header("businessId") String businessId,
                            Channel  channel) {
        rabbitMQConsumer.consumeWithRedisIdempotent(
                businessId, message, deliveryTag, channel,
                () -> handleHome(message));
    }

    /**
     * 处理秒杀业务
     */
    private void handleHome(String message) {
        HomeCatchRefreshMessage msg = JSONUtil.toBean(message, HomeCatchRefreshMessage.class);
        switch (msg.getType()){
            case "hot" :
                hotProductList(msg);
                break;
            case "new" :
                newProductList(msg);
                break;
            case "seckillProduct" :
                seckillProductList(msg);
                break;
            default:
                log.warn("未知类型: {}", msg.getType());
        }
    }

    /**
     * 刷新首页热销商品列表
     */
    private void hotProductList(HomeCatchRefreshMessage msg){
        String hotProductKey = msg.getKey();
        List<Product> products = productService.lambdaQuery()
                .orderByDesc(Product::getSales)
                .last("limit 4")
                .list();
        List<HotProductVO> hotProductVOList = BeanUtil.copyToList(products, HotProductVO.class);
        TimeUnit timeUnit = TimeUnit.valueOf(msg.getUnit());
        redisCatchExpire.catchExpire(hotProductKey, hotProductVOList, msg.getTime(), timeUnit);
    }

    /**
     * 获取最新商品列表
     */
    private void newProductList(HomeCatchRefreshMessage msg){
        String newProductKey = msg.getKey();
        List<Product> products = productService.lambdaQuery()
                .orderByDesc(Product::getCreateTime)
                .last("limit 4")
                .list();
        List<NewProductVO> newProductVOList = BeanUtil.copyToList(products, NewProductVO.class);
        TimeUnit timeUnit = TimeUnit.valueOf(msg.getUnit());
        redisCatchExpire.catchExpire(newProductKey, newProductVOList, msg.getTime(), timeUnit);
    }

    /**
     * 秒杀商品列表
     */
    private void seckillProductList(HomeCatchRefreshMessage msg){
        String seckillProductKey = msg.getKey();
        // 1. 根据当前时间获取还在进行中的秒杀活动
        List<Seckill> seckillList = seckillService.lambdaQuery()
                .le(Seckill::getStartTime, LocalDateTime.now())
                .ge(Seckill::getEndTime, LocalDateTime.now())
                .list();
        if (seckillList.isEmpty()){
            log.info("没有进行中的秒杀活动");
            TimeUnit timeUnit = TimeUnit.valueOf(msg.getUnit().toUpperCase());
            redisCatchExpire.catchExpire(seckillProductKey, Collections.emptyList(), msg.getTime(), timeUnit);
            return;
        }
        // 2. 获取秒杀商品列表，并按id分类
        List<Long> productIds = seckillList.stream().map(Seckill::getProductId).toList();
        List<Product> productList = productService.listByIds(productIds);
        Map<Long, Product> productMap = productList
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        // 3. 封装数据
        List<SeckillVO> seckillVOList = seckillList.stream().map(seckill -> {
            SeckillVO seckillVO = new SeckillVO();
            // 计算剩余时间
            seckillVO.setRemainingSeconds(Duration.between(LocalDateTime.now(), seckill.getEndTime()).getSeconds());
            Product product = productMap.get(seckill.getProductId());
            SeckillProductVO seckillProductVO = new SeckillProductVO();
            seckillProductVO.setId(product.getId());
            seckillProductVO.setName(product.getName());
            seckillProductVO.setImg(product.getMainImage());
            seckillProductVO.setSeckillPrice(seckill.getSeckillPrice());
            seckillProductVO.setOriginalPrice(product.getPrice());
            seckillProductVO.setStock(seckill.getStock());
            // 用简单不可变集合封装秒杀商品信息
            seckillVO.setSeckillProductList(Collections.singletonList(seckillProductVO));
            return seckillVO;
        }).toList();
        TimeUnit timeUnit = TimeUnit.valueOf(msg.getUnit());
        redisCatchExpire.catchExpire(seckillProductKey, seckillVOList, msg.getTime(), timeUnit);
    }

}
