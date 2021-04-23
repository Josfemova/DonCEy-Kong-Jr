package cr.ac.tec.ce3104.tc3.resources;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import cr.ac.tec.ce3104.tc3.physics.Size;

public class Sprite implements Sequence {
    public static final Sprite APPLE;
    public static final Sprite BANANA;
    public static final Sprite NISPERO;
    public static final Sprite VINES;
    public static final Sprite TODO; //FIXME

    static {
        try {
            APPLE   = new Sprite("static/00-apple");
            BANANA  = new Sprite("static/01-banana");
            NISPERO = new Sprite("static/02-nispero");
            VINES   = new Sprite("static/03-liana");
            TODO    = APPLE; //FIXME
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    public Sprite(String key) throws IOException {
        File path = Assets.pathToSprite(key);
        String filename = path.getName();

        this.id = Integer.parseInt(filename.substring(0, filename.indexOf('-')));

        BufferedImage image = ImageIO.read(path);
        this.size = new Size(image.getWidth(), image.getHeight());
    }

    @Override
    public Sprite[] getSprites() {
        return new Sprite[] { this };
    }

    public Integer getId() {
        return this.id;
    }

    public Size getSize() {
        return this.size;
    }

    private Integer id;
    private Size size;
}
