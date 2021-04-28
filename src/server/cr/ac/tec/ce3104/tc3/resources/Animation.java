package cr.ac.tec.ce3104.tc3.resources;

// Una secuencia de varios sprites en bucle
public class Animation extends Sequence {
    // Animaciones de jugador
    public static final Animation RUNNING_LEFT   = new Animation(54, 53, 55);
    public static final Animation RUNNING_RIGHT  = new Animation(5, 6, 4);
    public static final Animation CLIMBING_LEFT  = new Animation(50, 49);
    public static final Animation CLIMBING_RIGHT = new Animation(9, 10);
    public static final Animation DYING          = new Animation(12, 13);

    // Animaciones de cocodrilos
    public static final Animation RED_CROCODILE_DOWN   = new Animation(33, 34);
    public static final Animation RED_CROCODILE_UP     = new Animation(35, 56);
    public static final Animation BLUE_CROCODILE_DOWN  = new Animation(27, 28);
    public static final Animation BLUE_CROCODILE_LEFT  = new Animation(44, 45);
    public static final Animation BLUE_CROCODILE_RIGHT = new Animation(25, 26);

    // Obtiene los sprites
    @Override
    public Sprite[] getSprites() {
        return this.sprites;
    }

    // Secuencia de sprites
    private Sprite[] sprites;

    /**
     * Crea una instancia de animación a partir de una lista de identificadores de imagen sprite
     * @param spriteIds lista de inicialización con los identificadores de los sprites que conforman la animación
     */
    private Animation(Integer... spriteIds) {
        this.sprites = new Sprite[spriteIds.length];
        for (Integer i = 0; i < spriteIds.length; ++i) {
            this.sprites[i] = Sprite.byId(spriteIds[i]);
        }
    }
}
