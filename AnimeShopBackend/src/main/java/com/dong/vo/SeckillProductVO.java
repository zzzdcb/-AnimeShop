package com.dong.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillProductVO {
    private Long id; // 商品id
    private String name; // 商品名称
    private String img; // 商品主图
    private BigDecimal seckillPrice; // 秒杀价格
    private BigDecimal originalPrice; // 原价
    private Integer stock;  // 库存
}
