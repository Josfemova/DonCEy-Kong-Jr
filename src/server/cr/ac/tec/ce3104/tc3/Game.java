package cr.ac.tec.ce3104.tc3;

import java.util.List;
import java.util.ArrayList;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.levels.Level1;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.networking.CommandBatch;

public class Game {
    public Game(Integer playerId) {
        this.playerId = playerId;

        this.runnerThread = new Thread(() -> this.run(), "game" + playerId);
        this.runnerThread.start();
    }

    public synchronized void attachClient(ClientAdmin client) {
        this.commit();
        this.clients.add(client);
    }

    public Integer getPlayerId(){
        return playerId;
    }

    public void drawScreen1(){
        //Acá es donde va la creación de la escena de juego
        //Puede que convenga llamarla antes de comenzar la ejecución como Thread
    }

    private Integer playerId; //0,2,3
    private Integer waterLvl;
    private Level level = new Level1();
    private List<GameObject> gameObjects = new ArrayList<>();

    private Thread runnerThread;
    private List<ClientAdmin> clients = new ArrayList<>(); //Observers
    private CommandBatch outputQueue = new CommandBatch();

    /**
     * Control del loop del juego
     */
    private void run() {
        this.level.setup(this);
    }

    private synchronized void commit() {
        for (ClientAdmin client : this.clients) {
            client.sendBatch(this.outputQueue);
        }

        this.outputQueue.clear();
    }
}
