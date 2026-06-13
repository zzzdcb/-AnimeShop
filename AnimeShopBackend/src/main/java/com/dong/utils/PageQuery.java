package com.dong.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageQuery {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy;
    private Boolean isAsc; // true=升序, false=降序, null=默认降序

    /**
     * 构建分页对象（自动处理排序）
     */
    public <T> Page<T> toPage(OrderItem... extraItems) {
        Page<T> page = Page.of(pageNo, pageSize);

        // 1. 处理主要排序（如果指定了排序字段）
        if (StrUtil.isNotBlank(sortBy)) {
            // 判断排序方向：前端传了用前端，没传默认降序
            boolean asc = isAsc != null ? isAsc : false;
            if (asc) {
                page.addOrder(OrderItem.asc(sortBy));
            } else {
                page.addOrder(OrderItem.desc(sortBy));
            }
        }

        // 2. 添加额外的排序条件（如果有）
        if (extraItems != null) {
            for (OrderItem item : extraItems) {
                page.addOrder(item);
            }
        }

        return page;
    }

    /**
     * 按默认字段升序排序
     */
    public <T> Page<T> toMpPage(String defaultSortBy) {
        if (StrUtil.isBlank(sortBy)) {
            this.sortBy = defaultSortBy;
            this.isAsc = true;
        }
        return toPage();
    }

    /**
     * 按创建时间降序（最常用）
     */
    public <T> Page<T> toMpPageDefaultSortByCreateTime() {
        if (StrUtil.isBlank(sortBy)) {
            this.sortBy = "create_time";
            this.isAsc = false;
        }
        return toPage();
    }

    /**
     * 按更新时间降序
     */
    public <T> Page<T> toMpPageDefaultSortByUpdateTime() {
        if (StrUtil.isBlank(sortBy)) {
            this.sortBy = "update_time";
            this.isAsc = false;
        }
        return toPage();
    }

    /**
     * 价格排序专用（默认升序）
     */
    public <T> Page<T> toMpPageSortByPrice() {
        if (StrUtil.isBlank(sortBy)) {
            this.sortBy = "price";
            this.isAsc = true;
        }
        return toPage();
    }
}