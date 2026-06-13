package com.dong.schedTask;

import com.alibaba.fastjson.JSON;
import com.dong.common.RedisCatchExpire;
import com.dong.entity.Product;
import com.dong.service.IProductService;
import com.dong.utils.RedisConstants;
import com.dong.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NewProductTask {

    private final IProductService productService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisCatchExpire redisCatchExpire;

    @Scheduled(cron = "${task.home.new_shop_cron}")
    public void newProduct(){
        log.info("开始执行最新商品统计定时任务");
        // 1. 获取最新商品
        List<Product> newProductList = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime)
                .last("limit 4")
                .list();
        // 2. 构建key和value
        String newProductKey = RedisConstants.HOME_NEW_PRODUCT_KEY;
        String newProductStr = JSON.toJSONString(newProductList);
        // 3. 删除旧数据
        stringRedisTemplate.delete(newProductKey);
        // 4. 新增到缓存，设置逻辑过期时间为24小时
        redisCatchExpire.catchExpire(newProductKey, newProductStr, 24L, TimeUnit.HOURS);
    }
}
