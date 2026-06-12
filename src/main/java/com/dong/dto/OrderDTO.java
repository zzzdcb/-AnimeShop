package com.dong.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDTO> items; // 信息（订单和数量）

    @NotNull(message = "收货地址不能为空")
    private Long addressId; // 收货地址ID

    private String remark; // 订单备注
}
