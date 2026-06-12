package com.dong.service;

import com.dong.common.Result;
import com.dong.dto.OrderDTO;
import com.dong.dto.PageDTO;
import com.dong.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.vo.AddOrderVO;
import com.dong.vo.GetOrdersVO;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
public interface IOrdersService extends IService<Orders> {

    Result<AddOrderVO> createOrder(OrderDTO orderDTO);

    Result<PageDTO<GetOrdersVO>> getOrders(Integer pageNum, Integer status);

    Result<String> cancelOrder(Long orderId);

    Result<String> confirmOrder(Long orderId);
}
