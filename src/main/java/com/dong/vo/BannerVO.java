package com.dong.vo;

import lombok.Data;

@Data
public class BannerVO {

    private String title; // 标题
    private String imageUrl; // 图片地址
    private String linkUrl; // 链接地址
    private String type; // 类型
}
