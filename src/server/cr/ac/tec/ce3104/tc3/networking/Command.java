package cr.ac.tec.ce3104.tc3.networking;

import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

// Un comando en formato JSON, sea recibido o enviado
public class Command {
    /**
     * Crea un objeto json que contiene informacion sobre un error y retorna un comando construido a partir de dicho json
     * @param message mensaje de error a ingresar como valor
     * @return Comando de error creado
     */
    public static Command cmdError(String message) {
        return new Command().putString("error", message);
    }

    /**
     * Crea el comando de comunicacion inicial con un cliente
     * @param clientId id del cliente al que se le envia el comando
     * @param gameIds ids de los juegos activos en el servidor actual
     * @return comando construido a partir de la informacion dada
     */
    public static Command cmdWhoAmI(Integer clientId, List<Integer> gameIds) {
        return new Command().putInt("whoami", clientId).putIntList("games", gameIds);
    }

    /**
     * COnstruye un comando que indica los parametros de pantalla que un cliente debe utilizar
     * @param size dimensiones de la pantalla
     * @return comando construido a patir de la informacion dada
     */
    public static Command cmdGameArea(Size size) {
        return new Command().putInt("width", size.getWidth())
                            .putInt("height", size.getHeight());
    }

    /**
     * Crea un comando para indicarle al cliente que debe dibujar una nueva entidad de juego
     * @param id identificador de la entidad a crear
     * @param position posicion en la que se quiere crear a entidad
     * @param z prioridad de dibujo de la entidad a crear
     * @param speed indica las velocidades horizontal y vertical del objeto creado
     * @param sequence indica la serie de sprites que corresponden a la animacion de un objeto
     * @return Comando creado a partir de la informacion dada
     */
    public static Command cmdPut(Integer id, Position position, Integer z, Speed speed, Sequence sequence) {
        List<Integer> sequenceIds = new ArrayList<>();
        for (Sprite sprite : sequence.getSprites()) {
            sequenceIds.add(sprite.getId());
        }

        return new Command().putString("op", "put")
                            .putInt("id", id)
                            .putInt("x", position.getX())
                            .putInt("y", position.getY())
                            .putInt("z", z)
                            .putInt("num_x", speed.getX().getNumerator())
                            .putInt("num_y", speed.getY().getNumerator())
                            .putInt("denom_x", speed.getX().getDenominator())
                            .putInt("denom_y", speed.getY().getDenominator())
                            .putIntList("seq", sequenceIds);
    }
    
    /**
     * Crea un comando que indica que una entidad debe ser eliminada
     * @param id identificador de la entidad a elimiarse
     * @return comando creado a enviarse al cliente
     */
    public static Command cmdDelete(Integer id) {
        return new Command().putString("op", "delete")
                            .putInt("id", id);
    }

    /**
     * Crea comando para indicarle al cliente que debe aplicar u efecto de resaltado a la entidad indicada 
     * @param id identificador de la entidad a resaltar
     * @return Comando creado para ser enviado al cliente
     */
    public static Command cmdHighlight(Integer id) {
        return new Command().putString("op", "highlight")
                            .putInt("id", id);
    }

    /**
     * Crea comando para indicarle al cliente que debe remover el efecto de resaltado de una entidad
     * @param id identificador de la entidad
     * @return Comando creado para ser enviado al cliente
     */
    public static Command cmdUnhighlight(Integer id) {
        return new Command().putString("op", "unhighlight")
                            .putInt("id", id);
    }

    /**
     * Crea un comando con la informacion de una partida de juego
     * @param lives vidas del jugador de la partida
     * @param score puntaje del jugador de la partida
     * @return Comando construido para enviar al cliente
     */
    public static Command cmdStats(Integer lives, Integer score) {
        return new Command().putString("op", "stats")
                            .putInt("lives", lives)
                            .putInt("score", score);
    }

    /**
     * Inicializa un nuevo comando sin contenido
     */
    public Command() {
        this.json = new JSONObject();
    }

    /**
     * Inicializa un nuevo comando a partir de un string que se encuentra formateado como un objeto json
     * @param source string que contiene objeto json a representar
     */
    public Command(String source) {
        this.json = (JSONObject)JSONValue.parse(source);
    }

    // Convierte a representación textual
    @Override
    public String toString() {
        return this.json.toJSONString();
    }

    /**
     * Extrae un valor entero de un objeto json
     * @param key llave que identifica al campo que contiene el entero a extraer 
     * @return numero entero extraido
     */
    public Integer expectInt(String key) {
        //tiene que castear a long para poder extraerlo por...razones
        Long value = (Long)this.json.get(key);
        return value != null ? value.intValue() : null;
    }

    /**
     * Extrae un valor string de un objeto json
     * @param key llave que identifica el campo en el que se encuentra el valor que se quiere extraer
     * @return String valor del campo extraido
     */
    public String expectString(String key) {
        return (String)this.json.get(key);
    }

    /**
     * Coloca un par llave-valor en el objeto JSON actual. El valor es un Integer
     * @param key llave del valor Integer a insertar
     * @param value valor string a agregar al objeto json
     * @return Comando actual actualizado
     */
    public Command putInt(String key, Integer value) {
        return this.put(key, value);
    }

    /**
     * Coloca un par llave-valor en el objeto JSON actual. El valor es un string
     * @param key llave del valor string a insertar
     * @param value valor string a agregar al objeto json
     * @return Comando actual actualizado
     */
    public Command putString(String key, String value) {
        return this.put(key, value);
    }

    /**
     * Crea un campo de que almacena una llave-lista en el objeto json del comando actual
     * @param key llave para identificar la lista
     * @param list lista a insertarse como JSON array
     * @return Comando actual actualizado
     */
    @SuppressWarnings("unchecked")
    public Command putIntList(String key, List<Integer> list) {
        JSONArray array = new JSONArray();
        array.addAll(list);

        return this.put(key, array);
    }

    // Objeto JSON interno
    private JSONObject json;

    /**
     * Agrega un par llave-valor al objeto json del comando
     * @param key llave del valor a insertar
     * @param value valor a insertar
     * @return Comando actual actualizado
     */
    @SuppressWarnings("unchecked")
    private Command put(String key, Object value) {
        this.json.put(key, value);
        return this;
    }
}
