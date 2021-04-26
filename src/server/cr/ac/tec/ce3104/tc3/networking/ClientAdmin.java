package cr.ac.tec.ce3104.tc3.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import cr.ac.tec.ce3104.tc3.Key;
import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.Server;
import cr.ac.tec.ce3104.tc3.physics.Position;

enum ClientType {
    PLAYER,
    SPECTATOR;
}

public class ClientAdmin implements AutoCloseable {
    /**
     * Inicia una nueva instancia de la clase para administrar una conexion con un cliente
     * @param socket informacion de conexion con el cliente
     * @throws IOException error que puede surgir si hay problemas al leer el stream del socket
     */
    public ClientAdmin(Socket socket) throws IOException {
        this.socket = socket;
        this.id = nextClientId++;

        this.requestReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        //por alguna razón esto sirve pero DataOutputStream no
        this.commandSender = new PrintWriter(this.socket.getOutputStream(), true);

        this.runnerThread = new Thread(() -> this.run());
        this.runnerThread.start();
    }

    @Override
    public void close() throws IOException {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }

        if (this.game != null) {
            this.game.detachClient(this);
            this.game = null;
        }
    }
    /**
     * Obtiene el id del cliente que administra esta instania
     * @return id del cliente que administra la instancia actual
     */
    public Integer getClientId() {
        return id;
    }
    /**
     * Envia un solo comando al cliente
     * @param command comando a enviarse al cliente
     */
    public void sendSingle(Command command) {
        try {
            this.commandSender.println(command);
            this.commandSender.flush();
        } catch (Exception exception) {
            this.sendError(exception);
        }
    }
    /**
     * Envia un conjunto de comandos al cliente
     * @param batch collecion de comandos a ser enviados al cliente
     */
    public void sendBatch(CommandBatch batch) {
        try {
            this.commandSender.print(batch);
            this.commandSender.flush();
        } catch (Exception exception) {
            this.sendError(exception);
        }
    }
    /**
     * Le comunica al cliente que se ha enontrado un error durante la ejecucion
     * @param message descripcion del error encontrado
     */
    public void sendError(String message) {
        try {
            this.sendSingle(Command.cmdError(message));
            this.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            this.socket = null;
        }
    }
    /**
     * Envia una excepcion enontrada a stderr
     * @param exception excepcion a enviar a la salida estandar de error
     */
    public void sendError(Exception exception) {
        exception.printStackTrace();
        this.sendError(exception.toString());
    }

    private static Integer nextClientId = 0;

    private Game game;
    private Socket socket;
    private Integer id;
    private ClientType type;
    private Key lastKey = null;

    private Thread runnerThread;
    private BufferedReader requestReader;
    private PrintWriter commandSender;

    /**
     * Rutina de inicio del hilo del cliente
     */
    private void run() {
        try (this) {
            try {
                if (this.doHandshake())    {
                    while (this.processNext()) {
                        continue;
                    }
                }
            } catch (Exception exception) {
                   exception.printStackTrace();
                this.sendError(exception);
            }
        } catch (Exception nested) {
            nested.printStackTrace();
        }
    }
    /**
     * Procesa el mensaje de hanshake enviado por el cliente
     * @return Estado del procesamiento del hanshake con el cliente
     * @throws IOException error que puede surgir al haber problemas con leer el stream del socket
     */
    private Boolean doHandshake() throws IOException {
        List<Integer> gameIds = Server.getInstance().getGameIds();

        // primer mensaje
        this.sendSingle(Command.cmdWhoAmI(this.id, gameIds));
        // respuesta al init
        Integer gameId = this.receive().expectInt("init");
        if (gameId == null) {
            return false;
        }

        //suscribe a un jugador según el id provisto por init
        if (gameId == this.id) {
            this.type = ClientType.PLAYER;
            this.game = Server.getInstance().initPlayer(this);
        } else {
            this.type = ClientType.SPECTATOR;
            this.game = Server.getInstance().getGame(gameId);

            if (this.game == null) {
                this.sendError("invalid game ID");
            } else {
                this.game.attachClient(this);
            }
        }

        return this.game != null;
    }
    /**
     * Procesa el siguiente comando pendiente obtenido del cliente (manejo del input del usuario)
     * @return Booleano que indica estado en que finaliza el procesamiento
     * @throws IOException error que surge al haber problemas en lectura del stream del socket
     */
    private Boolean processNext() throws IOException {
        // inicia loop de cliente
        Command request = this.receive();

        //se quieren administrar comandos de quit para los dos tipos de cliente
        String operation = request.expectString("op");
        if (operation.equals("bye")) {
            return false;
        } else if (type == ClientType.SPECTATOR) {
            return true;
        }

        switch (operation) {
            case "press":
                this.lastKey = Key.parse(request.expectString("key"));
                this.game.onPress(this.lastKey);

                break;

            case "release":
                if (this.lastKey == Key.parse(request.expectString("key"))) {
                    this.game.onRelease();
                    this.lastKey = null;
                }

                break;

            case "move":
                Position position = new Position(request.expectInt("x"), request.expectInt("y"));
                this.game.onMove(request.expectInt("id"), position);

                break;

            default:
                this.sendError("invalid operation: " + operation);
                return false;
        }

        return true;
    }
    /**
     * Lee un comando del stream del socket
     * @return Comando construido desde el stream del socket
     * @throws IOException error que surge al haber un problema  al leer del socket
     */
    private Command receive() throws IOException {
        return new Command(this.requestReader.readLine());
    }
}
