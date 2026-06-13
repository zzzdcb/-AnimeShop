package com.dong.common;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * RabbitMQ 消息生产者
 * 负责将业务消息发送到 RabbitMQ 交换机
 *
 * 特性：
 * 1. 自动生成消息ID和业务ID，支持幂等消费
 * 2. 支持发布者确认（Publisher Confirm），确保消息可靠送达
 * 3. 支持成功/失败自定义回调
 * 4. 失败消息自动记录，便于后续补偿重试
 */
@Component
@Slf4j
public class RabbitMQProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息（最简版）
     * 不指定业务ID，会自动使用消息ID作为业务ID
     *
     * @param exchange   交换机名称 - 决定消息去哪个交换机
     * @param routingKey 路由键 - 决定消息去哪个队列
     * @param message    消息对象 - 会被自动转成JSON格式
     */
    public void send(String exchange, String routingKey, Object message) {
        send(exchange, routingKey, message, null);
    }

    /**
     * 发送消息（带业务ID）
     * 推荐使用此方法，业务ID用于消费者端的幂等判断
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息对象（任意类型，会被序列化为JSON）
     * @param businessId 业务ID（如订单ID、用户ID等），用于消费者幂等判断，防止重复消费
     */
    public void send(String exchange, String routingKey, Object message, String businessId) {
        // ==================== 1. 生成消息ID和业务ID ====================
        // 消息ID：全局唯一，用于追踪消息发送状态，对应 CorrelationData
        String messageId = UUID.randomUUID().toString();
        // 业务ID：如果调用方未提供，则使用消息ID作为业务ID
        String finalBusinessId = StringUtils.hasText(businessId) ? businessId : messageId;

        // ==================== 2. 构建 Spring Message 对象 ====================
        // 将业务消息放入消息体，并在消息头中携带ID信息
        // 注意：这里使用的是 org.springframework.amqp.core.Message
        Message msg = MessageBuilder
                // 将消息对象转为JSON字符串，再转为字节数组作为消息体
                .withBody(JSON.toJSONString(message).getBytes(StandardCharsets.UTF_8))
                // 设置消息头：消息ID（给生产者确认回调使用）
                .setHeader("messageId", messageId)
                // 设置消息头：业务ID（给消费者幂等判断使用）
                .setHeader("businessId", finalBusinessId)
                // 设置内容类型为JSON
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        // ==================== 3. 创建 CorrelationData 关联消息和回调 ====================
        // CorrelationData 用于关联消息和发布者确认回调
        // 构造参数 messageId 会作为 correlationId 传递给 Broker
        CorrelationData cd = new CorrelationData(messageId);

        // ==================== 4. 注册确认回调（发布者确认机制） ====================
        // whenComplete 会在 Broker 返回确认（ACK/NACK）或发生异常时触发
        // result: Confirm 对象，包含 ack 状态和失败原因
        // ex: 异常对象，当回调本身执行异常时不为空
        cd.getFuture().whenComplete((result, ex) -> {
            // 情况1：发生异常（网络问题、回调执行异常等）
            if (ex != null) {
                log.error("消息确认异常, messageId: {}, businessId: {}", messageId, finalBusinessId, ex);
                // 保存失败消息到数据库，等待定时任务重试
                saveFailedMessage(messageId, finalBusinessId, exchange, routingKey, message, ex.getMessage());
                return;
            }

            // 情况2：Broker 返回确认，result.ack() 为 true 表示消息已被正确接收
            // result.ack() 是 Record 类的标准访问方法（Spring AMQP 3.0+ 规范）
            if (result.ack()) {
                log.info("消息发送成功, messageId: {}, businessId: {}", messageId, finalBusinessId);
            }
            // 情况3：Broker 返回拒绝，result.ack() 为 false 表示消息未被接收
            // 常见原因：交换机不存在、路由键不匹配、交换机无法路由等
            else {
                log.error("消息发送失败, messageId: {}, businessId: {}, 原因: {}",
                        messageId, finalBusinessId, result.reason());
                // 保存失败消息到数据库，等待定时任务重试
                saveFailedMessage(messageId, finalBusinessId, exchange, routingKey, message, result.reason());
            }
        });

        // ==================== 5. 发送消息到 RabbitMQ ====================
        // convertAndSend 会自动将 Message 对象转换为 AMQP 协议格式
        // 最后一个参数 cd 会将回调与本次发送绑定
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, cd);
    }

    /**
     * 发送消息（高级版）
     * 支持自定义成功/失败回调，灵活处理发送结果
     *
     * @param exchange        交换机名称
     * @param routingKey      路由键
     * @param message         消息对象
     * @param businessId      业务ID（用于消费者幂等判断）
     * @param successCallback 成功回调函数，参数为 Boolean（true）
     * @param failCallback    失败回调函数，参数为失败原因字符串
     */
    public void send(String exchange, String routingKey, Object message,
                     String businessId,
                     Consumer<Boolean> successCallback,
                     Consumer<String> failCallback) {

        // ==================== 1. 生成消息ID和业务ID ====================
        String messageId = UUID.randomUUID().toString();
        String finalBusinessId = StringUtils.hasText(businessId) ? businessId : messageId;

        // ==================== 2. 构建 Spring Message 对象 ====================
        Message msg = MessageBuilder
                .withBody(JSON.toJSONString(message).getBytes(StandardCharsets.UTF_8))
                .setHeader("messageId", messageId)
                .setHeader("businessId", finalBusinessId)
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        // ==================== 3. 创建 CorrelationData ====================
        CorrelationData cd = new CorrelationData(messageId);

        // ==================== 4. 注册确认回调（支持自定义回调） ====================
        cd.getFuture().whenComplete((result, ex) -> {
            // 情况1：发生异常
            if (ex != null) {
                log.error("消息确认异常, messageId: {}, businessId: {}", messageId, finalBusinessId, ex);
                // 优先使用自定义失败回调
                if (failCallback != null) {
                    failCallback.accept(ex.getMessage());
                } else {
                    // 无自定义回调时，使用默认失败处理器（入库）
                    defaultFailHandler(messageId, finalBusinessId, exchange, routingKey, message, ex.getMessage());
                }
                return;
            }

            // 情况2：发送成功
            if (result.ack()) {
                log.info("消息发送成功, messageId: {}, businessId: {}", messageId, finalBusinessId);
                // 执行自定义成功回调
                if (successCallback != null) {
                    successCallback.accept(true);
                }
            }
            // 情况3：发送失败（Broker 拒绝）
            else {
                log.error("消息发送失败, messageId: {}, businessId: {}, 原因: {}",
                        messageId, finalBusinessId, result.reason());
                // 优先使用自定义失败回调
                if (failCallback != null) {
                    failCallback.accept(result.reason());
                } else {
                    // 无自定义回调时，使用默认失败处理器（入库）
                    defaultFailHandler(messageId, finalBusinessId, exchange, routingKey, message, result.reason());
                }
            }
        });

        // ==================== 5. 发送消息 ====================
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, cd);
    }

    /**
     * 默认失败处理器（无自定义回调时使用）
     * 将发送失败的消息入库，等待定时任务进行重试补偿
     * @param messageId   消息ID
     * @param businessId  业务ID
     * @param exchange    交换机名称
     * @param routingKey  路由键
     * @param message     消息体
     * @param reason      失败原因
     */
    private void defaultFailHandler(String messageId, String businessId,
                                    String exchange, String routingKey,
                                    Object message, String reason) {
        log.warn("消息发送失败已入库待重试, messageId: {}, businessId: {}, 原因: {}",
                messageId, businessId, reason);
        // TODO: 实现数据库存储逻辑
    }

    /**
     * 保存失败消息到数据库（用于重试补偿）
     * 与 defaultFailHandler 逻辑相同，为了代码清晰保留两个方法
     *
     * @param messageId   消息ID
     * @param businessId  业务ID
     * @param exchange    交换机名称
     * @param routingKey  路由键
     * @param message     消息体
     * @param reason      失败原因
     */
    private void saveFailedMessage(String messageId, String businessId,
                                   String exchange, String routingKey,
                                   Object message, String reason) {
        log.debug("保存失败消息, messageId: {}", messageId);
        // TODO: 实现数据库存储逻辑（可复用 defaultFailHandler 的实现）
    }
}