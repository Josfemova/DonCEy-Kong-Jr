package cr.ac.tec.ce3104.tc3;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;

public class Server {
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
        if (this.games.size() >= 2) {
            player.sendError("the maximum number of active games has been reached");
            return null;
        }

        Game game = new Game(player);
        this.games.put(game.getPlayerId(), game);

        return game;
    }

    /**
     * Busca un juego cuyo jugador sea identificado por playerId
     * @param playerId
     * @param spectator
     * @return
     */
    public Game getGame(Integer gameId) {
        return this.games.get(gameId);
    }

    public void removeGame(Integer gameId) {
        this.games.remove(gameId);
    }

    public List<Integer> getGameIds() {
        return new ArrayList<>(this.games.keySet());
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

    private static Server instance;
    private static final Integer PORT = 8080;

    private ServerSocket serverSocket;
    private HashMap<Integer, Game> games = new HashMap<>();

    private Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fatal error: unable to start server");
            System.exit(-1);
        }
    }

}
