package com.dong.vo;

import lombok.Data;

import java.util.List;

@Data
public class SeckillVO {
    private Long remainingSeconds; //  剩余秒数
    private List<SeckillProductVO> seckillProductList; // 秒杀商品列表
}
