package cr.ac.tec.ce3104.tc3;

import java.util.HashMap;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.levels.Level1;
import cr.ac.tec.ce3104.tc3.modes.Mode;
import cr.ac.tec.ce3104.tc3.modes.Dying;
import cr.ac.tec.ce3104.tc3.modes.Static;
import cr.ac.tec.ce3104.tc3.modes.ControllableMode;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.Placement;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObjectObserver;
import cr.ac.tec.ce3104.tc3.networking.Command;
import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.networking.CommandBatch;

public class Game implements GameObjectObserver {
    public Game(ClientAdmin playerClient) {
        this.playerId = playerClient.getClientId();
        this.attachClient(playerClient);

        this.log("New game");
        this.reset();
    }

    @Override
    public synchronized void onObjectDeleted(GameObject object) {
        this.outputQueue.add(object.makeDeleteCommand());
        this.gameObjects.remove(object.getId());

        this.commit();
    }

    @Override
    public synchronized void onObjectModeChanged(GameObject object) {
        if (object == this.player) {
            this.updateStats();
        }

        this.outputQueue.add(object.makePutCommand());
        this.commit();
    }

    @Override
    public void log(String message) {
        System.out.println("[GAME" + this.playerId + "] " + message);
    }

    public Integer getPlayerId() {
        return this.playerId;
    }

    public PlayerAvatar getPlayer() {
        return this.player;
    }

    public Integer getDifficulty() {
        return this.difficulty;
    }

    public HashMap<Integer, GameObject> getGameObjects() {
        return this.gameObjects;
    }

    public synchronized void onPlayerLost() {
        this.player.freeze();
        this.log("The player has lost");

        if (--this.lives > 0) {
            this.reset();
        } else {
            this.log("Game over");
            this.syncStats();
            this.commit();
        }
    }

    public synchronized void onPlayerWon() {
        ++this.lives;
        ++this.difficulty;

        this.log("The player has won");
        this.reset();
    }

    public <T extends GameObject> T spawn(T object) {
        synchronized (this) {
            this.gameObjects.put(object.getId(), object);
            this.onObjectModeChanged(object);
        }

        object.addObserver(this);
        return object;
    }

    public void spawn(GameObject[] objects) {
        synchronized (this) {
            for (GameObject object : objects) {
                this.gameObjects.put(object.getId(), object);
                this.onObjectModeChanged(object);
            }
        }

        for (GameObject object : objects) {
            object.addObserver(this);
        }
    }

    public synchronized Boolean wouldHit(GameObject object, Position position) {
        return new Placement(object, position, this.level, this.gameObjects.values()).getHitOrientation() != null;
    }

    public synchronized void onMove(Integer objectId, Position position) {
        GameObject object = this.gameObjects.get(objectId);
        if (object == null) {
            // La entidad probablemente se eliminó recientemente
            return;
        }

        Placement placement = new Placement(object, position, this.level, this.gameObjects.values());

        Orientation hitOrientation = placement.getHitOrientation();
        if (hitOrientation != null) {
            // Se contarresta el movimiento especulativo de los clientes
            object.getMode().onHit(object, hitOrientation);
        } else {
            object.relocate(position);
        }

        GameObject target = placement.getInteractionTarget();
        if (target != null) {
            object.onInteraction(target);
            target.onInteraction(object);
        }

        for (GameObject floating : placement.getTouchedFloatings()) {
            object.onFloatingContact(floating);
        }

        if (placement.inFreeFall()) {
            object.getMode().onFreeFall(object);
        }
    }

    public synchronized void attachClient(ClientAdmin client) {
        Integer maxClients = this.clients.get(this.playerId) != null ? 3 : 2;
        if (this.clients.size() >= maxClients) {
            client.sendError("no more expectators are allowed for this game");
            return;
        }

        this.commit();

        CommandBatch catchUp = new CommandBatch();
        catchUp.add(Command.cmdGameArea(this.level.getGameAreaSize()));

        for (GameObject object : this.gameObjects.values()) {
            catchUp.add(object.makePutCommand());
        }

        client.sendBatch(catchUp);
        this.clients.put(client.getClientId(), client);

        this.log("Client " + client.getClientId() + " has joined");
    }

    public synchronized void detachClient(ClientAdmin client) {
        this.clients.remove(client.getClientId());
        this.log("Client " + client.getClientId() + " has left");

        if (this.clients.isEmpty()) {
            this.log("No clients left; game finalized");
            Server.getInstance().removeGame(this.playerId);
        } else if (client.getClientId() == this.playerId) {
            this.log("Player client has left, freezing all entities...");

            // La partida se detiene inmediatamente si sale el jugador
            for (GameObject object : this.gameObjects.values()) {
                object.freeze();
            }
        }
    }

    public void onPress(Key key) {
        if (this.player.hasLost()) {
            return;
        }

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
        if (!this.player.hasLost()) {
            ((ControllableMode)this.player.getMode()).onRelease(this.player);
        }
    }

    public synchronized void setHighlight(Integer objectId, Boolean highlight) {
        this.outputQueue.add(highlight ? Command.cmdHighlight(objectId) : Command.cmdUnhighlight(objectId));
        this.commit();
    }

    private Level level = new Level1();
    private PlayerAvatar player;
    private HashMap<Integer, GameObject> gameObjects = new HashMap<>();

    private Integer lives = 3;
    private Integer score = 5000;
    private Integer difficulty = 0;

    private Integer playerId;
    private HashMap<Integer, ClientAdmin> clients = new HashMap<>(); //Observers
    private CommandBatch outputQueue = new CommandBatch();

    private void updateStats() {
        if (this.score != this.player.getScore()) {
            this.score = this.player.getScore();
            this.syncStats();
        }

        if (this.player.hasLost()) {
            Mode mode = this.player.getMode();
            if (!(mode instanceof Dying) && !(mode instanceof Static)) {
                for (GameObject other : this.gameObjects.values()) {
                    if (other != this.player) {
                        other.freeze();
                    }
                }

                this.player.switchTo(new Dying(this));
            }
        }
    }

    private synchronized void reset() {
        this.log("Begin level reset");

        for (GameObject object : this.gameObjects.values()) {
            this.outputQueue.add(object.makeDeleteCommand());
        }

        this.gameObjects.clear();
        this.syncStats();
        this.commit();

        this.player = this.level.setup(this, this.score);
    }

    private void syncStats() {
        this.outputQueue.add(Command.cmdStats(this.lives, this.score));
    }

    private void commit() {
        for (ClientAdmin client : this.clients.values()) {
            client.sendBatch(this.outputQueue);
        }

        this.outputQueue.clear();
    }
}
