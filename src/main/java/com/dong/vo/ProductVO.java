package com.dong.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情返回对象（完全匹配你要的JSON结构）
 */
@Data
public class ProductVO {

    private Long id; // 商品ID

    private String name; // 商品名称

    private String animeName; // 动画名称

    private String characterName; // 角色名称

    private BigDecimal price; // 商品价格

    private BigDecimal originalPrice; // 商品原价

    private String mainImage; // 商品主图

    private List<String> images; // 商品轮播图

    private String description; // 商品描述

    private Integer stock; // 商品库存

    private Integer sales; // 商品销量

    private Boolean isLimited; // 是否限定

    private Long categoryId; // 商品分类ID

    private String categoryName; // 商品分类名称
}