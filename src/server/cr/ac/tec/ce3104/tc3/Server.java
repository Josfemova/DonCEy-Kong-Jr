package cr.ac.tec.ce3104.tc3;
import java.io.IOException;
import java.net.ServerSocket;

import cr.ac.tec.ce3104.tc3.util.CEList;

public class Server {
    private ServerSocket serverSocket;
    private CEList<Game> games;
    private static Integer port = 8080;

    private static Server instance;
    private Server() throws IOException{
        serverSocket = new ServerSocket(port);
    }
    public static Server getInstance() throws IOException{
        if(instance == null){
            instance = new Server();
        }
        return instance;
    }
}
