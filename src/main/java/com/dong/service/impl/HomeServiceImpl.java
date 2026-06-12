package com.dong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.dong.common.RabbitMQProducer;
import com.dong.common.RedisCatchExpire;
import com.dong.common.Result;
import com.dong.entity.Product;
import com.dong.entity.Seckill;
import com.dong.producer.Home.HomeCatchRefreshMessage;
import com.dong.service.IHomeService;
import com.dong.service.IProductService;
import com.dong.service.ISeckillService;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.RedisConstants;
import com.dong.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeServiceImpl implements IHomeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitMQProducer rabbitMQProducer;
    private final RedisCatchExpire redisCatchExpire;
    private final IProductService productService;
    private final ISeckillService seckillService;

    /**
     * 获取轮播图
     */
    @Override
    public Result<List<BannerVO>> getBanner() {
        List<BannerVO> banners = new ArrayList<>();
        // 1. 获取数据
        List<HotProductVO> hotProductList = getHotProductList().getData();
        List<NewProductVO> newProductList = getNewProductList().getData();
        // 安全获取秒杀商品列表
        List<SeckillProductVO> seckillProductList = getSeckillVO().getData().stream()
                .findFirst()
                .map(SeckillVO::getSeckillProductList)
                .orElse(Collections.emptyList());
        // 2. 判断并组装数据
        if (!hotProductList.isEmpty()){
            HotProductVO hotProductVO = hotProductList.getFirst();
            BannerVO bannerVO = new BannerVO();
            bannerVO.setTitle("热销商品");
            bannerVO.setImageUrl(hotProductVO.getMainImage());
            bannerVO.setLinkUrl("/product/" + hotProductVO.getId());
            bannerVO.setType("product");
            banners.add(bannerVO);
        }
        if (!newProductList.isEmpty()){
            NewProductVO newProductVO = newProductList.getFirst();
            BannerVO bannerVO = new BannerVO();
            bannerVO.setTitle("新品上架");
            bannerVO.setImageUrl(newProductVO.getMainImage());
            bannerVO.setLinkUrl("/product/" + newProductVO.getId());
            bannerVO.setType("product");
            banners.add(bannerVO);
        }
        if (!seckillProductList.isEmpty()){
            SeckillProductVO seckillProduct = seckillProductList.getFirst();
            BannerVO bannerVO = new BannerVO();
            bannerVO.setTitle("秒杀特惠");
            bannerVO.setImageUrl(seckillProduct.getImg());
            bannerVO.setLinkUrl("/seckill/" + seckillProduct.getId());
            bannerVO.setType("product");
            banners.add(bannerVO);
        }
        return Result.success(banners);
    }

    /**
     * 获取热销商品
     */
    @Override
    public Result<List<HotProductVO>> getHotProductList() {
        String homeHotProductKey = RedisConstants.HOME_HOT_PRODUCT_KEY;
        Object result = redisCatchExpire.catchExpire(
                homeHotProductKey,
                "hot",
                Object.class,
                id -> getHotProducts(),
                id -> sendMQ(homeHotProductKey, "hot", 24L, "HOURS"),
                24L, TimeUnit.HOURS
        );
        List<HotProductVO> list = JSONUtil.toList(JSONUtil.parseArray(result), HotProductVO.class);
        return Result.success(list);
    }

    /**
     * 获取最新商品列表
     */
    @Override
    public Result<List<NewProductVO>> getNewProductList() {
        String homeNewProductKey = RedisConstants.HOME_NEW_PRODUCT_KEY;
        Object result = redisCatchExpire.catchExpire(
                homeNewProductKey,
                "new",
                Object.class,
                id -> getNewProducts(),
                id -> sendMQ(homeNewProductKey, "new", 24L, "HOURS"),
                24L, TimeUnit.HOURS
        );
        List<NewProductVO> list = JSONUtil.toList(JSONUtil.parseArray(result), NewProductVO.class);
        return Result.success(list);
    }

    /**
     * 获取秒杀商品
     */
    @Override
    public Result<List<SeckillVO>> getSeckillVO() {
        String homeSeckillKey = RedisConstants.HOME_SECKILL_PRODUCT_KEY;
        Object result = redisCatchExpire.catchExpire(
                homeSeckillKey,
                "seckillProduct",
                Object.class,
                id -> getSeckillProducts(),
                id -> sendMQ(homeSeckillKey, "seckillProduct", 5L, "MINUTES"),
                5L, TimeUnit.MINUTES
        );
        List<SeckillVO> list = JSONUtil.toList(JSONUtil.parseArray(result), SeckillVO.class);
        return Result.success(list);
    }

    /**
     * 从数据库获取热销商品列表
     */
    public List<HotProductVO> getHotProducts() {
        List<Product> products = productService.lambdaQuery()
                .orderByDesc(Product::getSales)
                .last("limit 4")
                .list();
        return BeanUtil.copyToList(products, HotProductVO.class);
    }

    /**
     * 从数据库获取新品列表
     */
    public List<NewProductVO> getNewProducts() {
        List<Product> products = productService.lambdaQuery()
                .orderByDesc(Product::getCreateTime)
                .last("limit 4")
                .list();
        return BeanUtil.copyToList(products, NewProductVO.class);
    }

    /**
     * 获取秒杀商品列表
     */
    public List<SeckillVO> getSeckillProducts() {
        // 1. 根据当前时间获取还在进行中的秒杀活动
        List<Seckill> seckillList = seckillService.lambdaQuery()
                .le(Seckill::getStartTime, LocalDateTime.now())
                .ge(Seckill::getEndTime, LocalDateTime.now())
                .list();
        if (seckillList.isEmpty()){
            return Collections.emptyList();
        }
        // 2. 获取秒杀商品列表，并按id分类
        List<Long> productIds = seckillList.stream().map(Seckill::getProductId).toList();
        List<Product> productList = productService.listByIds(productIds);
        Map<Long, Product> productMap = productList
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        // 3. 封装数据
        return seckillList.stream().map(seckill -> {
            SeckillVO seckillVO = new SeckillVO();
            // 计算剩余时间
            seckillVO.setRemainingSeconds(Duration.between(LocalDateTime.now(), seckill.getEndTime()).getSeconds());
            Product product = productMap.get(seckill.getProductId());
            if (product == null){
                log.warn("商品不存在, productId={}", seckill.getProductId());
                return null;
            }
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
    }


    /**
     * 发送到MQ中进行缓存重建
     * @param key 缓存key
     * @param type 方法类型
     * @param time 过期时间
     * @param unit 时间单位
     */
    private void sendMQ(String key,String type, Long time, String unit){
        String businessId = "refresh" + type + System.currentTimeMillis();
        HomeCatchRefreshMessage message = new HomeCatchRefreshMessage();
        message.setKey(key);
        message.setType(type);
        message.setTime(time);
        message.setUnit(unit);
        rabbitMQProducer.send(
                RabbitMQConstants.HOME_EXCHANGE,
                RabbitMQConstants.HOME_ROUTING,
                message,
                businessId
        );
    }
}
