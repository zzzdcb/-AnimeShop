package com.dong.schedTask;

import com.dong.entity.Seckill;
import com.dong.service.ISeckillService;
import com.dong.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeckillWarmTask {

    private final StringRedisTemplate stringRedisTemplate;
    private final ISeckillService seckillService;

    /**
     * 秒杀商品预热，活动开启的5分钟内，将库存预热到Redis中
     */
    @Scheduled(cron = "${task.seckill.order_warm_cron}")
    public void seckillWarm() {
        log.info("开始执行秒杀商品预热");
        // 1. 获取所有秒杀商品
        LocalDateTime now = LocalDateTime.now();
        List<Seckill> seckillList = seckillService.lambdaQuery()
                .le(Seckill::getStartTime, now.plusMinutes(5))
                .ge(Seckill::getEndTime, now)
                .list();
        if (seckillList.isEmpty()) return;
        // 2. 预热秒杀商品库存
        for (Seckill seckill : seckillList) {
            String stockKey = RedisConstants.SECKILL_STOCK_KEY + seckill.getId();
            // 检查是否已预热，避免重复设置
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
                log.info("库存已预热，跳过: id={}", seckill.getId());
                continue;
            }
            long stockKeyTTL = Duration.between(now, seckill.getEndTime()).getSeconds();
            stringRedisTemplate.opsForValue().set(
                    stockKey,
                    String.valueOf(seckill.getStock()),
                    stockKeyTTL, TimeUnit.SECONDS);
            log.info("预热库存: id={}, stock={}, 有效期={}秒",
                    seckill.getId(), seckill.getStock(), stockKeyTTL);
        }
    }
}
