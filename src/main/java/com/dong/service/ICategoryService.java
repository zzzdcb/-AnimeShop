package com.dong.service;

import com.dong.common.Result;
import com.dong.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 分类表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
public interface ICategoryService extends IService<Category> {

    Result<List<Category>> getCcategories();
}
