import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Admin {
    private static final String EXCHANGE_NAME = "logs";
    private static final String LOG_POST_QUEUE_NAME = "log_post_queue";


    public static void main(String[] args) throws Exception{
        System.out.println("ADMIN");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.queueDeclare(LOG_POST_QUEUE_NAME, false, false, false, null);

        DeliverCallback deliverCallbackLog = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[*]: " + message);
        };
        channel.basicConsume(LOG_POST_QUEUE_NAME, true, deliverCallbackLog, consumerTag -> { });

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Enter message to broadcast:");
            String message = br.readLine();
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("[-] Sent '" + message + "'");
        }
    }
}
