import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends Thread {

    private static Map<Integer, Socket> clientSockets = new HashMap<>();
    private static Lock lock = new ReentrantLock();
    private final Integer userId;
    private final Socket clientSocket;

    public Server(Integer id, Socket clientSocket) {
        this.userId = id;
        this.clientSocket = clientSocket;
    }

    public void run() {
        clientSockets.put(userId, clientSocket);
        String msg = "[Server] User" + userId + " has connected";
        System.out.println(msg);
        try {
            broadcast(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            while(true) {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                msg = in.readLine();
                if(msg == null) {
                    lock.lock();
                    clientSocket.close();
                    clientSockets.remove(userId);
                    msg = "[Server] User" + userId + " has disconnected";
                    lock.unlock();
                    System.out.println(msg);
                    broadcast(msg);
                    return;
                }
                msg = "User" + userId + ": " + msg;
                System.out.println(msg);
                broadcast(msg);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        int portNumber = 9009;
//        tcpConnection(portNumber);
//        udpChannel(portNumber);
        Thread thread1 = new Thread(() -> {
            try {
                Server.tcpConnection(portNumber);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                Server.udpChannel(portNumber);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        thread1.start();
        thread2.start();

    }

    private static void tcpConnection(int port) throws IOException {
        System.out.println("JAVA TCP SERVER");

        ServerSocket serverSocket = new ServerSocket(port);
        int userId = 0;
        while(true) {
            try {
                Socket clientSocket = serverSocket.accept();
                (new Server(userId, clientSocket)).start();
                userId += 1;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void udpChannel(int port) throws IOException {
        System.out.println("JAVA UDP SERVER");
        byte[] receiveBuffer = new byte[1024];
        DatagramSocket socket = new DatagramSocket(port);
        while(true) {
            Arrays.fill(receiveBuffer, (byte)0);
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String msg = new String(receivePacket.getData());
            broadcast(msg);
        }
    }

    private void broadcast(String message) throws IOException {
        for (Map.Entry<Integer, Socket> entry : clientSockets.entrySet()) {
            Integer id = entry.getKey();
            Socket clientSocket = entry.getValue();

            if (!Objects.equals(id, userId)) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
            }
        }
    }
}
