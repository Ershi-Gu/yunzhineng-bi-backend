package com.ershi.bibackend.businessmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 * @author Ershi
 * @date 2024/05/25
 */
public class TestInitMain {

    private static final String EXCHANGE_NAME = "test-exchange";
    private static final String QUEUE_NAME = "test_queue";
    private static final String ROUTING_KEY = "test";
    private static final String VIRTUAL_HOST = "ershi-bi"; // 指定虚拟主机

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guershi");
        factory.setPassword("guershi");
        factory.setVirtualHost(VIRTUAL_HOST); // 指定虚拟主机

        try {
            // 创建连接和通道
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // 创建交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            // 绑定队列到交换机
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
