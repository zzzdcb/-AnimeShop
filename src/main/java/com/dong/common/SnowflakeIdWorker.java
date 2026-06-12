package com.dong.common;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 雪花算法ID生成器（统一管理，避免重复实例）
 */
@Component
public class SnowflakeIdWorker {

    /**
     * 机器ID (0-31)
     */
    @Value("${snowflake.workerId:1}") //读取配置文件
    private long workerId;

    /**
     * 数据中心ID (0-31)
     */
    @Value("${snowflake.datacenterId:1}")
    private long datacenterId;

    private Snowflake snowflake;

    @PostConstruct
    public void init() {
        // 注意：这里一定要使用 getSnowflake 获取单例，不要用 createSnowflake，否则多个实例对象可能生成重复ID [citation:2][citation:5]
        this.snowflake = IdUtil.getSnowflake(workerId, datacenterId);
    }

    /**
     * 获取Long类型ID (数据库主键)
     */
    public long nextId() {
        return snowflake.nextId();
    }

    /**
     * 获取String类型ID (订单号展示)
     */
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}