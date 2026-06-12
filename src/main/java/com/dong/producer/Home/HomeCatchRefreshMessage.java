package com.dong.producer.Home;

import lombok.Data;

@Data
public class HomeCatchRefreshMessage {
    private String key; // 缓存key
    private String type; // 缓存类型
    private Long time; // 过期时间
    private String unit; // 过期时间单位
}
