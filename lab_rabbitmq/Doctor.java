import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.UUID;


public class Doctor {

    private final String privateQueueName;

    private static final String KNEE_QUEUE_NAME = "knee_queue";
    private static final String HIP_QUEUE_NAME = "hip_queue";
    private static final String ELBOW_QUEUE_NAME = "elbow_queue";
    private static final String LOG_POST_QUEUE_NAME = "log_post_queue";
    private static final String EXCHANGE_NAME = "logs";

    private Channel channel;

    public Doctor() {
        String id = UUID.randomUUID().toString();
        privateQueueName = "doctor_queue_" + id;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println("DOCTOR");

        Doctor doctor = new Doctor();
        Channel channel = doctor.getChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String logQueueName = channel.queueDeclare().getQueue();
        channel.queueBind(logQueueName, EXCHANGE_NAME, "");

        channel.queueDeclare(KNEE_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(HIP_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(ELBOW_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(LOG_POST_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(doctor.privateQueueName, false, false, false, null);

        doctor.receiveResults();
        doctor.receiveLogs(logQueueName);

        while(true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter [task] [surname]: ");
            String message = br.readLine();
            if (message.equals("exit")) {
                break;
            }
            try {
                doctor.determineTask(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void determineTask(String input) throws IOException {
        String[] extract = input.split(" ");
        if (extract.length != 2) {
            System.out.println("Wrong number of params");
            return;
        }
        String task = extract[0].toUpperCase();
        String surname = extract[1];
        switch(task) {
            case "KNEE":
                kneePublish(task + " " + surname);
                break;
            case "HIP":
                hipPublish(task + " " + surname);
                break;
            case "ELBOW":
                elbowPublish(task + " " + surname);
                break;
            default:
                System.out.println("Wrong task");
                break;
        }
    }

    private void kneePublish(String message) throws IOException {
        AMQP.BasicProperties props = prepareProperties(privateQueueName);
        channel.basicPublish("", KNEE_QUEUE_NAME, props, message.getBytes());
        channel.basicPublish("", LOG_POST_QUEUE_NAME, null, message.getBytes());
    }
    private void hipPublish(String message) throws IOException {
        AMQP.BasicProperties props = prepareProperties(privateQueueName);
        channel.basicPublish("", HIP_QUEUE_NAME, props, message.getBytes());
        channel.basicPublish("", LOG_POST_QUEUE_NAME, null, message.getBytes());
    }
    private void elbowPublish(String message) throws IOException {
        AMQP.BasicProperties props = prepareProperties(privateQueueName);
        channel.basicPublish("", ELBOW_QUEUE_NAME, props, message.getBytes());
        channel.basicPublish("", LOG_POST_QUEUE_NAME, null, message.getBytes());
    }

    private void receiveResults() throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[+] I've received results: " + message);
        };
        channel.basicConsume(privateQueueName, true, deliverCallback, consumerTag -> { });
    }
    private void receiveLogs(String logQueueName) throws IOException {
        DeliverCallback deliverCallbackLog = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[*]: " + message);
        };
        channel.basicConsume(logQueueName, true, deliverCallbackLog, consumerTag -> { });
    }

    private static AMQP.BasicProperties prepareProperties(String privateQueueName) {
        return new AMQP.BasicProperties
                .Builder()
                .replyTo(privateQueueName)
                .build();
    }

    public Channel getChannel() {
        return channel;
    }
}
