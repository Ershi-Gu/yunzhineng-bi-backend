package com.ershi.bibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class TopicConsumer {

    private static final String EXCHANGE_NAME = "topic-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
        Channel channel3 = connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME, "topic");
        String queueName1 = "qianduan_queue";
        channel1.queueDeclare(queueName1, true, false, false, null);
        channel1.queueBind(queueName1, EXCHANGE_NAME, "#.qianduan.#");

        channel2.exchangeDeclare(EXCHANGE_NAME, "topic");
        String queueName2 = "houduan_queue";
        channel2.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "#.houduan.#");

        channel3.exchangeDeclare(EXCHANGE_NAME, "topic");
        String queueName3 = "chanpin_queue";
        channel3.queueDeclare(queueName3, true, false, false, null);
        channel3.queueBind(queueName3, EXCHANGE_NAME, "#.chanpin.#");

        DeliverCallback xiaoa_a_deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小 a] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback xiao_b_deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小 b] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback xiao_c_deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小 c] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel1.basicConsume(queueName1, true, xiaoa_a_deliverCallback, consumerTag -> {
        });
        channel2.basicConsume(queueName2, true, xiao_b_deliverCallback, consumerTag -> {
        });
        channel3.basicConsume(queueName3, true, xiao_c_deliverCallback, consumerTag -> {
        });
    }
}