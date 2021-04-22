package cr.ac.tec.ce3104.tc3.networking;

import java.util.List;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Command {
    public static Command cmdNew(Integer id, Position position, Sprite sprite) {
        return new Command().putString("op", "new").putInt("id", id)
                            .putInt("x", position.x).putInt("y", position.y)
                            .putInt("sprite", sprite.getId());
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
        return ((Long)this.json.get(key)).intValue();
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
