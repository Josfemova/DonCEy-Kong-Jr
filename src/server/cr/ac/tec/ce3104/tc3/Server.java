package cr.ac.tec.ce3104.tc3;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.ArrayList;

import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;

public class Server {
    private ServerSocket serverSocket = null;
    private static Integer port = 8080;
    private static List<Game> games = new ArrayList<>();

    private static Server instance;

    private Server() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fatal error: unable to start server");
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

    public List<Integer> getGameIds() {
        List<Integer> gameIds = new ArrayList<>();
        for (Game game : games) {
            gameIds.add(game.getPlayerId());
        }

        return gameIds;
    }

    /**
     * Ejecuta un bucle infinito en búsqueda de nuevos clientes
     */
    public void startUp() {
        // listen for clients
        while (true) {
            try {
                // empieza una nueva conexión con cliente
                new ClientAdmin(serverSocket.accept());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
