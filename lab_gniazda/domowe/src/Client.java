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

        Socket socket = new Socket(hostName, portNumber);
        DatagramSocket ds = new DatagramSocket();


        givePort(ds, hostName, portNumber);
//        tcpConnection(hostName, portNumber);

        Thread thread1 = new Thread(() -> Client.tcpReceiveMessage(socket));
        Thread thread2 = new Thread(() -> {
            try {
                Client.udpReceiveMessage(ds);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread1.start();
        thread2.start();

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
                    //DatagramSocket ds = null;
                    InetAddress addr = InetAddress.getByName(hostName);
                    byte[] sendBuffer = message.getBytes();
                    DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, addr, portNumber);
                    System.out.println("Na tym wysyłam udp" + ds.getLocalPort() + ds.getLocalAddress());
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
        System.out.println("Na tym nasłuchuje tcp" + socket.getLocalPort() + socket.getLocalAddress());
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

    private static void givePort(DatagramSocket ds, String hostName, int portNumber) throws IOException {
        InetAddress addr = InetAddress.getByName(hostName);
        byte[] sendBuffer = "".getBytes();
        DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, addr, portNumber);
        ds.send(dp);
    }

    private static void udpReceiveMessage(DatagramSocket socket) throws IOException {
        byte[] receiveBuffer = new byte[1024];
        while(true) {
        Arrays.fill(receiveBuffer, (byte)0);
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(receivePacket);
        String msg = new String(receivePacket.getData());
        System.out.println("received msg: " + msg);}
    }
}