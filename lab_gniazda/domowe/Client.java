import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {

        System.out.println("JAVA TCP CLIENT");
        String hostName = "localhost";
        int portNumber = 9009;
        Socket socket = null;

        String username;
        socket = new Socket(hostName, portNumber);

        Thread thread1 = new Thread(new ReceiveMessage(socket));
        Thread thread2 = new Thread(new SendMessage(socket));

        thread1.start();
        thread2.start();
    }
    static class SendMessage implements Runnable {

        private Socket socket;
        public SendMessage(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                PrintWriter out = null;
                try {
                    out = new PrintWriter(this.socket.getOutputStream(), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Scanner scanner = new Scanner(System.in);
                String message = scanner.nextLine();
                out.println(message);
            }
        }
    }

    static class ReceiveMessage implements Runnable {

        private Socket socket;

        public ReceiveMessage(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            while(true) {
                try {
                    in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String response = null;
                try {
                    response = in.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(response);
            }
        }
    }


}