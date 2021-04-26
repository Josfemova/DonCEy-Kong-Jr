package cr.ac.tec.ce3104.tc3;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;

public class Server {
    /**
     * Obtiene la instancia activa del servidor. Si el servidor no ha sido inicializado anteriormente, entonces lo inicializa
     * @return Referencia a instancia unica del servidor
     */
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }

        return instance;
    }

    /**
     * Inicializa un nuevo juego con el cliente dado como el cliente jugador
     * @param player cliente a ser registrado como el cliente jugador de la partida a crear
     * @return Juego con el cliente dado como jugador
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
     * @param playerId identificador del cliente jugador del juego
     * @return juego cuyo jugador tiene el id dado
     */
    public Game getGame(Integer gameId) {
        return this.games.get(gameId);
    }
    /**
     * Elimina un juego del servidor
     * @param gameId identificador del juego a eliminarse
     */
    public void removeGame(Integer gameId) {
        this.games.remove(gameId);
    }
    /**
     * Obtiene la lista de los id's que identifican las partidas activas
     * @return lista de ids de partidas activas
     */
    public List<Integer> getGameIds() {
        return new ArrayList<>(this.games.keySet());
    }

    /**
     * Ejecuta un bucle infinito en búsqueda de nuevos clientes
     */
    public void startUp() {
        try {
            this.adminWindow = new Admin(System.out);

            PrintStream fakeStdout = new PrintStream(adminWindow.getOutputStream());
            System.setOut(fakeStdout);
            System.setErr(fakeStdout);

            System.out.println("[SERVR] Listening on 127.0.0.1:" + PORT + "...");

            // listen for clients
            while (true) {
                // empieza una nueva conexión con cliente
                Socket socket = serverSocket.accept();
                System.out.println("[SERVR] Accepted connection from " + socket.getRemoteSocketAddress());
                new ClientAdmin(socket);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static Server instance;
    private static final Integer PORT = 8080;

    private ServerSocket serverSocket;
    private HashMap<Integer, Game> games = new HashMap<>();
    private Admin adminWindow;

    /**
     * Constructor privado ya que la clase es un Singleton
     */
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
