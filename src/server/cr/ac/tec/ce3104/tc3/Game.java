package cr.ac.tec.ce3104.tc3;

import cr.ac.tec.ce3104.tc3.gameobjects.Crocodile;
import cr.ac.tec.ce3104.tc3.gameobjects.EnvironmentObject;
import cr.ac.tec.ce3104.tc3.gameobjects.Fruit;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.networking.Client;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class Game {
    private Integer playerId; //0,2,3
    private CEList<Client> clients;
    private Integer waterLvl;
    private CEList<EnvironmentObject> environmentObjects;
    private CEList<Fruit> fruits;
    private CEList<Crocodile> crocodiles;
    private PlayerAvatar player;
    public Game(){

    }
}
