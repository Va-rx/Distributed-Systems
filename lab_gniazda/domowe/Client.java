import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static Socket socket;

    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP CLIENT");
        String hostName = "localhost";
        int portNumber = 9009;

        socket = new Socket(hostName, portNumber);

        Thread thread1 = new Thread(Client::receiveMessage);
        Thread thread2 = new Thread(Client::sendMessage);

        thread1.start();
        thread2.start();
    }

    private static void receiveMessage() {
        String response = null;
        while(true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(response);
        }
    }

    private static void sendMessage() {
        PrintWriter out = null;
        while (true) {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();
            out.println(message);
        }
    }
}