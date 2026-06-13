package com.dong.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemsVO {
    private Long productId; // 商品ID
    private String productName; // 商品名称
    private BigDecimal price; // 购买单价
    private Integer quantity; // 购买数量
    private String image; // 商品图片
}
