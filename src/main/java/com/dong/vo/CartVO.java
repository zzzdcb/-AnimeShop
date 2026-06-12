package com.dong.vo;

import com.dong.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {
    private List<Product> items; // 购物车商品列表

    private BigDecimal totalPrice; // 购物车商品总价

    private Integer totalQuantity; // 购物车商品总数量
}
