package com.dong.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AddOrderVO {

    private String orderNo; // 订单号

    private BigDecimal payAmount; // 支付金额

    private LocalDateTime expireTime; // 过期时间
}
