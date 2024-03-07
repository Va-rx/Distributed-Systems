import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        String hostName = "localhost";
        int portNumber = 9009;

//        tcpConnection(hostName, portNumber);
        Socket socket = new Socket(hostName, portNumber);
        Thread thread1 = new Thread(() -> Client.tcpReceiveMessage(socket));
        thread1.start();


        while(true) {
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();

            if (Objects.equals(message, "/U")) {
                System.out.println("Udp multi-lined message enabled");

                message = "";
                String new_line = null;
                while(!Objects.equals(new_line, "/U")) {
                    new_line = scanner.nextLine();
                    message = message.concat(new_line) + "\n";
                }
                String[] lines = message.split("\\r?\\n");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < lines.length - 1; i++) {
                    sb.append(lines[i]);
                    sb.append("\n");
                }

                // Połącz pozostałe linie
                message = sb.toString();

                try {
                    DatagramSocket ds = null;
                    ds = new DatagramSocket();
                    InetAddress addr = InetAddress.getByName(hostName);
                    byte[] sendBuffer = message.getBytes();
                    DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, addr, portNumber);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                tcpSendMessage(socket, message);
            }
        }
//        udpConnection(hostName, portNumber);

//        Thread thread1 = new Thread(() -> {
//            try {
//                Client.tcpConnection(hostName, portNumber);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        Thread thread2 = new Thread(() -> {
//            try {
//                Client.udpConnection(hostName, portNumber);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        thread1.start();
//        thread2.start();
    }


    private static void tcpConnection(String host, int port) throws IOException {
        System.out.println("JAVA TCP CLIENT");

        Socket socket = new Socket(host, port);

        Thread thread1 = new Thread(() -> Client.tcpReceiveMessage(socket));
        //Thread thread2 = new Thread(() -> Client.tcpSendMessage(socket, host, port));

        thread1.start();
        //thread2.start();
    }

    private static void tcpReceiveMessage(Socket socket) {
        String response = null;
        while(true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = in.readLine();
                if (response == null) {
                    System.out.println("Server has disconnected");
                    System.exit(0);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(response);
        }
    }

    private static void tcpSendMessage(Socket socket, String message) throws IOException {
        PrintWriter out = null;
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(message);
//        while (true) {
////            try {
////                out = new PrintWriter(socket.getOutputStream(), true);
////            } catch (IOException e) {
////                throw new RuntimeException(e);
////            }
////            Scanner scanner = new Scanner(System.in);
////            String message = scanner.nextLine();out.println(message);
//        }
    }
}