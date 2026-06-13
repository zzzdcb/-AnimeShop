package com.dong.service;

import com.dong.common.Result;
import com.dong.dto.AddCartDTO;
import com.dong.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.vo.CartVO;

import java.util.List;

/**
 * <p>
 * 购物车商品表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-23
 */
public interface ICartService extends IService<Cart> {

    Result<String> addCart(AddCartDTO addCartDTO);

    Result<CartVO> getCartListByUserId();

    Result<String> removeCart(List<Long> productIds);
}
