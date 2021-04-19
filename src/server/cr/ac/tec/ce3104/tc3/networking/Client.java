package cr.ac.tec.ce3104.tc3.networking;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class Client {
    private static Integer ids; 
    private Socket socket;
    private ClientType type;
    private Integer id;

    Client(String ip, Integer port) throws UnknownHostException, IOException{
        socket = new Socket(ip, port);
    }
}
