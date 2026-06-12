package com.dong.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {

    private List<OrderItemDTO> items; // 信息（订单和数量）

    private Long addressId; // 收货地址ID

    private String remark; // 订单备注
}
