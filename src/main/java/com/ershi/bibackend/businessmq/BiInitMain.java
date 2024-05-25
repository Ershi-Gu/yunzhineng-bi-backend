package com.ershi.bibackend.businessmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BiInitMain {

    private static final String EXCHANGE_NAME = "bi-exchange";
    private static final String QUEUE_NAME = "bi_queue";
    private static final String ROUTING_KEY = "bi_routingKey";
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
