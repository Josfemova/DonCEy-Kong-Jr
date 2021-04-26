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

public class Command {
    public static Command cmdError(String message) {
        return new Command().putString("error", message);
    }

    public static Command cmdWhoAmI(Integer clientId, List<Integer> gameIds) {
        return new Command().putInt("whoami", clientId).putIntList("games", gameIds);
    }

    public static Command cmdGameArea(Size size) {
        return new Command().putInt("width", size.getWidth())
                            .putInt("height", size.getHeight());
    }

    public static Command cmdPut(Integer id, Position position, Speed speed, Sequence sequence) {
        List<Integer> sequenceIds = new ArrayList<>();
        for (Sprite sprite : sequence.getSprites()) {
            sequenceIds.add(sprite.getId());
        }

        return new Command().putString("op", "put")
                            .putInt("id", id)
                            .putInt("x", position.getX())
                            .putInt("y", position.getY())
                            .putInt("num_x", speed.getX().getNumerator())
                            .putInt("num_y", speed.getY().getNumerator())
                            .putInt("denom_x", speed.getX().getDenominator())
                            .putInt("denom_y", speed.getY().getDenominator())
                            .putIntList("seq", sequenceIds);
    }

    public static Command cmdDelete(Integer id) {
        return new Command().putString("op", "delete")
                            .putInt("id", id);
    }

    public static Command cmdHighlight(Integer id) {
        return new Command().putString("op", "highlight")
                            .putInt("id", id);
    }

    public static Command cmdUnhighlight(Integer id) {
        return new Command().putString("op", "unhighlight")
                            .putInt("id", id);
    }

    public static Command cmdStats(Integer lives, Integer score) {
        return new Command().putString("op", "stats")
                            .putInt("lives", lives)
                            .putInt("score", score);
    }

    public Command() {
        this.json = new JSONObject();
    }

    public Command(String source) {
        this.json = (JSONObject)JSONValue.parse(source);
    }

    @Override
    public String toString() {
        return this.json.toJSONString();
    }

    public Integer expectInt(String key) {
        //tiene que castear a long para poder extraerlo por...razones
        Long value = (Long)this.json.get(key);
        return value != null ? value.intValue() : null;
    }

    public String expectString(String key) {
        return (String)this.json.get(key);
    }

    public Command putInt(String key, Integer value) {
        return this.put(key, value);
    }

    public Command putString(String key, String value) {
        return this.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public Command putIntList(String key, List<Integer> list) {
        JSONArray array = new JSONArray();
        array.addAll(list);

        return this.put(key, array);
    }

    private JSONObject json;

    @SuppressWarnings("unchecked")
    private Command put(String key, Object value) {
        this.json.put(key, value);
        return this;
    }
}
