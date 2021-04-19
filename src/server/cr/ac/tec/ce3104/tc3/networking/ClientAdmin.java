package cr.ac.tec.ce3104.tc3.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.Server;
import cr.ac.tec.ce3104.tc3.util.CEList;

public class ClientAdmin extends Thread {

    private static Integer nextClientId = 0;
    private static CEList<JSONObject> commands = new CEList<>();
    private Game game;
    private Socket socket;
    private ClientType type;
    private Integer id;

    public ClientAdmin(Socket socket) {
        this.socket = socket;
        id = nextClientId;
        ++nextClientId;
        System.out.println("new user connected!! Id: " + id);
    }
    public Integer getClientId(){
        return id;
    }
    /**
     * Agrega un comando al queue de mensajes a enviar al cliente
     * @param cmd
     */
    public void addCommand(JSONObject cmd){ //equivalente a update() en patrón observer
        commands.add(cmd);
    }
    public void run() {
        BufferedReader requestReader;
        PrintWriter commandSender;
        try {
            requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            commandSender = new PrintWriter(socket.getOutputStream(), true); //por alguna razón esto sirve pero DataOutputStream no
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // tareas inciales

        JSONObject request;
        JSONObject command = new JSONObject();

        //por conveniencia temporal de legibilidad, todo en un solo try por ahora
        CEList<Integer> gameIds = Server.getInstance().getGameIds();
        try {
            // primer mensaje
            command.put("whoami", id);
            command.put("games", gameIds.toJsonArray());
            String cmd = command.toJSONString();
            commandSender.println(cmd);
            //commandSender.writeUTF(command.toString()+"\n");
            commandSender.flush();
            command.clear();

            // respuesta al init
            request = (JSONObject) JSONValue.parse(requestReader.readLine());
            System.out.println(request);
            System.out.println(request.get("init"));
            //tiene que castear a long para poder extraerlo por...razones
            Integer playerId = ((Long) request.get("init")).intValue();
            //suscribe a un jugador según el id provisto por init
            if (gameIds.contains(playerId)) {
                // start as spectator
                type = ClientType.SPECTATOR;
                game = Server.getInstance().initPlayer(this);
                //TODO
            } else {
                // start as player
                type = ClientType.PLAYER;
                game = Server.getInstance().initSpectator(playerId, this);
            }

            //resolución original del NES
            //aspect ratio 16:15 en caso de querer escalar
            command.put("width",256);
            command.put("height",240);
            commandSender.println(command.toJSONString());
            commandSender.flush();
            command.clear();

        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        // inicia loop de cliente
        //Todavía falta gran parte de la lógica acá
        while (true) {
            System.out.println("looking for updates");
            try {
                //Procesamiento de entrada de usuario
                if(type == ClientType.PLAYER){//es probable que se quieran administrar comandos de quit de parte del usuario
                    request = (JSONObject) JSONValue.parse(requestReader.readLine()); // 
                    System.out.println("received: " + request);
                    //TODO
                    //
                    //
                    //

                }
                if(!commands.isEmpty()){ //envía todo comando agregado desde última iteración
                    for(JSONObject cmd: commands){
                        commandSender.println(command.toJSONString());
                        commandSender.flush();
                        command.clear();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
