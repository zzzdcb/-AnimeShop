package com.dong.service.impl;

import com.dong.common.Result;
import com.dong.entity.Category;
import com.dong.mapper.CategoryMapper;
import com.dong.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public Result<List<Category>> getCcategories() {
        List< Category> list = query().list();
        return Result.success(list);
    }
}
