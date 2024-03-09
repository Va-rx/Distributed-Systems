import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.rmi.ServerError;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static final List<ClientAddress> addresses = new ArrayList<>();

    public void run(int port) throws IOException {
        ServerSocket serverSocket = null;
        Thread udpConnection = null;

        int clientId = 0;
        try {
            serverSocket = new ServerSocket(port);
            udpConnection = new Thread(new UDPConnection(port));
            udpConnection.start();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread tcpConnection = new Thread(new TCPConnection(clientSocket, clientId));
                tcpConnection.start();
                clientId += 1;
                if (clientId == Integer.MIN_VALUE) {
                    System.out.println("Max amounts of clients is reached.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    private static class TCPConnection implements Runnable {
        ClientAddress client;
        private static final Lock lock = new ReentrantLock();

        public TCPConnection(Socket socket, Integer id) {
            this.client = new ClientAddress(socket.getInetAddress(), socket.getPort(), socket, id);
        }

        public void run() {
            if (!addresses.contains(client)) {
                addresses.add(client);
                String msg = "[Server] User" + client.getId() + " has joined the chat.";
                broadcast(msg);
            }

            try {
                while (true) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getSocket().getInputStream()));
                    String msg = in.readLine();
                    if (msg == null) {
                        lock.lock();
                        addresses.remove(this.client);
                        this.client.getSocket().close();
                        lock.unlock();

                        msg = "[Server] User" + this.client.getId() + " has disconnected";
                        broadcast(msg);
                        return;
                    }
                    msg = "User" + this.client.getId() + ": " + msg;
                    broadcast(msg);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // broadcast sender's msg to other clients
        private void broadcast(String message) {
            System.out.println(message);
            try {
                for (ClientAddress data : addresses) {
                    if (!Objects.equals(this.client, data)) {
                        PrintWriter out = new PrintWriter(data.getSocket().getOutputStream(), true);
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class UDPConnection implements Runnable {
        private final DatagramSocket ds;

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

                    InetAddress clientIp = receivePacket.getAddress();
                    Integer clientPort = receivePacket.getPort();
                    Integer clientId = null;
                    for (ClientAddress data : addresses) {
                        if (clientIp.equals(data.getIp()) && clientPort.equals(data.getPort())) {
                            clientId = data.getId();
                        }
                    }

                    String msg = new String(receivePacket.getData());
                    msg = "User" + clientId + ": " + msg;
                    broadcast(msg, clientId);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                this.ds.close();
            }
        }

        private void broadcast(String message, Integer clientId) {
            System.out.println(message);
            try {
                byte[] sendBuffer = message.getBytes();
                for (ClientAddress data : addresses) {
                    if (!Objects.equals(clientId, data.getId())) {
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, data.getIp(), data.getPort());
                        ds.send(sendPacket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
            Server server = new Server();
            try {
                server.run(9009);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

