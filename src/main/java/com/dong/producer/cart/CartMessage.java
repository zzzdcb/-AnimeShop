package com.dong.producer.cart;

import lombok.Data;

@Data
public class CartMessage {

    private Long userId; // 用户id
    private Long productId; // 商品id
    private Integer quantity; // 商品数量
}
