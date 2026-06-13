package com.dong.config;

import com.dong.utils.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    //==========声明交换机=========

    // 购物车交换机
    @Bean
    public DirectExchange cartExchange() {
        return new DirectExchange(RabbitMQConstants.CART_EXCHANGE, true, false);
    }
    // 逻辑过期交换机
    @Bean
    public DirectExchange expireExchange() {
        return new DirectExchange(RabbitMQConstants.EXPIRE_EXCHANGE, true, false);
    }

    // 地址交换机
    @Bean
    public DirectExchange addressExchange() {
        return new DirectExchange(RabbitMQConstants.ADDRESS_EXCHANGE, true, false);
    }

    // 秒杀交换机
    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(RabbitMQConstants.SECKILL_EXCHANGE, true, false);
    }

    //首页数据防击穿交换机
    @Bean
    public DirectExchange homeExchange() {
        return new DirectExchange(RabbitMQConstants.HOME_EXCHANGE, true, false);
    }

    //==========声明队列=========

    // 购物车添加/更新队列
    @Bean
    public Queue cartQueue() {
        return new Queue(RabbitMQConstants.CART_QUEUE, true);
    }
    // 购物车删除队列
    @Bean
    public Queue removeCartQueue() {
        return new Queue(RabbitMQConstants.REMOVE_CART_QUEUE, true);
    }

    // 逻辑过期队列
    @Bean
    public Queue expireQueue() {
        return new Queue(RabbitMQConstants.EXPIRE_QUEUE, true);
    }

    // 地址队列
    @Bean
    public Queue addressQueue() {
        return new Queue(RabbitMQConstants.ADDRESS_QUEUE, true);
    }
    // 地址删除队列
    @Bean
    public Queue removeAddressQueue() {
        return new Queue(RabbitMQConstants.REMOVE_ADDRESS_QUEUE, true);
    }

    // 秒杀队列
    @Bean
    public Queue seckillQueue() {
        return new Queue(RabbitMQConstants.SECKILL_QUEUE, true);
    }

   // 首页数据防击穿队列
    @Bean
    public Queue homeQueue() {
        return new Queue(RabbitMQConstants.HOME_QUEUE, true);
    }
    //==========绑定队列和交换机=========

    // 购物车添加/更新队列绑定购物车交换机
    @Bean
    public Binding bindCartQueue(Queue cartQueue, DirectExchange cartExchange) {
        return BindingBuilder.bind(cartQueue)
                .to(cartExchange)
                .with(RabbitMQConstants.CART_ROUTING);
    }
    // 购物车删除队列绑定购物车交换机
    @Bean
    public Binding bindRemoveCartQueue(Queue removeCartQueue, DirectExchange cartExchange) {
        return BindingBuilder.bind(removeCartQueue)
                .to(cartExchange)
                .with(RabbitMQConstants.REMOVE_CART_ROUTING);
    }

    // 逻辑过期队列绑定逻辑过期交换机
    @Bean
    public Binding bindExpireQueue(Queue expireQueue, DirectExchange expireExchange) {
        return BindingBuilder.bind(expireQueue)
                .to(expireExchange)
                .with(RabbitMQConstants.EXPIRE_ROUTING);
    }

    // 地址队列绑定地址交换机
    @Bean
    public Binding bindAddressQueue(Queue addressQueue, DirectExchange addressExchange) {
        return BindingBuilder.bind(addressQueue)
                .to(addressExchange)
                .with(RabbitMQConstants.ADDRESS_ROUTING);
    }
    // 地址删除队列绑定地址交换机
    @Bean
    public Binding bindRemoveAddressQueue(Queue removeAddressQueue, DirectExchange addressExchange) {
        return BindingBuilder.bind(removeAddressQueue)
                .to(addressExchange)
                .with(RabbitMQConstants.REMOVE_ADDRESS_ROUTING);
    }

    // 秒杀队列绑定秒杀交换机
    @Bean
    public Binding bindSeckillQueue(Queue seckillQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue)
                .to(seckillExchange)
                .with(RabbitMQConstants.SECKILL_ROUTING);
    }

    // 首页队列绑定首页交换机
    @Bean
    public Binding bindHomeQueue(Queue homeQueue, DirectExchange homeExchange) {
        return BindingBuilder.bind(homeQueue)
                .to(homeExchange)
                .with(RabbitMQConstants.HOME_ROUTING);
    }
}
