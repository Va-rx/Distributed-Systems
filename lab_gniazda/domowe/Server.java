import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {

    static Map<Integer, Socket> clientSockets = new HashMap<>();
    Integer id;
    Socket clientSocket;


    public Server(Integer id, Socket clientSocket) {
        this.id = id;
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            while(true) {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                String msg = in.readLine();
                System.out.println("User" + this.id + ": " + msg);
                for (Map.Entry<Integer, Socket> entry : clientSockets.entrySet()) {
                    Integer id = entry.getKey();
                    Socket clientSocket = entry.getValue();



                    if (!Objects.equals(id, this.id)) {
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println("User" + this.id + ": " + msg);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP SERVER ");
        int portNumber = 9009;
        int id = 0;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("user" + id + " connected");
                clientSockets.put(id, clientSocket);
                (new Server(id, clientSocket)).start();
                id = id + 1;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//        System.out.println("JAVA TCP SERVER");
//        int portNumber = 9009;
//        ServerSocket serverSocket = null;
//
//        ArrayList<Integer> ids = new ArrayList<>();
//        Integer id = 0;
////        Integer newId = ids.getLast() + 1;
//        try {
//            // create socket
//            serverSocket = new ServerSocket(portNumber);
//
//            while(true){
//
//                // accept client
//                Socket clientSocket = serverSocket.accept();
////                ids.add(id, );
//                System.out.println("client connected");
//
//                // in & out streams
//                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//                // read msg, send response
//                //String msg = in.readLine();
//                //System.out.println("received msg: " + msg);
//                out.println("Pong Java Tcp");
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        finally{
//            if (serverSocket != null){
//                serverSocket.close();
//            }
//        }
//    }
}
