import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        String hostName = "localhost";
        int portNumber = 9009;
        int portMulti = 9999;
        client.run(hostName, portNumber, portMulti);
    }

    public void run(String host, int portUni, int portMulti) throws IOException {
        Socket tcpSocket = tcpEstablishConnection(host, portUni);
        DatagramSocket udpSocket = udpEstablishConnection(tcpSocket.getLocalPort());

        InetAddress group = InetAddress.getByName("228.5.6.7");
        MulticastSocket multiSocket = multiEstablishConnection(group, portMulti);


        while (true) {
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();

            if (Objects.equals(message, "/U")) {
                message = getMultilineMsgToSend("/U");
                udpSendMessage(udpSocket, message, portUni);
            } else if (Objects.equals(message, "/M")) {
                message = getMultilineMsgToSend("/M");
                multiSendMessage(multiSocket, group, portMulti, message);
            } else {
                tcpSendMessage(tcpSocket, message);
            }
        }
    }

    public Socket tcpEstablishConnection(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        Thread thread = new Thread(() -> Client.tcpReceiveMessage(socket));
        thread.start();
        return socket;
    }

    private static void tcpReceiveMessage(Socket socket) {
        String response = null;
        while (true) {
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

    public void tcpSendMessage(Socket socket, String message) throws IOException {
        PrintWriter out = null;
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(message);
    }

    public DatagramSocket udpEstablishConnection(int port) throws SocketException {
        DatagramSocket socket = new DatagramSocket(port);
        Thread thread = new Thread(() -> Client.udpReceiveMessage(socket));
        thread.start();
        return socket;
    }

    private static void udpReceiveMessage(DatagramSocket socket) {
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

    public void udpSendMessage(DatagramSocket socket, String message, int portUni) throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName("localhost"), portUni);
        socket.send(dp);
    }

    public MulticastSocket multiEstablishConnection(InetAddress group, int port) throws IOException {
        MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(group);
        Thread thread = new Thread(() -> Client.multiReceiveMessage(socket));
        thread.start();
        return socket;
    }

    private static void multiReceiveMessage(MulticastSocket socket) {
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

    public void multiSendMessage(MulticastSocket socket, InetAddress group, int portMulti, String message) throws IOException {
        DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), group, portMulti);
        socket.leaveGroup(group);
        socket.send(dp);
        socket.joinGroup(group);
    }

    private String getMultilineMsgToSend(String symbol) {
        Scanner scanner = new Scanner(System.in);
        String message = "";
        String new_line = null;
        while (!Objects.equals(new_line, symbol)) {
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
        return message;
    }
}




