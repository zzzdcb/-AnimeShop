package com.dong.service.impl;

import com.dong.common.Result;
import com.dong.dto.AddCartDTO;
import com.dong.entity.Cart;
import com.dong.entity.Product;
import com.dong.mapper.CartMapper;
import com.dong.common.RabbitMQProducer;
import com.dong.producer.cart.CartMessage;
import com.dong.producer.cart.RemoveCartMessage;
import com.dong.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.service.IProductService;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.RedisConstants;
import com.dong.utils.SecurityUtils;
import com.dong.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车商品表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-05-23
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final RabbitMQProducer rabbitMQProducer;
    private final IProductService productService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 添加、更新购物车，发送到消息队列
     *
     */
    @Override
    public Result<String> addCart(AddCartDTO addCartDTO) {

        // 1. 获取登录用户id
        Long userId = SecurityUtils.getCurrentUser().getId();

        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 设置业务ID
        String cartupdateId = "cart:update" + userId + ":"
                + addCartDTO.getProductId() + ":" + System.currentTimeMillis();
        // 3. 构建消息
        CartMessage cartMessage = new CartMessage();
        cartMessage.setUserId(userId);
        cartMessage.setProductId(addCartDTO.getProductId());
        cartMessage.setQuantity(addCartDTO.getQuantity());
        // 4. 发送消息
        rabbitMQProducer.send(
                RabbitMQConstants.CART_EXCHANGE, // 交换机
                RabbitMQConstants.CART_ROUTING, // 路由键
                cartMessage, // 消息体
                cartupdateId // 业务ID
        );
        return Result.success("操作成功");
    }

    /**
     * 获取购物车列表
     *
     * @return
     */
    @Override
    public Result<CartVO> getCartListByUserId() {
        // 1. 获取登录用户id
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 查询
        // 2.1 先查缓存
        String cartKey = RedisConstants.CART_KEY + userId;
        Map<Object, Object> cartMap = stringRedisTemplate.opsForHash().entries(cartKey);
        // 2.1.1 缓存为空，直接返回
        if (cartMap.containsKey("empty")) {
            return Result.success("购物车为空");
        }
        // 2.1.2 缓存不为空
        if (!cartMap.isEmpty()) {
            List<Cart> cartList = cartMap.entrySet().stream().map(entry -> {
                Cart cart = new Cart();
                cart.setProductId(Long.valueOf(entry.getKey().toString()));
                cart.setQuantity(Integer.valueOf(entry.getValue().toString()));
                return cart;
            }).toList();
            List<Long> productIdList = cartList.stream().map(Cart::getProductId).toList();
            // 获取商品列表
            List<Product> products = productService.listByIds(productIdList);
            return Result.success(buildCartVO(cartList, products));
        }
        // 2.2 缓存没有查数据库
        // 2.2.1 获取购物车列表
        List<Cart> cartListByUserId = lambdaQuery().eq(Cart::getUserId, userId).list();
        // 2.2.2 数据库没数据，标记空数据存进Redis，过期时间5分钟
        if (cartListByUserId.isEmpty()) {
            stringRedisTemplate.opsForHash().put(cartKey, "empty", "1");
            stringRedisTemplate.expire(cartKey, 5, TimeUnit.MINUTES);
            return Result.success("购物车为空");
        }
        // 2.2.3 获取商品列表
        List<Long> productIdList = cartListByUserId.stream().map(Cart::getProductId).toList();
        List<Product> products = productService.listByIds(productIdList);
        // 3. 同步到Redis
        Map<String, String> cartToRedisMp = cartListByUserId.stream().collect(Collectors.toMap(
                cart -> cart.getProductId().toString(),
                cart -> cart.getQuantity().toString()
        ));
        stringRedisTemplate.opsForHash().putAll(cartKey, cartToRedisMp);
        stringRedisTemplate.expire(cartKey, RedisConstants.CART_USER_TTL, TimeUnit.DAYS);
        // 4. 调用方法返回
        return Result.success(buildCartVO(cartListByUserId, products));
    }

    /**
     * 删除购物车
     *
     */
    @Override
    public Result<String> removeCart(List<Long> productIds) {
        // 1. 获取登录用户id
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("请先登录");
        }
        // 2. 构建消息体
        RemoveCartMessage removeCartMessage = new RemoveCartMessage();
        removeCartMessage.setUserId(userId);
        removeCartMessage.setProductIdList(productIds);
        removeCartMessage.setExplain("removeCart");
        // 3. 构建业务ID
        String productIdsStr = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String businessId = "cart:remove:" + userId + ":" + productIdsStr;
        // 4. 发送到消息队列删除
        rabbitMQProducer.send(
                RabbitMQConstants.CART_EXCHANGE, // 交换机
                RabbitMQConstants.REMOVE_CART_ROUTING, // 路由键
                removeCartMessage,
                businessId
        );
        return Result.success("删除成功");
    }


    /**
     * 计算总价工具方法
     *
     */
    private BigDecimal getTotalPrice(List<Product> products, List<Cart> cartList) {
        //1. 转成商品Id到商品的映射,确保id和商品一一对应
        Map<Long, Product> productMap = products
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        //2. 计算总价并返回
        return cartList.stream()
                .map(cart -> {
                    Product product = productMap.get(cart.getProductId());
                    if (product == null || product.getPrice() == null) {
                        return BigDecimal.ZERO;
                    }
                    return product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算总量构建返回
     *
     */
    private CartVO buildCartVO(List<Cart> cartList, List<Product> products) {
        BigDecimal totalPrice = getTotalPrice(products, cartList);
        int totalQuantity = cartList.stream()
                .mapToInt(Cart::getQuantity)
                .sum();
        CartVO cartVO = new CartVO();
        cartVO.setTotalPrice(totalPrice);
        cartVO.setTotalQuantity(totalQuantity);
        cartVO.setItems(products);
        return cartVO;
    }

}
