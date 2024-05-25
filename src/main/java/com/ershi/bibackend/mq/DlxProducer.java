package com.ershi.bibackend.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DlxProducer {

    private static final String DLX_EXCHANGE_NAME = "dlx-direct-exchange";

    private static final String EXCHANGE_NAME = "direct-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明死信交换机
            channel.exchangeDeclare(DLX_EXCHANGE_NAME, "direct");
            // 声明死信队列, 并绑定到死信交换机
            String laobanDlxQueueName = "laoban_dlx_queue";
            channel.queueDeclare(laobanDlxQueueName, true, false, false, null);
            channel.queueBind(laobanDlxQueueName, DLX_EXCHANGE_NAME, "laoban_dlx");
            String waibaoDlxQueueName = "waibao_dlx_queue";
            channel.queueDeclare(waibaoDlxQueueName, true, false, false, null);
            channel.queueBind(waibaoDlxQueueName, DLX_EXCHANGE_NAME, "waibao_dlx");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                String[] strings = userInput.split(" ");
                if (strings.length < 1) {
                    continue;
                }
                String message = strings[0];
                String routingKey = strings[1];

                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + " with routing:" + routingKey + "'");
            }
        }
    }

}