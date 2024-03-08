import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public class ClientAddress {
    private InetAddress ip;
    private Integer port;
    private Socket socket;
    private Integer id;

    public ClientAddress(InetAddress ip, Integer port, Socket socket, Integer id) {
        this.ip = ip;
        this.port = port;
        this.socket = socket;
        this.id = id;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setCs(Socket socket) {
        this.socket = socket;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
