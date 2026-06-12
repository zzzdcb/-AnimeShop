package com.dong.controller;


import com.dong.common.Result;
import com.dong.dto.PageDTO;
import com.dong.dto.ProductDTO;
import com.dong.entity.Product;
import com.dong.service.IProductService;
import com.dong.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public Result<PageDTO<Product>> getPageProducts(ProductDTO productDTO) {
        PageDTO<Product> products = productService.getProducts(productDTO);
        return Result.success(products);
    }

    @GetMapping("{id}")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        return productService.getProductById( id);
    }
}
