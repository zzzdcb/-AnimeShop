package com.dong.dto;

import lombok.Data;

@Data
public class AddCartDTO {

    private Long productId; // 商品ID

    private Integer quantity; // 新数量
}
