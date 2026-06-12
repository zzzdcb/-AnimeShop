package com.dong.dto;

import com.dong.utils.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true) // 继承父类属性,保证子类和父类属性一致
@Data
public class ProductDTO extends PageQuery {

    //分类ID
    private Long categoryId;

    //关键字
    private String keyword;

    //最小价格
    private BigDecimal minPrice;

    //最大价格
    private BigDecimal maxPrice;

    //是否限定
    private Boolean isLimited;
}
