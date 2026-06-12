package com.dong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.common.Result;
import com.dong.dto.PageDTO;
import com.dong.dto.ProductDTO;
import com.dong.entity.Category;
import com.dong.entity.Product;
import com.dong.entity.ProductDetail;
import com.dong.mapper.ProductMapper;
import com.dong.service.ICategoryService;
import com.dong.service.IProductDetailService;
import com.dong.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    private final IProductDetailService productDetailService;
    private final ICategoryService categoryService;

    @Override
    public PageDTO<Product> getProducts(ProductDTO productDTO) {
        // 1. 分页条件获取
        Page<Product> page = productDTO.toPage();

        // 2. 查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        //2.1  分类
        wrapper.eq(productDTO.getCategoryId() != null,
                Product::getCategoryId, productDTO.getCategoryId());
        //2.2 价格
        wrapper.ge(productDTO.getMinPrice() != null,
                        Product::getPrice, productDTO.getMinPrice())
                .le(productDTO.getMaxPrice() != null,
                        Product::getPrice, productDTO.getMaxPrice());
        //2.3 是否限定
        wrapper.eq(productDTO.getIsLimited() != null,
                 Product::getIsLimited,
                 Boolean.TRUE.equals(productDTO.getIsLimited()) ? 1 : 0);

        //2.4 关键字
        if (StrUtil.isNotBlank(productDTO.getKeyword())){
            wrapper.and(
                    w->w.like(Product::getName, productDTO.getKeyword())
                        .or()
                        .like(Product::getAnimeName, productDTO.getKeyword())
                        .or()
                        .like(Product::getCharacterName, productDTO.getKeyword())
            );
        }

        // 3. 查询数据，使用IService的page方法
        Page<Product> p = this.page(page, wrapper);

        // 4. 转换数据
        return PageDTO.of(p, Product.class);
    }

    /**
     * 获取商品详情
     * @param id 商品id
     * @return 商品详情
     */
    @Override
    public Result<ProductVO> getProductById(Long id) {
        // 1. 查询商品信息
        Product product = getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 2. 复制商品基本信息到 VO
        ProductVO productVO = new ProductVO();
        // 复制数据，因为后文new了 productVO，如果写ProductVO.class，内容会被丢弃
        BeanUtil.copyProperties(product, productVO);

        // 3. 查询分类信息
        Long categoryId = product.getCategoryId();
        if (categoryId != null) {
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                productVO.setCategoryId(category.getId());
                productVO.setCategoryName(category.getName());
            }
        }

        // 4. 查询商品详情（图片、描述等）
        ProductDetail productDetail = productDetailService.getById(id);
        if (productDetail != null) {
            productVO.setImages(productDetail.getImages());
            productVO.setDescription(productDetail.getDescription());
        }

        return Result.success(productVO);
    }
}
