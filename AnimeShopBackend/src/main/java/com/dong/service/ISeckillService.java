package com.dong.service;

import com.dong.common.Result;
import com.dong.entity.Seckill;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 秒杀活动表 服务类
 * </p>
 *
 * @author author
 * @since 2026-05-03
 */
public interface ISeckillService extends IService<Seckill> {

    Result<String> placeOrder(Long seckillId, Integer quantity);
}
