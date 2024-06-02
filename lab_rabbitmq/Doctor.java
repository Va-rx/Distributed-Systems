import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Doctor {

    private static final String KNEE_QUEUE_NAME = "knee_queue";
    private static final String HIP_QUEUE_NAME = "hip_queue";
    private static final String ELBOW_QUEUE_NAME = "elbow_queue";
    private static final String RESULT_QUEUE_NAME = "result_queue";

    private Channel channel;

    public Doctor() {
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

        channel.queueDeclare(KNEE_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(HIP_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(ELBOW_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(RESULT_QUEUE_NAME, false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[+] I've received results: " + message);
        };
        boolean autoAck = true;
        channel.basicConsume(RESULT_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });


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
        channel.basicPublish("", KNEE_QUEUE_NAME, null, message.getBytes());

    }
    private void hipPublish(String message) throws IOException {
        channel.basicPublish("", HIP_QUEUE_NAME, null, message.getBytes());
    }
    private void elbowPublish(String message) throws IOException {
        channel.basicPublish("", ELBOW_QUEUE_NAME, null, message.getBytes());
    }

    public Channel getChannel() {
        return channel;
    }
}
