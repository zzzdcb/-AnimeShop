package com.dong.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.common.RabbitMQProducer;
import com.dong.common.Result;
import com.dong.common.SnowflakeIdWorker;
import com.dong.dto.OrderDTO;
import com.dong.dto.OrderItemDTO;
import com.dong.dto.PageDTO;
import com.dong.entity.*;
import com.dong.mapper.OrdersMapper;
import com.dong.producer.cart.RemoveCartMessage;
import com.dong.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.utils.PageQuery;
import com.dong.utils.RabbitMQConstants;
import com.dong.utils.SecurityUtils;
import com.dong.utils.TimeUtil;
import com.dong.vo.AddOrderVO;
import com.dong.vo.GetOrdersVO;
import com.dong.vo.OrderItemsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    private final IOrderItemService orderItemService;
    private final IAddressService addressService;
    private final SnowflakeIdWorker snowflakeIdWorker;
    private final ICartService cartService;
    private final IProductService productService;
    private final TimeUtil timeUtil;
    private final RabbitMQProducer rabbitMQProducer;

    /**
     * 创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<AddOrderVO> createOrder(OrderDTO orderDTO) {
        Long userId = SecurityUtils.getCurrentUser().getId();

        // 1. 获取地址
        Address address = addressService.getById(orderDTO.getAddressId());
        if (address == null) {
            return Result.error("地址不存在");
        }

        // 2. 提取商品信息
        List<Long> productIds = orderDTO.getItems().stream()
                .map(OrderItemDTO::getProductId)
                .collect(Collectors.toList());

        Map<Long, Integer> quantityMap = orderDTO.getItems().stream()
                .collect(Collectors.toMap(OrderItemDTO::getProductId, OrderItemDTO::getQuantity));

        // 3. 查询商品
        List<Product> products = productService.listByIds(productIds);

        // 4. 校验库存
        List<String> failProducts = products.stream()
                .filter(product -> product.getStock() < quantityMap.get(product.getId()))
                .map(Product::getName)
                .toList();

        if (!failProducts.isEmpty()) {
            return Result.error("商品" + failProducts + "库存不足");
        }

        // 5. 生成订单ID
        Long orderId = snowflakeIdWorker.nextId();
        String orderNo = snowflakeIdWorker.nextIdStr();

        // 6. 批量更新库存
        String caseSql = products.stream()
                .map(p -> "WHEN " + p.getId() + " THEN stock - " + quantityMap.get(p.getId()))
                .collect(Collectors.joining(" "));

        String ids = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        productService.getBaseMapper().update(null,
                new UpdateWrapper<Product>().setSql("stock = CASE id " + caseSql + " END")
                        .apply("id IN (" + ids + ")"));

        // 7. 构建订单明细
        List<OrderItem> orderItems = products.stream()
                .map(product -> {
                    OrderItem item = new OrderItem();
                    item.setOrderId(orderId);
                    item.setProductId(product.getId());
                    item.setProductName(product.getName());
                    item.setPrice(product.getPrice());
                    item.setQuantity(quantityMap.get(product.getId()));
                    return item;
                })
                .collect(Collectors.toList());

        orderItemService.saveBatch(orderItems);

        // 8. 计算总金额
        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 9. 保存订单
        Orders order = new Orders();
        order.setId(orderId);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(1);
        order.setAddressSnapshot(address.getReceiver() + " " + address.getPhone() + " " + address.getAddress());
        order.setRemark(orderDTO.getRemark());
        order.setCreateTime(LocalDateTime.now());
        save(order);

        // 10. 返回
        AddOrderVO vo = new AddOrderVO();
        vo.setOrderNo(orderNo);
        vo.setPayAmount(totalAmount);
        vo.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return Result.success(vo);
    }

    /**
     * 获取订单列表
     */
    @Override
    public Result<PageDTO<GetOrdersVO>> getOrders(Integer pageNum, Integer status) {
        // 1. 获取登录用户ID
        Long userId = SecurityUtils.getCurrentUser().getId();
        // 2. 转成分页参数查询（工具类实现）
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(pageNum);
        Page<Orders> pageByCreateTime = pageQuery.toMpPageDefaultSortByCreateTime();
        Page<Orders> page = lambdaQuery()
                .eq(Orders::getUserId, userId)
                .eq(status != null, Orders::getStatus, status)
                .page(pageByCreateTime);
        // 3. 调用工具类转成VO返回
        PageDTO<GetOrdersVO> getOrdersVOPageDTO = PageDTO.of(page, this::convertOrderVO);
        return Result.success(getOrdersVOPageDTO);
    }

    /**
     * 取消订单
     */
    @Override
    public Result<String> cancelOrder(Long orderId) {
        // 1. 获取登录用户ID
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 2. 判断订单是否存在
        Orders orders = lambdaQuery()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getId, orderId)
                .one();
        if (orders == null) {
            return Result.error("订单不存在");
        }
        // 3. 判断订单状态，待付款和待发货状态都可以取消
        Integer status = orders.getStatus();
        if (status != 0 && status != 1) {
            return Result.error("订单状态错误，只能取消待付款或待发货的订单");
        }
        // 4. 查询订单明细
        List<OrderItem> orderItemList = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .list();
        // 5. 恢复库存，失败的保存
        List<String> failList = orderItemList.stream()
                .filter(item -> {
                    boolean success = productService.lambdaUpdate()
                            .eq(Product::getId, item.getProductId())
                            .setSql("stock=stock+" + item.getQuantity())
                            .update();
                    return !success;
                })
                .map(OrderItem::getProductName)
                .toList();
        if (!failList.isEmpty()) {
            log.error("恢复库存失败: orderId={}, 失败商品={}", orderId, failList);
            throw new RuntimeException("恢复库存失败");
        }
        // 6. 订单取消
        boolean update = lambdaUpdate()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, 4)
                .update();
        if (!update) {
            throw new RuntimeException("订单取消失败");
        }
        log.info("订单取消成功: orderId={}", orderId);
        return Result.success("订单取消成功");
    }

    /**
     * 确认收货
     */
    @Override
    public Result<String> confirmOrder(Long orderId) {
        // 1. 获取登录用户ID
        Long userId = SecurityUtils.getCurrentUser().getId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        // 2. 校验
        Orders orders = lambdaQuery()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getId, orderId)
                .one();
        if (orders == null) {
            return Result.error("订单不存在");
        }
        if (orders.getStatus() != 2) {
            return Result.error("订单状态错误");
        }
        // 3. 订单确认
        boolean update = lambdaUpdate()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, 3)
                .update();
        // 4. 判断是否成功
        if (!update) {
            return Result.error("订单确认失败");
        }
        return Result.success("订单确认成功");
    }

    /**
     * 获取订单状态文本
     */
    private String getStatuxText(Integer status) {
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "完成";
            case 4 -> "取消";
            default -> "未知";
        };
    }

    /**
     * 将订单转换成VO，并查询订单明细封装到VO中
     */
    private GetOrdersVO convertOrderVO(Orders orders) {
        GetOrdersVO getOrderVO = new GetOrdersVO();
        // 1. 查询订单明细
        List<OrderItem> orderItemList = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, orders.getId())
                .list();
        // 2. 获取商品ID列表并查询商品信息
        List<Long> productIds = orderItemList.stream()
                .map(OrderItem::getProductId)
                .toList();
        List<Product> productList = productService.listByIds(productIds);
        // 3. 构建商品ID到商品的映射
        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        // 4. 转成订单明细VO
        List<OrderItemsVO> orderItemsVOList = orderItemList.stream()
                .map(orderItem -> {
                    OrderItemsVO orderItemsVO = new OrderItemsVO();
                    orderItemsVO.setProductId(orderItem.getProductId());
                    orderItemsVO.setProductName(orderItem.getProductName());
                    orderItemsVO.setPrice(orderItem.getPrice());
                    orderItemsVO.setQuantity(orderItem.getQuantity());
                    // 从商品信息中获取图片
                    Product product = productMap.get(orderItem.getProductId());
                    if (product != null) {
                        orderItemsVO.setImage(product.getMainImage());
                    }
                    return orderItemsVO;
                }).toList();
        // 5. 封装订单VO
        getOrderVO.setOrderId(orders.getId());
        getOrderVO.setOrderNo(orders.getOrderNo());
        getOrderVO.setPayAmount(orders.getPayAmount());
        getOrderVO.setStatus(orders.getStatus());
        getOrderVO.setStatusText(getStatuxText(orders.getStatus()));
        getOrderVO.setCreateTime(orders.getCreateTime());
        getOrderVO.setOrderItemList(orderItemsVOList);
        return getOrderVO;
    }
}
