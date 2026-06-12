package com.dong.common;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * RabbitMQ 消费者公共辅助类
 * 封装：幂等检查、ACK/NACK、异常处理、重试策略
 * 注意：Channel 是非线程安全的，调用方必须保证每个消费者线程使用独立的 Channel
 *
 * @author dong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final StringRedisTemplate redisTemplate;

    /**
     * 消费消息（最完整版，支持自定义幂等检查和重试策略）
     *
     * @param businessId         业务ID
     * @param messageId          消息ID
     * @param deliveryTag        投递标签
     * @param channel            Channel（非线程安全，需线程独占）
     * @param idempotentChecker  幂等检查器，返回true表示未处理过
     * @param businessLogic      业务逻辑
     * @param onFailure          失败回调，用于释放幂等锁（可为null）
     * @param retryPolicy        重试策略，返回true表示可重试（为null时使用默认策略）
     */
    public void consume(String businessId,
                        String messageId,
                        long deliveryTag,
                        Channel channel,
                        BooleanSupplier idempotentChecker,
                        Runnable businessLogic,
                        Runnable onFailure,
                        Predicate<Exception> retryPolicy) {

        log.info("收到消息, businessId: {}, messageId: {}", businessId, messageId);

        // 1. 幂等检查
        boolean isFirstTime;
        try {
            isFirstTime = idempotentChecker.getAsBoolean();
        } catch (Exception e) {
            log.error("幂等检查异常, businessId: {}, messageId: {}", businessId, messageId, e);
            nack(channel, deliveryTag, false);
            return;
        }

        // 2. 重复消息，直接确认跳过
        if (!isFirstTime) {
            log.warn("重复消息已跳过, businessId: {}, messageId: {}", businessId, messageId);
            ack(channel, deliveryTag);
            return;
        }

        // 3. 执行业务逻辑
        try {
            businessLogic.run();
            ack(channel, deliveryTag);
            log.info("消费成功, businessId: {}, messageId: {}", businessId, messageId);
        } catch (Exception e) {
            log.error("消费失败, businessId: {}, messageId: {}", businessId, messageId, e);

            // 失败回调（释放幂等锁）
            if (onFailure != null) {
                try {
                    onFailure.run();
                } catch (Exception ex) {
                    log.error("失败回调执行异常, businessId: {}, messageId: {}", businessId, messageId, ex);
                }
            }

            // 判断重试：优先使用自定义策略，否则用默认策略
            boolean retryable = retryPolicy != null ? retryPolicy.test(e) : isRetryable(e);
            nack(channel, deliveryTag, retryable);
        }
    }

    /**
     * 消费消息（向后兼容，使用默认重试策略）
     */
    public void consume(String businessId,
                        String messageId,
                        long deliveryTag,
                        Channel channel,
                        BooleanSupplier idempotentChecker,
                        Runnable businessLogic,
                        Runnable onFailure) {
        consume(businessId, messageId, deliveryTag, channel,
                idempotentChecker, businessLogic, onFailure, null);
    }

    /**
     * 消费消息（使用Redis自动幂等）
     * 业务失败时自动删除幂等锁，允许重试
     *
     * @param businessId     业务ID
     * @param messageId      消息ID
     * @param deliveryTag    投递标签
     * @param channel        Channel
     * @param ttlHours       幂等锁过期时间（小时），必须大于0
     * @param businessLogic  业务逻辑
     */
    public void consumeWithRedisIdempotent(String businessId,
                                           String messageId,
                                           long deliveryTag,
                                           Channel channel,
                                           long ttlHours,
                                           Runnable businessLogic) {

        if (ttlHours <= 0) {
            throw new IllegalArgumentException("ttlHours 必须大于0，当前值: " + ttlHours);
        }

        String idempotentKey = "idempotent:" + businessId + ":" + messageId;

        consume(businessId, messageId, deliveryTag, channel,
                // 幂等检查：Redis SETNX，成功返回true表示第一次处理
                () -> {
                    Boolean success = redisTemplate.opsForValue()
                            .setIfAbsent(idempotentKey, businessId, Duration.ofHours(ttlHours));
                    return Boolean.TRUE.equals(success);
                },
                businessLogic,
                // 失败回调：删除幂等锁（最多重试3次），允许消息重试
                () -> {
                    boolean deleted = false;
                    for (int i = 0; i < 3; i++) {
                        if (Boolean.TRUE.equals(redisTemplate.delete(idempotentKey))) {
                            deleted = true;
                            break;
                        }
                        try {
                            Thread.sleep(100L * (i + 1));
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    if (!deleted) {
                        log.error("幂等锁删除失败，可能导致消息丢失, businessId: {}, messageId: {}",
                                businessId, messageId);
                    }
                }
        );
    }

    /**
     * 消费消息（使用Redis自动幂等，默认24小时过期）
     */
    public void consumeWithRedisIdempotent(String businessId,
                                           String messageId,
                                           long deliveryTag,
                                           Channel channel,
                                           Runnable businessLogic) {
        consumeWithRedisIdempotent(businessId, messageId, deliveryTag, channel, 24, businessLogic);
    }

    /**
     * 消费消息（无幂等检查）
     * 适用于日志、埋点等允许重复消费的场景
     */
    public void consumeWithoutIdempotent(String businessId,
                                         String messageId,
                                         long deliveryTag,
                                         Channel channel,
                                         Runnable businessLogic) {

        log.info("收到消息, businessId: {}, messageId: {}", businessId, messageId);

        try {
            businessLogic.run();
            ack(channel, deliveryTag);
            log.info("消费成功, businessId: {}", businessId);
        } catch (Exception e) {
            log.error("消费失败, businessId: {}, messageId: {}", businessId, messageId, e);
            nack(channel, deliveryTag, isRetryable(e));
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 确认消息
     */
    private void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("ACK失败, deliveryTag: {}", deliveryTag, e);
        }
    }

    /**
     * 拒绝消息
     */
    private void nack(Channel channel, long deliveryTag, boolean requeue) {
        try {
            channel.basicNack(deliveryTag, false, requeue);
        } catch (IOException e) {
            log.error("NACK失败, deliveryTag: {}", deliveryTag, e);
        }
    }

    /**
     * 判断异常是否可重试（默认策略）
     * 可重试：timeout（超时）、connection（连接异常）、deadlock（死锁）等临时性故障
     */
    private boolean isRetryable(Exception e) {
        String message = e.getMessage();
        return message != null && (
                message.contains("timeout") ||
                        message.contains("connection") ||
                        message.contains("deadlock") ||
                        message.contains("Lock wait")
        );
    }
}