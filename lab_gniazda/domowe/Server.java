import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {

    static Map<Integer, Socket> clientSockets = new HashMap<>();
    private Integer userId;
    private Socket clientSocket;


    public Server(Integer id, Socket clientSocket) {
        userId = id;
        this.clientSocket = clientSocket;
    }

    public void run() {
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
                    clientSocket.close();
                    clientSockets.remove(userId);
                    msg = "[Server] User" + userId + " has disconnected";
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
        System.out.println("JAVA TCP SERVER ");
        int portNumber = 9009;
        int userId = 0;

        ServerSocket serverSocket = new ServerSocket(portNumber);
        Socket clientSocket = null;
        while(true) {
            try {
                clientSocket = serverSocket.accept();
                userId += 1;
                clientSockets.put(userId, clientSocket);
                (new Server(userId, clientSocket)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
