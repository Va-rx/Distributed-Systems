import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class Technic {

    private static final String KNEE_QUEUE_NAME = "knee_queue";
    private static final String HIP_QUEUE_NAME = "hip_queue";
    private static final String ELBOW_QUEUE_NAME = "elbow_queue";
    private static final String LOG_POST_QUEUE_NAME = "log_post_queue";
    private static final String EXCHANGE_NAME = "logs";

    private static final Set<String> SPECIALIZATIONS = Set.of("KNEE", "HIP", "ELBOW");

    private Channel channel;
    private String[] queueNames;

    public Technic() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("TECHNIC");

        Technic technic = new Technic();
        Channel channel = technic.getChannel();
        String[] specializations = technic.readSpecializationsFromInput();

        channel.basicQos(1);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String logQueueName = channel.queueDeclare().getQueue();

        channel.queueBind(logQueueName, EXCHANGE_NAME, "");
        channel.queueDeclare(LOG_POST_QUEUE_NAME, false, false, false, null);

        technic.makeQueues(specializations);

        technic.receiveTask();
        technic.receiveLogs(logQueueName);
    }
    private void makeQueues(String[] specializations) throws IOException {
        String[] queueNames = new String[2];
        int index = 0;
        for (String specialization : specializations) {
            switch(specialization) {
                case "KNEE":
                    channel.queueDeclare(KNEE_QUEUE_NAME, false, false, false, null);
                    queueNames[index] = KNEE_QUEUE_NAME;
                    index++;
                    break;
                case "HIP":
                    channel.queueDeclare(HIP_QUEUE_NAME, false, false, false, null);
                    queueNames[index] = HIP_QUEUE_NAME;
                    index++;
                    break;
                case "ELBOW":
                    channel.queueDeclare(ELBOW_QUEUE_NAME, false, false, false, null);
                    queueNames[index] = ELBOW_QUEUE_NAME;
                    index++;
                    break;
            }
        }
        setQueueNames(queueNames);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setQueueNames(String[] queueNames) {
        this.queueNames = queueNames;
    }

    public String[] getQueueNames() {
        return queueNames;
    }

    private String[] readSpecializationsFromInput() throws IOException {
        String[] specializations = new String[2];
        int index = 0;
        while(index != 2) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter specialization no. " + (index+1));
            String specialization = br.readLine().toUpperCase();
            if (SPECIALIZATIONS.contains(specialization)) {
                specializations[index] = specialization;
                index++;
            }
            else {
                System.out.println("WRONG SPECIALIZATION");
            }
        }
        return specializations;
    }

    private void receiveLogs(String logQueueName) throws IOException {
        DeliverCallback deliverCallbackLog = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[*]: " + message);
        };
        channel.basicConsume(logQueueName, true, deliverCallbackLog, consumerTag -> { });
    }

    private void receiveTask() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[+] I've received a " + message);

            try {
                message = makeTask(message, delivery);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            resultsPublish(message, delivery);
        };
        channel.basicConsume(queueNames[0], false, deliverCallback, consumerTag -> { });
        channel.basicConsume(queueNames[1], false, deliverCallback, consumerTag -> { });
    }

    private String makeTask(String message, Delivery delivery) throws InterruptedException, IOException {
        Thread.sleep(5000);
        System.out.println("[-] I've finished a task. Sending results to a doctor");
        message += " done";
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        return message;
    }

    private void resultsPublish(String message, Delivery delivery) throws IOException {
        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();

        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, message.getBytes());
        channel.basicPublish("", LOG_POST_QUEUE_NAME, null, message.getBytes());
    }
}
