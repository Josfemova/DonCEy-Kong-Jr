package cr.ac.tec.ce3104.tc3;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.levels.Level1;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.networking.Command;
import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.networking.CommandBatch;

public class Game {
    public Game(ClientAdmin playerClient) {
        this.playerId = playerClient.getClientId();
        this.runnerThread = new Thread(() -> this.run(), "game" + this.playerId);
        this.runnerThread.start();

        this.player = this.level.setup(this);
        this.attachClient(playerClient);
    }

    public Integer getPlayerId() {
        return this.playerId;
    }

    public synchronized void spawn(GameObject object) {
        this.gameObjects.put(object.getId(), object);
        this.outputQueue.add(object.makePutCommand());
    }

    public synchronized void attachClient(ClientAdmin client) {
        this.commit();

        CommandBatch catchUp = new CommandBatch();
        catchUp.add(Command.cmdGameArea(this.level.getGameAreaSize()));

        for (GameObject object : this.gameObjects.values()) {
            catchUp.add(object.makePutCommand());
        }

        client.sendBatch(catchUp);
        this.clients.add(client);
    }

    private Level level = new Level1();
    private PlayerAvatar player;
    private HashMap<Integer, GameObject> gameObjects = new HashMap<>();

    private Integer playerId;
    private Thread runnerThread;
    private List<ClientAdmin> clients = new ArrayList<>(); //Observers
    private CommandBatch outputQueue = new CommandBatch();

    /**
     * Control del loop del juego
     */
    private void run() {
    }

    private synchronized void commit() {
        for (ClientAdmin client : this.clients) {
            client.sendBatch(this.outputQueue);
        }

        this.outputQueue.clear();
    }
}
