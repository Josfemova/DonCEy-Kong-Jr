package cr.ac.tec.ce3104.tc3.resources;

public class Animation implements Sequence {
    public static final Animation RUNNING_RIGHT = new Animation(4, 5, 6, 7);
    public static final Animation RUNNING_LEFT  = new Animation(55, 54, 53, 52);

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
