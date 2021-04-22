package cr.ac.tec.ce3104.tc3.resources;

import java.io.File;

public class Assets {
    public static File pathToSprite(String key) {
        return new File("assets/sprites/" + key + ".png");
    }
}
