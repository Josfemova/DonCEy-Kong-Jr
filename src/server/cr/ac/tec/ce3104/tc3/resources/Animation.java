package cr.ac.tec.ce3104.tc3.resources;

public class Animation extends Sequence {
    public static final Animation RUNNING_LEFT   = new Animation(55, 54, 53);
    public static final Animation RUNNING_RIGHT  = new Animation(4, 5, 6);
    public static final Animation CLIMBING_LEFT  = new Animation(50, 49);
    public static final Animation CLIMBING_RIGHT = new Animation(9, 10);
    public static final Animation DYING          = new Animation(12, 13);

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
