package com.dong.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 所属番剧
     */
    @TableField("anime_name")
    private String animeName;

    /**
     * 角色名称
     */
    @TableField("character_name")
    private String characterName;

    /**
     * 售价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 主图URL
     */
    @TableField("main_image")
    private String mainImage;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 是否限定:0-否,1-是
     */
    @TableField("is_limited")
    private Integer isLimited;

    /**
     * 状态:0-下架,1-上架
     */
    @TableField("status")
    private Integer status;

    /**
     * 上架时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
