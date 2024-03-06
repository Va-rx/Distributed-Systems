import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        String hostName = "localhost";
        int portNumber = 9009;

//        tcpConnection(hostName, portNumber);
        udpConnection(hostName, portNumber);

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
        Thread thread2 = new Thread(() -> Client.tcpSendMessage(socket));

        thread1.start();
        thread2.start();
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

    private static void tcpSendMessage(Socket socket) {
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

    private static void udpConnection(String host, int port) throws Exception{
        System.out.println("JAVA UDP CLIENT");

        Thread thread1 = new Thread(() -> Client.udpReceiveMessage(host, port));
        Thread thread2 = new Thread(() -> Client.udpSendMessage(host, port));

        thread1.start();
        thread2.start();
    }

    private static void udpReceiveMessage(String host, int port) {
        DatagramSocket ds = null;
        byte[] buff = new byte[1024];
        Arrays.fill(buff, (byte)0);
        try {
            ds = new DatagramSocket();
            while(true) {
                DatagramPacket dp = new DatagramPacket(buff, buff.length);
                ds.receive(dp);
                String response = new String(dp.getData());
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void udpSendMessage(String host, int port) {
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            InetAddress addr = InetAddress.getByName(host);
            while(true) {
                Scanner scanner = new Scanner(System.in);
                byte[] message = scanner.nextLine().getBytes();
                DatagramPacket dp = new DatagramPacket(message, message.length, addr, port);
                ds.send(dp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}