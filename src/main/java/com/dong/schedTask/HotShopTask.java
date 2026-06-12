package com.dong.schedTask;

import com.alibaba.fastjson.JSON;
import com.dong.common.RedisCatchExpire;
import com.dong.entity.Product;
import com.dong.service.IProductService;
import com.dong.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class HotShopTask {

    private final RedisCatchExpire redisCatchExpire;
    private final IProductService productService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 定时任务：统计热销商品
     */
    @Scheduled(cron = "${task.home.hot_shop_cron}")
    public void hotShop(){
        log.info("开始执行热销商品统计定时任务");
        // 1. 获取销量前四的商品
        List<Product> productHotList = productService.lambdaQuery()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("limit 4")
                .list();
        // 获取销量最高的商品给轮播图用
        // 2. 设置key和 value
        String hotProductKey = RedisConstants.HOME_HOT_PRODUCT_KEY;
        String productString = JSON.toJSONString(productHotList);
        // 3. 删除旧数据
        stringRedisTemplate.delete(hotProductKey);
        // 4. 保存新数据，适应工具类防击穿，过期时间设置24小时，与定时任务刷新时间一致
        redisCatchExpire.catchExpire(
                hotProductKey, productString,
                24L, TimeUnit.HOURS);
        log.info("热销商品统计完成，共{}件", productHotList.size());
    }
}
