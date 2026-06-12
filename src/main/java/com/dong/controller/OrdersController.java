package com.dong.controller;


import com.dong.common.Result;
import com.dong.dto.OrderDTO;
import com.dong.dto.PageDTO;
import com.dong.service.IOrdersService;
import com.dong.vo.AddOrderVO;
import com.dong.vo.GetOrdersVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final IOrdersService ordersService;

    @PostMapping
    public Result<AddOrderVO> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return ordersService.createOrder(orderDTO);
    }

    @GetMapping
    public Result<PageDTO<GetOrdersVO>> getOrder(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status
    ) {
        return ordersService.getOrders(page, pageSize, status);
    }

    @DeleteMapping("/{orderNo}")
    public Result<String> cancelOrder(@PathVariable String orderNo) {
        return ordersService.cancelOrder(orderNo);
    }

    @PostMapping("/{orderNo}/confirm")
    public Result<String> confirmOrder(@PathVariable String orderNo) {
        return ordersService.confirmOrder(orderNo);
    }
}
