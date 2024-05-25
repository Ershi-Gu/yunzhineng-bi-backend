package com.ershi.bibackend.mq;

import com.rabbitmq.client.*;

public class DirectConsumer {

    private static final String EXCHANGE_NAME = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName1 = "xiaoer_queue";
        channel1.queueDeclare(queueName1, true, false, false, null);
        channel1.queueBind(queueName1, EXCHANGE_NAME, "xiaoer");

        channel2.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName2 = "xiaoshi_queue";
        channel2.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "xiaoshi");

        DeliverCallback xiaoer_deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小二] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback xiaoshi_deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小十] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel1.basicConsume(queueName1, true, xiaoer_deliverCallback, consumerTag -> {
        });
        channel2.basicConsume(queueName2, true, xiaoshi_deliverCallback, consumerTag -> {
        });
    }
}