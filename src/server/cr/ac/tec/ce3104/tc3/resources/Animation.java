package cr.ac.tec.ce3104.tc3.resources;

public class Animation extends Sequence {
    public static final Animation RUNNING_LEFT   = new Animation(54, 53, 55);
    public static final Animation RUNNING_RIGHT  = new Animation(5, 6, 4);
    public static final Animation CLIMBING_LEFT  = new Animation(50, 49);
    public static final Animation CLIMBING_RIGHT = new Animation(9, 10);
    public static final Animation DYING          = new Animation(12, 13);

    public static final Animation RED_CROCODILE_DOWN = new Animation(33, 34);
    public static final Animation RED_CROCODILE_UP   = new Animation(35, 56);

    @Override
    public Sprite[] getSprites() {
        return this.sprites;
    }

    private Sprite[] sprites;

    private Animation(Integer... spriteIds) {
        this.sprites = new Sprite[spriteIds.length];
        for (Integer i = 0; i < spriteIds.length; ++i) {
            this.sprites[i] = Sprite.byId(spriteIds[i]);
        }
    }
}
