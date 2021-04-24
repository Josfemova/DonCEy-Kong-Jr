package cr.ac.tec.ce3104.tc3;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.levels.Level1;
import cr.ac.tec.ce3104.tc3.modes.ControllableMode;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObjectObserver;
import cr.ac.tec.ce3104.tc3.networking.Command;
import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.networking.CommandBatch;

public class Game implements GameObjectObserver {
    public Game(ClientAdmin playerClient) {
        this.playerId = playerClient.getClientId();
        this.runnerThread = new Thread(() -> this.run(), "game" + this.playerId);
        this.runnerThread.start();

        this.player = this.level.setup(this);
        this.attachClient(playerClient);
    }

    @Override
    public synchronized void onObjectModeChanged(GameObject object) {
        this.outputQueue.add(object.makePutCommand());
        this.commit();
    }

    public Integer getPlayerId() {
        return this.playerId;
    }

    public void spawn(GameObject object) {
        synchronized (this) {
            this.gameObjects.put(object.getId(), object);
            this.onObjectModeChanged(object);
        }

        object.addObserver(this);
    }

    public synchronized void onMove(Integer objectId, Position position) {
        GameObject object = this.gameObjects.get(objectId);
        //TODO
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

    public synchronized void detachClient(ClientAdmin client) {
        this.clients.remove(client);
        if (this.clients.isEmpty()) {
            Server.getInstance().removeGame(this.playerId);
        }
    }

    public void onPress(Key key) {
        ControllableMode mode = (ControllableMode)this.player.getMode();
        switch (key) {
            case UP:
                mode.onMoveUp(this.player);
                break;

            case DOWN:
                mode.onMoveDown(this.player);
                break;

            case LEFT:
                mode.onMoveLeft(this.player);
                break;

            case RIGHT:
                mode.onMoveRight(this.player);
                break;

            case JUMP:
                mode.onJump(this.player);
                break;
        }
    }

    public void onRelease() {
        ((ControllableMode)this.player.getMode()).onRelease(this.player);
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
