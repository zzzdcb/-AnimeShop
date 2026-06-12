package com.dong.service;

import com.dong.common.Result;
import com.dong.dto.PageDTO;
import com.dong.dto.ProductDTO;
import com.dong.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.utils.PageQuery;
import com.dong.vo.ProductVO;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
public interface IProductService extends IService<Product> {


    PageDTO<Product> getProducts(ProductDTO productDTO);

    Result<ProductVO> getProductById(Long id);
}
