package cr.ac.tec.ce3104.tc3;

import java.io.IOException;
import java.net.ServerSocket;

import org.json.simple.JSONObject;

import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class Server {
    private ServerSocket serverSocket = null;
    private static Integer port = 8080;
    private static CEList<Game> games = new CEList<>();

    private static Server instance;

    private Server() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fatal Error: Unable to Start Server");
            System.exit(-1);
        }
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }
    /**
     * Inicializa un nuevo juego (Hilo) y suscribe al jugador a las actualizaciones del servidor 
     * @param player
     * @return
     */
    public Game initPlayer(ClientAdmin player) {
        Game game = new Game(player.getClientId());
        game.attachClient(player);
        game.start();
        games.add(game);
        return game;
    }
    /**
     * Busca un juego cuyo jugador sea identificado por playerId, y suscribe al cliente dado a las actualizaciones del servidor
     * @param playerId
     * @param spectator
     * @return
     */
    public Game initSpectator(Integer playerId, ClientAdmin spectator) {
        Game game = null;
        for (Game x : games) {
            if (x.getPlayerId() == playerId) {
                game = x;
                break;
            }
        }
        game.attachClient(spectator);
        return game;
    }

    public CEList<Integer> getGameIds() {
        CEList<Integer> gameIds = new CEList<>();
        for (Game game : games) {
            gameIds.add(game.getPlayerId());
        }
        return gameIds;
    }

    public void updateGame(JSONObject cmd, Integer Id) {

    }
    /**
     * Ejecuta un bucle infinito en búsqueda de nuevos clientes
     */
    public void StartUp() {
        // startup command interpreter
        CmdInterpreter interpreter = new CmdInterpreter();
        interpreter.start();

        // listen for clients
        while (true) {
            try {
                // empieza una nueva conexión con cliente
                new ClientAdmin(serverSocket.accept()).start();

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
