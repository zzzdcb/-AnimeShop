package com.dong.vo;

import com.dong.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GetOrdersVO {

    private String orderId;
    private String orderNo; // 订单号
    private BigDecimal payAmount; // 支付金额
    private Integer status; // 订单状态：0-待付款,1-待发货,2-待收货,3-待评价,4-已完成,5-已取消
    private String statusText; // 订单状态文本
    private LocalDateTime createTime; // 下单时间
    private List<OrderItemsVO> orderItemList; // 订单项列表
}
