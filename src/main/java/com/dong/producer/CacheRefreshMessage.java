package com.dong.producer;

import lombok.Data;

@Data
public class CacheRefreshMessage {

    private String key; // 缓存key
    private Long id; // 数据id
    private Long time; // 过期时间
    private String unit; // 过期时间单位
}
