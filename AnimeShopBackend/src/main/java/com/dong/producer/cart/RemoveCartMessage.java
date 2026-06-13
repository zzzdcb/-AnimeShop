package com.dong.producer.cart;

import lombok.Data;

import java.util.List;

@Data
public class RemoveCartMessage {

    private Long userId; // 用户ID
    private List<Long> productIdList; // 商品ID列表
    private String explain; // 删除原因
}
