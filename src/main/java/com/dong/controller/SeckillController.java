package com.dong.controller;


import com.dong.common.Result;
import com.dong.service.ISeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 秒杀活动表 前端控制器
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final ISeckillService seckillService;
    @PostMapping("/{seckillId}/place")
    public Result<String> placeOrder(@PathVariable Long seckillId,
                                     @RequestParam Integer quantity) {
        return seckillService.placeOrder(seckillId, quantity);
    }
}
