package com.dong.producer.seckill;

import lombok.Data;

@Data
public class SeckillMessage {

    private Long userId; // 用户id
    private Long productId; // 秒杀商品id
    private Integer quantity; // 秒杀商品数量
}
