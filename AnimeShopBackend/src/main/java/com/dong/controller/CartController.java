package com.dong.controller;


import com.dong.common.Result;
import com.dong.dto.AddCartDTO;
import com.dong.service.ICartService;
import com.dong.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 购物车商品表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-05-23
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @PostMapping("/update")
    public Result<String> addCart(@RequestBody AddCartDTO addCartDTO) {
        return cartService.addCart(addCartDTO);
    }

    @GetMapping("/list")
    public Result<CartVO> getCartListByUserId() {
        return cartService.getCartListByUserId();
    }

    @DeleteMapping("/remove")
    public Result<String> removeCart(@RequestParam List<Long> productIds) {
        return cartService.removeCart(productIds);
    }
}
