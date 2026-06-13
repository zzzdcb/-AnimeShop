package com.dong.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NewProductVO {
    private Long id; // 商品id
    private String name; // 商品名称
    private String animeName; // 动漫名称
    private BigDecimal price; // 商品价格
    private String mainImage; // 商品图片
}
