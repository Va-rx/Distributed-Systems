import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static Map<Integer, Socket> clientSockets = new HashMap<>();
    private ServerSocket serverSocket;

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Thread UDPconnection = new Thread(new UDPConnection(port));
        UDPconnection.start();
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Thread TCPconnection = new Thread(new TCPConnection(clientSocket));
            TCPconnection.start();
        }
    }

    public static Integer getNumOfClients() {
        return clientSockets.size();
    }

    private static class TCPConnection implements Runnable {
        private final Socket clientSocket;
        private final Integer clientId;
        private static final Lock lock = new ReentrantLock();

        public TCPConnection(Socket socket) {
            this.clientSocket = socket;
            this.clientId = getNumOfClients();
        }

        public void run() {
            clientSockets.put(clientId, this.clientSocket);
            String msg = "[Server] User" + clientId + " has joined the chat.";

            try {
                broadcast(msg);
                while (true) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                    msg = in.readLine();
                    if (msg == null) {
                        lock.lock();
                        clientSockets.remove(clientId);
                        clientSocket.close();
                        lock.unlock();

                        msg = "[Server] User" + clientId + " has disconnected";
                        broadcast(msg);
                        return;
                    }
                    msg = "User" + clientId + ": " + msg;
                    broadcast(msg);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // broadcast sender's msg to other clients
        private void broadcast(String message) throws IOException {
            System.out.println(message);

            for (Map.Entry<Integer, Socket> entry : clientSockets.entrySet()) {
                Socket clientSocket = entry.getValue();

                if (!Objects.equals(clientSocket, this.clientSocket)) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(message);
                }
            }
        }
    }

    private static class UDPConnection implements Runnable {

        private final DatagramSocket ds;
        List<ClientAddress> addresses = new ArrayList<>();


        public UDPConnection(Integer port) throws SocketException {
            this.ds = new DatagramSocket(port);
        }

        public void run() {
            byte[] receiveBuffer = new byte[1024];
            try {
                while (true) {
                    Arrays.fill(receiveBuffer, (byte) 0);
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    this.ds.receive(receivePacket);

                    InetAddress clientAddress = receivePacket.getAddress();
                    Integer clientPort =receivePacket.getPort();
                    ClientAddress ca = new ClientAddress(clientAddress, clientPort);
                    if (!addresses.contains(ca)) {
                        addresses.add(ca);
                        System.out.println("Dodano:" + clientAddress + " " + clientPort);

                    }

                    String msg = new String(receivePacket.getData());
//                    msg = "User" + clientId + ": " + msg;
                    msg = "User unknown: " + msg;
                    broadcast(msg, clientPort, clientAddress);

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void broadcast(String message, Integer sender_port, InetAddress sender_address) throws IOException {
            //System.out.println(message);
            byte[] sendBuffer = message.getBytes();
//            System.out.println("Ponizej lista");
            for (ClientAddress data : addresses) {
//                System.out.println("port" + data.getPort());
//                System.out.println("ip" + data.getIp());
                if(!Objects.equals(sender_port, data.getPort())) {
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, sender_address, data.getPort());
                    System.out.println("Sent to" + data.getPort());
                    ds.send(sendPacket);
                }
            }
//            for (Map.Entry<Integer, Socket> entry : clientSockets.entrySet()) {
//                Socket clientSocket = entry.getValue();
//                Integer userId = entry.getKey();
////                System.out.println(userId + " " + clientSocket.getPort());
////                System.out.println("DS: " + this.ds.getPort());
//
//                if (!Objects.equals(sender_port, clientSocket.getPort())) {
//                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientSocket.getInetAddress(), clientSocket.getPort());
////                    System.out.println("Powinienem wyslac: " + userId);
//                    ds.send(sendPacket);
//                }
//            }
        }
    }

        public static void main(String[] args) throws IOException {
            Server server = new Server();
            server.run(9009);
        }
    }

