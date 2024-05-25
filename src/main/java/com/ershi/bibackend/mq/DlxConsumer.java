package com.ershi.bibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxConsumer {

    private static final String DLX_EXCHANGE_NAME = "dlx-direct-exchange";

    private static final String EXCHANGE_NAME = "direct-exchange";


    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 声明队列名
        final String QUEUE_NAME = "dlx_test_queue";

        // 指定队列消息过期参数
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 5000);
        // 指定死信交换机
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        // 指定死信路由键
        args.put("x-dead-letter-routing-key", "laoban_dlx");

        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        // 绑定队列到业务交换机
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "test");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        };

        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
    }
}