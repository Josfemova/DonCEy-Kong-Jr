package cr.ac.tec.ce3104.tc3;

import java.util.Map;

import org.json.simple.JSONObject;

import cr.ac.tec.ce3104.tc3.gameobjects.Crocodile;
import cr.ac.tec.ce3104.tc3.gameobjects.EnvironmentObject;
import cr.ac.tec.ce3104.tc3.gameobjects.Fruit;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.PlatformType;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.networking.ClientAdmin;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class Game extends Thread{
    
    private Integer playerId; //0,2,3
    private CEList<ClientAdmin> clients = new CEList<>(); //Observers
    private CEList<EnvironmentObject> environmentObjects= new CEList<>();
    private CEList<Fruit> fruits= new CEList<>();
    private CEList<Crocodile> crocodiles= new CEList<>();
    private PlayerAvatar player;
    public static final Integer lenghtUnit = 8; //8 px es dist. min
    public static final Integer screen[] = {256, 240};
    Integer ypos[] = new Integer[screen[1]/lenghtUnit];
    Integer xpos[] = new Integer[screen[0]/lenghtUnit];

    public Game(Integer playerId){
        this.playerId = playerId;
        for(int y = 0, i=0; y<screen[1]; y+=lenghtUnit, i++){
            ypos[i] = y; //posiciones horizontales de 0 a 248
        }
        for(int x = 0, i=0; x<screen[0]; x+=lenghtUnit, i++){
            xpos[i] = x; //posiciones horizontales de 0 a 248
        }
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

        //agua
        for(int i = 0; i<xpos.length;++i){
            environmentObjects.add(new Platform(xpos[i],ypos[29], PlatformType.WATER1));
            environmentObjects.add(new Platform(xpos[i],ypos[28], PlatformType.WATER2));
        }

        //plataformas
        for(int i=9;i<12;i++) environmentObjects.add(new Platform(xpos[i],ypos[5] , PlatformType.BRICK));
        for(int i=0;i<19;i++) environmentObjects.add(new Platform(xpos[i],ypos[8] , PlatformType.BRICK));
        for(int i=18;i<26;i++)environmentObjects.add(new Platform(xpos[i],ypos[9] , PlatformType.BRICK));
        for(int i=6;i<10;i++) environmentObjects.add(new Platform(xpos[i],ypos[14] , PlatformType.BRICK));
        for(int i=24;i<32;i++)environmentObjects.add(new Platform(xpos[i],ypos[17] , PlatformType.BRICK));
        for(int i=6;i<12;i++) environmentObjects.add(new Platform(xpos[i],ypos[19] , PlatformType.BRICK));
        
        //pasto
        drawGrass(0, 27, 8);
        environmentObjects.add(new Platform(0,ypos[19] , PlatformType.GRASS2));//tapa 1
        drawGrass(17, 26, 3);
        drawGrass(12, 25, 4);
        drawGrass(21, 25, 4);
        drawGrass(26, 24, 4);

        //tierra
        environmentObjects.add(new Platform(xpos[13],ypos[29] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[14],ypos[29] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[13],ypos[28] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[14],ypos[28] , PlatformType.DIRT));

        environmentObjects.add(new Platform(xpos[18],ypos[29] , PlatformType.DIRT));

        environmentObjects.add(new Platform(xpos[22],ypos[29] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[23],ypos[29] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[22],ypos[28] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[23],ypos[28] , PlatformType.DIRT));

        environmentObjects.add(new Platform(xpos[27],ypos[29] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[28],ypos[29] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[27],ypos[28] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[28],ypos[28] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[27],ypos[27] , PlatformType.DIRT));
        environmentObjects.add(new Platform(xpos[28],ypos[27] , PlatformType.DIRT));
    }
    private void drawGrass(Integer ixpos, Integer iypos, Integer lenght){
        environmentObjects.add(new Platform(xpos[ixpos], iypos , PlatformType.GRASS1));
        int i;
        for(i=1; i<lenght; i++)
            environmentObjects.add(new Platform(xpos[ixpos+i], iypos, PlatformType.GRASS2));
        environmentObjects.add(new Platform(xpos[ixpos+i], iypos , PlatformType.GRASS3));
    }
}
