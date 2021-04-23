package cr.ac.tec.ce3104.tc3.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.Server;

enum ClientType {
    PLAYER,
    SPECTATOR
}

public class ClientAdmin implements AutoCloseable {
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
        if(this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
    }

    public Integer getClientId() {
        return id;
    }

    public void sendSingle(Command command) {
        try {
            this.commandSender.println(command);
            this.commandSender.flush();
        } catch (Exception exception) {
            this.sendError(exception);
        }
    }

    public void sendBatch(CommandBatch batch) {
        try {
            this.commandSender.print(batch);
            this.commandSender.flush();
        } catch (Exception exception) {
            this.sendError(exception);
        }
    }

    public void sendError(String message) {
        if (this.game != null) {
            this.game = null;

            try {
                this.sendSingle(Command.cmdError(message));
                this.close();
            } catch (Exception exception) {
                exception.printStackTrace();
                this.socket = null;
            }
        }
    }

    public void sendError(Exception exception) {
        exception.printStackTrace();
        this.sendError(exception.toString());
    }

    private static Integer nextClientId = 0;

    private Game game;
    private Socket socket;
    private ClientType type;
    private Integer id;

    private Thread runnerThread;
    private BufferedReader requestReader;
    private PrintWriter commandSender;

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

    private Boolean doHandshake() throws IOException {
        List<Integer> gameIds = Server.getInstance().getGameIds();

        // primer mensaje
        this.sendSingle(Command.cmdWhoAmI(this.id, gameIds));
        // respuesta al init
        Integer gameId = this.receive().expectInt("init");

        //suscribe a un jugador según el id provisto por init
        if (gameId == this.id) {
            this.type = ClientType.PLAYER;
            this.game = Server.getInstance().initPlayer(this);
        } else {
            this.type = ClientType.SPECTATOR;
            this.game = Server.getInstance().getGame(gameId);

            if (game == null) {
                this.sendError("invalid game ID");
                return false;
            }

            this.game.attachClient(this);
        }

        return true;
    }

    private Boolean processNext() throws IOException {
        // inicia loop de cliente
        //Todavía falta gran parte de la lógica acá

        //Procesamiento de entrada de usuario
        //es probable que se quieran administrar comandos de quit de parte del usuario
        if (type == ClientType.SPECTATOR) {
            return true;
        }

        Command request = this.receive();
        System.out.println("received: " + request);
        //TODO

        this.sendError("invalid operation");
        return false;
    }

    private Command receive() throws IOException {
        return new Command(this.requestReader.readLine());
    }
}
