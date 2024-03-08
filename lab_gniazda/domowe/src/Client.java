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
        int portMulti = 9999;

        Socket socket = new Socket(hostName, portNumber);

        DatagramSocket ds = new DatagramSocket(socket.getLocalPort());

        InetAddress group = InetAddress.getByName("228.5.6.7");
        MulticastSocket ms = new MulticastSocket(portMulti);
        ms.joinGroup(group);

        Thread thread1 = new Thread(() -> Client.tcpReceiveMessage(socket));
        Thread thread2 = new Thread(() -> Client.udpReceiveMessage(ds));
        Thread thread3 = new Thread(() -> Client.multicastReceiveMessage(ms));
        thread1.start();
        thread2.start();
        thread3.start();

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
                message = sb.toString();

                try {
                    InetAddress addr = InetAddress.getByName(hostName);
                    byte[] sendBuffer = message.getBytes();
                    DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, addr, portNumber);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(Objects.equals(message, "/M")) {
                System.out.println("Udp multicast multi-lined message enabled");

                message = "";
                String new_line = null;
                while(!Objects.equals(new_line, "/M")) {
                    new_line = scanner.nextLine();
                    message = message.concat(new_line) + "\n";
                }
                String[] lines = message.split("\\r?\\n");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < lines.length - 1; i++) {
                    sb.append(lines[i]);
                    sb.append("\n");
                }
                message = sb.toString();

                DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), group, portMulti);
                ms.leaveGroup(group);
                ms.send(dp);
                ms.joinGroup(group);
            }
            else {
                tcpSendMessage(socket, message);
            }
        }
    }

    private Socket establishTcpConnection(String hostname, int port) throws IOException {


        return new Socket(hostname, port);
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
    }

    private static void udpReceiveMessage(DatagramSocket socket){
        byte[] receiveBuffer = new byte[1024];
        try {
            while (true) {
                Arrays.fill(receiveBuffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void multicastReceiveMessage(MulticastSocket socket) {
        byte[] receiveBuffer = new byte[1024];
        try {
            while (true) {
                Arrays.fill(receiveBuffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                System.out.println("from multicast: " + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}