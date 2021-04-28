package cr.ac.tec.ce3104.tc3.resources;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import cr.ac.tec.ce3104.tc3.physics.Size;

public class Sprite extends Sequence {
    public static final Sprite APPLE   = Sprite.byId(0);
    public static final Sprite BANANA  = Sprite.byId(1);
    public static final Sprite NISPERO = Sprite.byId(2);

    public static final Sprite VINES           = Sprite.byId(57);
    public static final Sprite VINES_WITH_LEAF = Sprite.byId(58);

    public static final Sprite STANDING_FIRST = Sprite.byId(14);
    public static final Sprite STANDING_LEFT  = Sprite.byId(54);
    public static final Sprite STANDING_RIGHT = Sprite.byId(5);
    public static final Sprite JUMPING_LEFT   = Sprite.byId(52);
    public static final Sprite JUMPING_RIGHT  = Sprite.byId(7);
    public static final Sprite FALLING_LEFT   = Sprite.byId(51);
    public static final Sprite FALLING_RIGHT  = Sprite.byId(8);
    public static final Sprite HANGING_LEFT   = Sprite.byId(50);
    public static final Sprite HANGING_RIGHT  = Sprite.byId(9);

    public static final Sprite DONKEY_KONG  = Sprite.byId(16);
    public static final Sprite MARIO        = Sprite.byId(21);
    public static final Sprite FALLING_BLUE = Sprite.byId(25);

    public static final Sprite WATER1   = Sprite.byId(37);
    public static final Sprite WATER2   = Sprite.byId(38);
    public static final Sprite DIRT     = Sprite.byId(39);
    public static final Sprite BRICK    = Sprite.byId(40);
    public static final Sprite GRASS1   = Sprite.byId(41);
    public static final Sprite GRASS2   = Sprite.byId(42);
    public static final Sprite GRASS3   = Sprite.byId(43);
    
    public static final Sprite KEY   = Sprite.byId(59);

    /**
     * Crea y carga una imagen como sprite a utilizar en el juego
     * @param id identificador asociado al archivo de imagen del sprite. Mismo para servidor y clientes
     * @return Sprite asociado al id provisto
     */
    public static Sprite byId(Integer id) {
        if (Sprite.sprites == null) {
            Sprite.sprites = new HashMap<>();

            try {
                for (File path : Assets.listSpritePaths()) {
                    Sprite sprite = new Sprite(path);
                    Sprite.sprites.put(sprite.id, sprite);
                }
            } catch (IOException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }

        return Sprite.sprites.get(id);
    }

    @Override
    public Sprite[] getSprites() {
        return new Sprite[] { this };
    }

    @Override
    public Size getSize() {
        return this.size;
    }
    /**
     * Obtiene el id asociado a la instancia de Sprite actual
     * @return identificador de la imagen del sprite actual
     */
    public Integer getId() {
        return this.id;
    }

    private static HashMap<Integer, Sprite> sprites = null;

    private Integer id;
    private Size size;
    /**
     * Crea una instancia de sprite a partir de una ruta de archivo
     * @param path ruta del archivo de imagen
     * @throws IOException error en caso de problemas al abrir archivo de imagen
     */
    private Sprite(File path) throws IOException {
        String filename = path.getName();
        this.id = Integer.parseInt(filename.substring(0, filename.indexOf('-')));

        BufferedImage image = ImageIO.read(path);
        this.size = new Size(image.getWidth(), image.getHeight());
    }
}
