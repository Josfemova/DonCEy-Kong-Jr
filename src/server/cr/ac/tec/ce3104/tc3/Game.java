package cr.ac.tec.ce3104.tc3;

import cr.ac.tec.ce3104.tc3.gameobjects.Crocodile;
import cr.ac.tec.ce3104.tc3.gameobjects.EnvironmentObject;
import cr.ac.tec.ce3104.tc3.gameobjects.Fruit;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class Game extends Thread{
    
    private Integer playerId; //0,2,3
    private CEList<ClientAdmin> clients; //Observers
    private Integer waterLvl;
    private CEList<EnvironmentObject> environmentObjects;
    private CEList<Fruit> fruits;
    private CEList<Crocodile> crocodiles;
    private PlayerAvatar player;
    public Game(Integer playerId){
        this.playerId = playerId;
    }
    public void attachClient(ClientAdmin client){
        clients.add(client);
    }
    /**
     * Agrega los comandos acumulados al queue de cada cliente suscrito para redibujar el juego en cada cliente
     */
    public void update(){
        //notify observers
        for(ClientAdmin client: clients){
            //TODO
            //
            //client.addCommand();
            System.out.println("updating clients");
        }
    }
    /**
     * Control del loop del juego
     */
    public void run(){

    }
    public Integer getPlayerId(){
        return playerId;
    }
    public void drawScreen1(){
        //Acá es donde va la creación de la escena de juego
        //Puede que convenga llamarla antes de comenzar la ejecución como Thread
    }
}
